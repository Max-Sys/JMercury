package org.maxsys.jmercury.server;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import jssc.SerialPortList;
import org.maxsys.dblib.PDM;

public class NetServer implements Runnable {

    private boolean isCancelled = false;
    private final HashMap<Integer, Socket> sockets = new HashMap<>();
    private int LastOpenedSocket = 0;
    private Thread msrvt;
    private MeterServer msrv;

    public static void CloseSocket(Socket socket) {
        try {
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(NetServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void SendToSrv(Socket socket, String data) {
        try {
            socket.getOutputStream().write((data + "\000").getBytes());
        } catch (IOException ex) {
            Logger.getLogger(NetServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void SendToSrvBig(Socket socket, String data) {
        try {
            OutputStream os = socket.getOutputStream();
            try (BufferedOutputStream bos = new BufferedOutputStream(os, 4096); OutputStreamWriter isw = new OutputStreamWriter(bos); BufferedWriter bw = new BufferedWriter(isw)) {
                data += "\000";
                bw.write(data, 0, data.length());
            }
        } catch (IOException ex) {
            Logger.getLogger(NetServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static String GetRespFromSrv(Socket socket) {
        int ci;
        StringBuilder si = new StringBuilder();
        try {
            while ((ci = socket.getInputStream().read()) >= 0 && ci != 0) {
                si.append((char) ci);
            }
        } catch (IOException ex) {
            return "";
        }
        return si.toString();
    }

    @Override
    public void run() {
        STL.Log("NetServer: is running!");

        if (Vars.serverID != -1) {
            STL.Log("NetServer: loading meters...");
            PDM pdm = new PDM();
            ResultSet rs = pdm.getResultSet("em", "SELECT meters.k, meters.`name`, meters.group_id, meters.comport, meters.rsaddr, meters.serial, meters.ki, meters.flags, metergroups.groupname FROM meters LEFT JOIN metergroups ON meters.group_id = metergroups.k WHERE meters.server_id = " + Vars.serverID + " AND meters.hide = 0 ORDER BY meters.group_id, meters.`name`");
            try {
                while (rs.next()) {
                    Integer k = rs.getInt("meters.k");
                    String metername = PDM.getStringFromHex(rs.getString("meters.name"));
                    String groupname = PDM.getStringFromHex(rs.getString("metergroups.groupname"));
                    String comport = rs.getString("meters.comport");
                    Integer rsaddr = rs.getInt("meters.rsaddr");
                    String serial = rs.getString("meters.serial");
                    Integer ki = rs.getInt("meters.ki");
                    String flags = rs.getString("meters.flags");
                    EMeter emeter = new EMeter(metername, groupname, ki, comport, rsaddr, k);
                    emeter.setMeterSN(serial);
                    emeter.setMeterFlags(flags);
                    emeter.setMeterFlag("busy", "no");
                    emeter.setMeterFlag("preosv", "no");
                    Vars.meters.put(emeter.getIdInDB(), emeter);
                }
            } catch (SQLException ex) {
                Logger.getLogger(NetServer.class.getName()).log(Level.SEVERE, null, ex);
            }
            pdm.closeResultSet();
            STL.Log("NetServer: " + Vars.meters.size() + " meters loaded.");
        } else {
            STL.Log("NetServer: error loading meters!");
            System.exit(-1);
        }

        msrv = new MeterServer();
        msrvt = new Thread(msrv);
        msrvt.start();
        if (Vars.isConsole) {
            msrv.setMsvrPaused(false);
        } else {
            msrv.setMsvrPaused(true);
        }

        ServerSocket ssocket = null;

        try {
            ssocket = new ServerSocket(4545);
        } catch (IOException ex) {
            Logger.getLogger(NetServer.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (ssocket == null) {
            return;
        }

        while (!isCancelled) {
            Socket socket = null;
            try {
                socket = ssocket.accept();
            } catch (IOException ex) {
                Logger.getLogger(NetServer.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (socket == null) {
                continue;
            }

            sockets.put(LastOpenedSocket, socket);

            Thread ssrv;
            ssrv = new Thread(new Runnable() {

                private final int mySocket = LastOpenedSocket;

                @Override
                public void run() {
                    Socket sock = sockets.get(mySocket);

                    while (!sock.isClosed()) {

                        // Read command
                        String cmd = NetServer.GetRespFromSrv(sock);

                        if (cmd.isEmpty()) {
                            try {
                                sock.close();
                            } catch (IOException ex) {
                                Logger.getLogger(NetServer.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        } else {
                            STL.Log("NetServer: socket " + mySocket + " (" + sock + "), cmd: " + cmd);
                        }

                        // Commands processing
                        /*
                         Команды сервера:
                         getStatus - возвращает информацию о работе сервера.
                         GetServerProps - возвращает настройки сервера.
                         MsvrRun - пуск сервера счетчиков.
                         MsvrPause - пауза сервера счетчиков.
                         NonQuerySQL - выполнить sql запрос на сервере.
                         NonQuerySQLai - выполнить sql запрос на сервере и вернуть ai.
                         GetMsvrStatus - взять статус сервера счетчиков.
                         getMetersData - взять строчку с данными всех счетчиков.
                         newMeterStatusChannel - взять статус счетчика.
                         GetMeterAddress - взять адрес счетчика.
                         GetMeterSN - взять серийный номер счетчика.
                         deleteMeterFromDB - удалить счетчик из базы данных.
                         UpdateMeterInDB - обновить информацию о счетчике в БД.
                         GetSerialPortNames - взять имена портов.
                         GetMeterGroupNames - взять группы счетчиков с сервера.
                         GetMeterInfo - взять информацию о счетчике (имя, порт и т.д.)
                         GetAvgArs - взять AvgARs с ... по
                         GetApRpDays - взять ApRp за дни
                         GetApRpMonths - взять ApRp за месяцы
                         GetMeterDiagram - взять данные для диаграммы
                         GetMinMaxMonth - взять минимальный и максимальный Calendar в таблице месяцев
                         GetForReport - взять ForReport
                         GetLog - взять лог.
                         */
                        switch (cmd) {
                            case "StopServer":
                                StopServer();
                                break;
                            case "getStatus":
                                getStatus(sock);
                                break;
                            case "MsvrRun":
                                MsvrRun();
                                break;
                            case "MsvrPause":
                                MsvrPause();
                                break;
                            case "GetMsvrStatus":
                                GetMsvrStatus(sock);
                                break;
                            case "RefreshMeters":
                                RefreshMeters(sock);
                                break;
                            case "getMetersData":
                                getMetersData(sock);
                                break;
                            case "newMeterStatusChannel":
                                newMeterStatusChannel(sock);
                                break;
                            case "deleteMeterFromDB":
                                deleteMeterFromDB(sock);
                                break;
                            case "GetSerialPortNames":
                                GetSerialPortNames(sock);
                                break;
                            case "GetMeterGroupNames":
                                GetMeterGroupNames(sock);
                                break;
                            case "GetMeterAddress":
                                GetMeterAddress(sock);
                                break;
                            case "GetMeterSN":
                                GetMeterSN(sock);
                                break;
                            case "GetServerProps":
                                GetServerProps(sock);
                                break;
                            case "NonQuerySQL":
                                NonQuerySQL(sock);
                                break;
                            case "NonQuerySQLai":
                                NonQuerySQLai(sock);
                                break;
                            case "GetMeterFlags":
                                GetMeterFlags(sock);
                                break;
                            case "SetMeterFlags":
                                SetMeterFlags(sock);
                                break;
                            case "GetMeterInfo":
                                GetMeterInfo(sock);
                                break;
                            case "UpdateMeterInDB":
                                UpdateMeterInDB(sock);
                                break;
                            case "GetLog":
                                GetLog(sock);
                                break;
                            case "GetAvgArs":
                                GetAvgArs(sock);
                                break;
                            case "GetApRpDays":
                                GetApRpDays(sock);
                                break;
                            case "GetApRpMonths":
                                GetApRpMonths(sock);
                                break;
                            case "GetMeterDiagram":
                                GetMeterDiagram(sock);
                                break;
                            case "GetMinMaxMonth":
                                GetMinMaxMonth(sock);
                                break;
                            case "GetForReport":
                                GetForReport(sock);
                                break;
                        }
                    }
                    sockets.remove(mySocket);
                }

                private void StopServer() {
                    msrv.setMsvrRunning(false);

                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(NetServer.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    isCancelled = true;

                    try {
                        Socket tempsock = new Socket(Vars.SrvAddr, 4545);
                        NetServer.SendToSrv(tempsock, "");
                        NetServer.CloseSocket(tempsock);
                    } catch (IOException ex) {
                        Logger.getLogger(NetServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                private void getStatus(Socket socket) {
                    String status = Vars.Version + "\n";
                    status += "Server name: " + Vars.prop.getProperty("Servername") + "\n";
                    status += "Meters registred: " + Vars.meters.size();
                    NetServer.SendToSrv(socket, status);
                }

                private void MsvrRun() {
                    if (!msrvt.isAlive()) {
                        msrvt.start();
                    }
                    msrv.setMsvrPaused(false);
                }

                private void MsvrPause() {
                    msrv.setMsvrPaused(true);
                }

                private void GetMsvrStatus(Socket sock) {
                    String status = "";
                    if (msrv.isIsMsvrPaused()) {
                        status += "Paused";
                    } else {
                        status += "Running";
                    }
                    NetServer.SendToSrv(sock, status);
                }

                private void RefreshMeters(Socket sock) {
                    String resp;

                    if (Vars.serverID > -1) {
                        PDM pdm = new PDM();
                        String sql = "SELECT meters.k, meters.`name`, meters.group_id, meters.comport, meters.rsaddr, meters.serial, meters.ki, meters.flags, metergroups.groupname FROM meters LEFT JOIN metergroups ON meters.group_id = metergroups.k WHERE meters.server_id = " + Vars.serverID + " AND meters.hide = 0 ORDER BY meters.group_id, meters.`name`";
                        ResultSet rs = pdm.getResultSet("em", sql);
                        try {
                            while (rs.next()) {
                                Integer k = rs.getInt("meters.k");
                                if (!Vars.meters.containsKey(k)) {
                                    String metername = PDM.getStringFromHex(rs.getString("meters.name"));
                                    String groupname = PDM.getStringFromHex(rs.getString("metergroups.groupname"));
                                    String comport = rs.getString("meters.comport");
                                    Integer rsaddr = rs.getInt("meters.rsaddr");
                                    String serial = rs.getString("meters.serial");
                                    Integer ki = rs.getInt("meters.ki");
                                    String flags = rs.getString("meters.flags");
                                    EMeter emeter = new EMeter(metername, groupname, ki, comport, rsaddr, k);
                                    emeter.setMeterSN(serial);
                                    emeter.setMeterFlags(flags);
                                    emeter.setMeterFlag("busy", "no");
                                    Vars.meters.put(emeter.getIdInDB(), emeter);
                                    System.out.println("Adding " + metername);
                                }
                            }
                        } catch (SQLException ex) {
                            Logger.getLogger(NetServer.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        pdm.closeResultSet();
                        resp = "Ok";
                    } else {
                        resp = "Err";
                    }

                    NetServer.SendToSrv(sock, resp);
                }

                private void getMetersData(Socket sock) {
                    StringBuilder mdstr = new StringBuilder();

                    Object[] meters = Vars.meters.values().toArray();
                    Arrays.sort(meters, new Comparator<Object>() {

                        @Override
                        public int compare(Object o1, Object o2) {
                            EMeter em1 = (EMeter) o1;
                            EMeter em2 = (EMeter) o2;
                            String cs1 = em1.getGroupName() + em1.getMeterName();
                            String cs2 = em2.getGroupName() + em2.getMeterName();
                            return cs1.compareTo(cs2);
                        }
                    });

                    for (Object emo : meters) {
                        EMeter em = (EMeter) emo;
                        mdstr.append(em.getIdInDB());
                        mdstr.append("\001");
                        mdstr.append(em.getMeterName());
                        mdstr.append("\001");
                        mdstr.append(em.getGroupName());
                        mdstr.append("\001");
                        mdstr.append(em.getMeterComPort());
                        mdstr.append("\001");
                        mdstr.append(em.getMeterAddress() & 0xFF);
                        mdstr.append("\001");
                        mdstr.append(em.getMeterKi());
                        mdstr.append("\001");
                        mdstr.append(em.getMeterSN());
                        mdstr.append("\001");
                        mdstr.append(em.getMeterFlag("osv") == null ? "no" : em.getMeterFlag("osv"));
                        mdstr.append("\001");
                        mdstr.append("\n");
                    }
                    NetServer.SendToSrv(sock, PDM.getHexString(mdstr.toString()));
                }

                private void newMeterStatusChannel(Socket sock) {
                    STL.Log("NetServer: StatusChannel created (" + sock + ")");
                    String statcmd = "";
                    while (!statcmd.equals("close")) {
                        statcmd = GetRespFromSrv(sock);
                        if (statcmd.equals("get")) {
                            String statuses = "-1\001";
                            if (msrv.isIsMsvrPaused()) {
                                statuses += "MsvrPaused\n";
                            } else {
                                statuses += "MsvrRunning\n";
                            }

                            for (EMeter em : Vars.meters.values()) {
                                statuses += em.getIdInDB();
                                statuses += "\001";
                                statuses += em.getStatus();
                                statuses += "\n";
                            }

                            NetServer.SendToSrv(sock, PDM.getHexString(statuses));
                        }
                        if (statcmd.isEmpty()) {
                            break;
                        }
                    }
                    STL.Log("NetServer: StatusChannel closed (" + sock + ")");
                }

                private void deleteMeterFromDB(Socket sock) {
                    String ki = NetServer.GetRespFromSrv(sock);
                    PDM pdm = new PDM();
                    pdm.executeNonQueryUpdate("em", "UPDATE meters SET hide = 1 WHERE k = " + ki);
                    Vars.meters.remove(Integer.valueOf(ki));
                }

                private void GetSerialPortNames(Socket sock) {
                    String[] strs;
                    if (System.getProperty("os.name").startsWith("Win")) {
                        strs = SerialPortList.getPortNames();
                    } else {
                        strs = Vars.getNixPortNames();
                    }

                    StringBuilder resp = new StringBuilder();
                    for (String pn : strs) {
                        resp.append(pn).append("\n");
                    }

                    NetServer.SendToSrv(sock, PDM.getHexString(resp.toString()));
                }

                private void GetMeterGroupNames(Socket sock) {
                    StringBuilder groups = new StringBuilder();
                    PDM pdm = new PDM();
                    ResultSet rs = pdm.getResultSet("em", "SELECT k, groupname FROM metergroups WHERE hide = 0");
                    try {
                        while (rs.next()) {
                            groups.append(rs.getInt("k")).append("\001");
                            groups.append(rs.getString("groupname")).append("\n");
                        }
                    } catch (SQLException ex) {
                        Logger.getLogger(NewMeterDialog.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    pdm.closeResultSet();
                    NetServer.SendToSrv(sock, PDM.getHexString(groups.toString()));
                }

                private void GetMeterAddress(Socket sock) {
                    String portname = PDM.getStringFromHex(NetServer.GetRespFromSrv(sock));
                    EMeter meter = new EMeter("", "", 1, portname, 0, 0);
                    byte b;
                    try {
                        b = meter.getMeterAddress();
                    } catch (Exception e) {
                        NetServer.SendToSrv(sock, "null");
                        return;
                    }
                    NetServer.SendToSrv(sock, String.valueOf(b));
                }

                private void GetMeterSN(Socket sock) {
                    String portname = PDM.getStringFromHex(NetServer.GetRespFromSrv(sock));
                    EMeter meter = new EMeter("", "", 1, portname, 0, 0);
                    String sn = meter.getMeterSN();
                    if (sn == null) {
                        NetServer.SendToSrv(sock, "null");
                    } else {
                        NetServer.SendToSrv(sock, sn);
                    }
                }

                private void GetServerProps(Socket sock) {
                    String props = Vars.prop.getProperty("Servername") + "\n";
                    props += Vars.serverID;
                    NetServer.SendToSrv(sock, props);
                }

                private void NonQuerySQL(Socket sock) {
                    String sql = PDM.getStringFromHex(NetServer.GetRespFromSrv(sock));
                    PDM pdm = new PDM();
                    pdm.executeNonQuery("em", sql);
                }

                private void NonQuerySQLai(Socket sock) {
                    String sql = PDM.getStringFromHex(NetServer.GetRespFromSrv(sock));
                    PDM pdm = new PDM();
                    int ai = pdm.executeNonQueryAI("em", sql);
                    NetServer.SendToSrv(sock, String.valueOf(ai));
                }

                private void GetMeterFlags(Socket sock) {
                    String ki = NetServer.GetRespFromSrv(sock);
                    String flags = Vars.meters.get(Integer.valueOf(ki)).getMeterName() + "\n" + Vars.meters.get(Integer.valueOf(ki)).getMeterFlags();
                    NetServer.SendToSrv(sock, PDM.getHexString(flags));
                }

                private void SetMeterFlags(Socket sock) {
                    String kis = NetServer.GetRespFromSrv(sock);
                    int ki = Integer.valueOf(kis);
                    String flags = PDM.getStringFromHex(NetServer.GetRespFromSrv(sock));

                    Vars.meters.get(ki).setMeterFlags(flags);

                    PDM pdm = new PDM();
                    pdm.executeNonQueryUpdate("em", "UPDATE meters SET flags = '" + Vars.meters.get(ki).getMeterFlags() + "' WHERE k = " + Vars.meters.get(ki).getIdInDB());
                }

                private void GetMeterInfo(Socket sock) {
                    int ki = Integer.valueOf(NetServer.GetRespFromSrv(sock));
                    EMeter em = Vars.meters.get(ki);
                    String info = em.getMeterName() + "\n";
                    info += em.getGroupName() + "\n";
                    info += em.getMeterComPort() + "\n";
                    info += String.valueOf(em.getMeterAddress() & 0xFF) + "\n";
                    info += em.getMeterSN() + "\n";
                    info += String.valueOf(em.getMeterKi());
                    NetServer.SendToSrv(sock, PDM.getHexString(info));
                }

                private void UpdateMeterInDB(Socket sock) {
                    int idInDB = Integer.valueOf(NetServer.GetRespFromSrv(sock));
                    String sql = PDM.getStringFromHex(NetServer.GetRespFromSrv(sock));
                    PDM pdm = new PDM();
                    pdm.executeNonQuery("em", sql);
                    Vars.meters.remove(idInDB);
                }

                private void GetLog(Socket sock) {
                    String filter = PDM.getStringFromHex(NetServer.GetRespFromSrv(sock));
                    String log = STL.getLog(filter);
                    NetServer.SendToSrvBig(sock, PDM.getHexString(log));
                }

                private void GetAvgArs(Socket sock) {
                    String paramstr = PDM.getStringFromHex(NetServer.GetRespFromSrv(sock));

                    String[] params = paramstr.split("\n");
                    int IdInDB = Integer.valueOf(params[0]);
                    long longFrom = Long.valueOf(params[1]);
                    long longTo = Long.valueOf(params[2]);

                    Calendar caFrom = new GregorianCalendar();
                    caFrom.setTimeInMillis(longFrom);
                    Calendar caTo = new GregorianCalendar();
                    caTo.setTimeInMillis(longTo);

                    StringBuilder resp = new StringBuilder();

                    PDM pdm = new PDM();
                    String sql = "SELECT Aplus, Aminus, Rplus, Rminus, arPeriod, arDT FROM avgars WHERE hide = 0 AND meter_id = " + IdInDB + " AND arDT BETWEEN '" + PDM.getDTString(caFrom) + "' AND '" + PDM.getDTString(caTo) + "' ORDER BY arDT";
                    ResultSet rs = pdm.getResultSet("em", sql);
                    try {
                        while (rs.next()) {
                            //AvgAR aar = new AvgAR(ap, am, rp, rm, dt, period);
                            resp.append(rs.getDouble("Aplus"));
                            resp.append("\001");
                            resp.append(rs.getDouble("Aminus"));
                            resp.append("\001");
                            resp.append(rs.getDouble("Rplus"));
                            resp.append("\001");
                            resp.append(rs.getDouble("Rminus"));
                            resp.append("\001");
                            resp.append(PDM.getCalendarFromTime(rs.getTimestamp("arDT")).getTimeInMillis());
                            resp.append("\001");
                            resp.append(rs.getInt("arPeriod"));
                            resp.append("\001");
                            resp.append("\n");
                        }
                    } catch (SQLException ex) {
                        Logger.getLogger(NetServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    pdm.closeResultSet();

                    NetServer.SendToSrvBig(sock, PDM.getHexString(resp.toString()));
                }

                private void GetApRpDays(Socket sock) {
                    String paramstr = PDM.getStringFromHex(NetServer.GetRespFromSrv(sock));
                    int IdInDB = Integer.valueOf(paramstr);

                    StringBuilder resp = new StringBuilder();

                    PDM pdm = new PDM();
                    String sql = "SELECT Aplus, Rplus, dayDT FROM daydata WHERE hide = 0 AND meter_id = " + IdInDB + " ORDER BY dayDT";
                    ResultSet rs = pdm.getResultSet("em", sql);
                    try {
                        while (rs.next()) {
                            resp.append(rs.getDouble("Aplus"));
                            resp.append("\001");
                            resp.append(rs.getDouble("Rplus"));
                            resp.append("\001");
                            resp.append(PDM.getCalendarFromTime(rs.getTimestamp("dayDT")).getTimeInMillis());
                            resp.append("\001");
                            resp.append("\n");
                        }
                    } catch (SQLException ex) {
                        Logger.getLogger(NetServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    pdm.closeResultSet();

                    NetServer.SendToSrvBig(sock, PDM.getHexString(resp.toString()));
                }

                private void GetApRpMonths(Socket sock) {
                    String paramstr = PDM.getStringFromHex(NetServer.GetRespFromSrv(sock));
                    int IdInDB = Integer.valueOf(paramstr);

                    StringBuilder resp = new StringBuilder();

                    PDM pdm = new PDM();
                    String sql = "SELECT Aplus, Rplus, AplusOnBeg, RplusOnBeg, AplusOnEnd, RplusOnEnd, monthDT FROM monthdata WHERE hide = 0 AND meter_id = " + IdInDB + " ORDER BY monthDT";
                    ResultSet rs = pdm.getResultSet("em", sql);
                    try {
                        while (rs.next()) {
                            resp.append(rs.getDouble("Aplus"));
                            resp.append("\001");
                            resp.append(rs.getDouble("Rplus"));
                            resp.append("\001");
                            resp.append(rs.getDouble("AplusOnBeg"));
                            resp.append("\001");
                            resp.append(rs.getDouble("RplusOnBeg"));
                            resp.append("\001");
                            resp.append(rs.getDouble("AplusOnEnd"));
                            resp.append("\001");
                            resp.append(rs.getDouble("RplusOnEnd"));
                            resp.append("\001");
                            resp.append(PDM.getCalendarFromTime(rs.getTimestamp("monthDT")).getTimeInMillis());
                            resp.append("\001");
                            resp.append("\n");
                        }
                    } catch (SQLException ex) {
                        Logger.getLogger(NetServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    pdm.closeResultSet();

                    NetServer.SendToSrvBig(sock, PDM.getHexString(resp.toString()));
                }

                private void GetMeterDiagram(Socket sock) {
                    int ki = Integer.valueOf(NetServer.GetRespFromSrv(sock));
                    EMeter em = Vars.meters.get(ki);
                    String busy = em.getMeterFlag("busy") == null ? "no" : em.getMeterFlag("busy");
                    if (busy.equals("yes")) {
                        NetServer.SendToSrv(sock, "busy");
                        return;
                    }

                    em.setMeterFlag("busy", "yes");
                    em.setMeterFlag("busy_timer", "60");

                    String resp = ">>>";
                    NetServer.SendToSrv(sock, resp);

                    Calendar camt = em.getMeterTime();
                    if (camt == null) {
                        resp = "err";
                    } else {
                        resp = String.valueOf(camt.getTimeInMillis());
                    }

                    NetServer.SendToSrv(sock, resp);

                    resp = String.valueOf(em.getU1());
                    NetServer.SendToSrv(sock, resp);
                    resp = String.valueOf(em.getU2());
                    NetServer.SendToSrv(sock, resp);
                    resp = String.valueOf(em.getU3());
                    NetServer.SendToSrv(sock, resp);
                    resp = String.valueOf(em.getFreq());
                    NetServer.SendToSrv(sock, resp);

                    resp = String.valueOf(em.getI1());
                    NetServer.SendToSrv(sock, resp);
                    resp = String.valueOf(em.getI2());
                    NetServer.SendToSrv(sock, resp);
                    resp = String.valueOf(em.getI3());
                    NetServer.SendToSrv(sock, resp);

                    resp = String.valueOf(em.getP1());
                    NetServer.SendToSrv(sock, resp);
                    resp = String.valueOf(em.getP2());
                    NetServer.SendToSrv(sock, resp);
                    resp = String.valueOf(em.getP3());
                    NetServer.SendToSrv(sock, resp);
                    resp = String.valueOf(em.getPsum());
                    NetServer.SendToSrv(sock, resp);

                    resp = String.valueOf(em.getQ1());
                    NetServer.SendToSrv(sock, resp);
                    resp = String.valueOf(em.getQ2());
                    NetServer.SendToSrv(sock, resp);
                    resp = String.valueOf(em.getQ3());
                    NetServer.SendToSrv(sock, resp);
                    resp = String.valueOf(em.getQsum());
                    NetServer.SendToSrv(sock, resp);

                    resp = String.valueOf(em.getS1());
                    NetServer.SendToSrv(sock, resp);
                    resp = String.valueOf(em.getS2());
                    NetServer.SendToSrv(sock, resp);
                    resp = String.valueOf(em.getS3());
                    NetServer.SendToSrv(sock, resp);
                    resp = String.valueOf(em.getSsum());
                    NetServer.SendToSrv(sock, resp);

                    resp = String.valueOf(em.getA12());
                    NetServer.SendToSrv(sock, resp);
                    resp = String.valueOf(em.getA13());
                    NetServer.SendToSrv(sock, resp);
                    resp = String.valueOf(em.getA23());
                    NetServer.SendToSrv(sock, resp);

                    em.setMeterFlag("busy", "no");
                    em.setMeterFlag("preosv", "no");
                    em.setMeterFlag("osv", "no");
                    em.setMeterFlag("statusstr", "");
                }

                private void GetMinMaxMonth(Socket sock) {
                    PDM pdm = new PDM();
                    Object[] row = pdm.getSingleRow("em", "SELECT MIN(monthDT), MAX(monthDT) FROM monthdata WHERE meter_id IN (SELECT meter_id FROM meters WHERE server_id = " + Vars.serverID + ")");
                    if (row.length != 2) {
                        NetServer.SendToSrv(sock, "");
                        return;
                    }
                    Calendar c0 = PDM.getCalendarFromTime((Timestamp) row[0]);
                    Calendar c1 = PDM.getCalendarFromTime((Timestamp) row[1]);
                    String resp = String.valueOf(c0.getTimeInMillis()) + "\n" + String.valueOf(c1.getTimeInMillis());
                    NetServer.SendToSrv(sock, PDM.getHexString(resp));
                }

                private void GetForReport(Socket sock) {
                    int Year = Integer.valueOf(NetServer.GetRespFromSrv(sock));
                    int Month = Integer.valueOf(NetServer.GetRespFromSrv(sock));

                    PDM pdm = new PDM();
                    String sql = "SELECT"
                            + " metergroups.groupname AS GroupName"
                            + ", meters.name AS MeterName"
                            + ", meters.serial AS MeterSN"
                            + ", monthdata.AplusOnBeg / meters.ki AS Aplus1"
                            + ", monthdata.AplusOnEnd / meters.ki AS Aplus2"
                            + ", (monthdata.AplusOnEnd / meters.ki) - (monthdata.AplusOnBeg / meters.ki) AS Aplus21"
                            + ", meters.ki AS MeterKi"
                            + ", monthdata.Aplus AS Aplus21Ki"
                            + " FROM monthdata LEFT JOIN meters ON monthdata.meter_id = meters.k LEFT JOIN metergroups ON meters.group_id = metergroups.k"
                            + " WHERE meters.hide = 0 AND monthdata.hide = 0 AND metergroups.hide = 0"
                            + " AND YEAR(monthdata.monthDT) = " + Year
                            + " AND MONTH(monthdata.monthDT) = " + Month
                            + " AND meters.server_id = " + Vars.serverID
                            + " ORDER BY metergroups.groupname, meters.name";
                    ResultSet rs = pdm.getResultSet("em", sql);
                    StringBuilder resp = new StringBuilder();
                    DecimalFormat df = new DecimalFormat("#.##");
                    try {
                        while (rs.next()) {
                            resp.append(PDM.getStringFromHex(rs.getString("GroupName")));
                            resp.append("\001");
                            resp.append(PDM.getStringFromHex(rs.getString("MeterName")));
                            resp.append("\001");
                            resp.append(rs.getString("MeterSN"));
                            resp.append("\001");
                            resp.append(df.format(rs.getDouble("Aplus1")));
                            resp.append("\001");
                            resp.append(df.format(rs.getDouble("Aplus2")));
                            resp.append("\001");
                            resp.append(df.format(rs.getDouble("Aplus21")));
                            resp.append("\001");
                            resp.append(rs.getInt("MeterKi"));
                            resp.append("\001");
                            resp.append(df.format(rs.getDouble("Aplus21Ki")));
                            resp.append("\001");
                            resp.append("\n");
                        }
                    } catch (SQLException ex) {
                        Logger.getLogger(NetServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    pdm.closeResultSet();

                    NetServer.SendToSrvBig(sock, PDM.getHexString(resp.toString()));
                }
            }
            );
            ssrv.start();

            LastOpenedSocket++;
        }

        STL.Log("NetServer: is closed!");
        System.exit(0);
    }
}
