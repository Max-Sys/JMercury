package org.maxsys.jmercury.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    public static Socket GetNewSocket() {
        try {
            Socket newsocket = new Socket(Vars.SrvAddr, 4545);
            return newsocket;
        } catch (IOException ex) {
            Logger.getLogger(NetServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

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

    public static String GetRespFromSrv(Socket socket) {
        int ci;
        String si = "";
        try {
            while ((ci = socket.getInputStream().read()) >= 0 && ci != 0) {
                si += (char) ci;
            }
        } catch (IOException ex) {
            //Logger.getLogger(NetServer.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        }
        return si;
    }

    public static boolean sendIsMsrvPaused() {
        Socket socket = NetServer.GetNewSocket();
        NetServer.SendToSrv(socket, "GetMsvrStatus");
        String resp = NetServer.GetRespFromSrv(socket);
        NetServer.CloseSocket(socket);
        return resp.equals("Paused");
    }

    public static void sendMsvrRun() {
        Socket socket = NetServer.GetNewSocket();
        NetServer.SendToSrv(socket, "MsvrRun");
        NetServer.CloseSocket(socket);
    }

    public static void sendMsvrPause() {
        Socket socket = NetServer.GetNewSocket();
        NetServer.SendToSrv(socket, "MsvrPause");
        NetServer.CloseSocket(socket);
    }

    public static void sendStopServer() {
        Socket socket = NetServer.GetNewSocket();
        NetServer.SendToSrv(socket, "StopServer");
        NetServer.CloseSocket(socket);
    }

    public static boolean sendRefreshMeters() {
        Socket socket = NetServer.GetNewSocket();
        NetServer.SendToSrv(socket, "RefreshMeters");
        String resp = NetServer.GetRespFromSrv(socket);
        NetServer.CloseSocket(socket);
        return resp.equals("Ok");
    }

    public static String sendGetMetersData() {
        Socket socket = NetServer.GetNewSocket();
        NetServer.SendToSrv(socket, "getMetersData");
        String resp = NetServer.GetRespFromSrv(socket);
        NetServer.CloseSocket(socket);
        String metersData = PDM.getStringFromHex(resp);
        return metersData;
    }

    public static void sendDeleteMeterFromDB(int idInDB) {
        Socket socket = NetServer.GetNewSocket();
        NetServer.SendToSrv(socket, "deleteMeterFromDB");
        NetServer.SendToSrv(socket, String.valueOf(idInDB));
        NetServer.CloseSocket(socket);
    }

    public static String[] sendGetSerialPortNames() {
        Socket socket = NetServer.GetNewSocket();
        NetServer.SendToSrv(socket, "GetSerialPortNames");
        String resp = PDM.getStringFromHex(NetServer.GetRespFromSrv(socket));
        NetServer.CloseSocket(socket);
        return resp.split("\n");
    }

    @Override
    public void run() {
        System.out.println("NetServer is running!");

        msrv = new MeterServer();
        msrvt = new Thread(msrv);

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
                            System.out.println("socket " + mySocket + " (" + sock + "), cmd: " + cmd);
                        }

                        // Commands processing
                        /*
                         Команды сервера:
                         getStatus - возвращает информацию о работе сервера.
                         SaveAndConnect - соединяется с базой данных.
                         AddNewServerInstance - добавить новый инстанс сервера в базу данных.
                         MsvrRun - пуск сервера счетчиков.
                         MsvrPause - пауза сервера счетчиков.
                         GetMsvrStatus - взять статус сервера счетчиков.
                         getMetersData - взять строчку с данными всех счетчиков.
                         newMeterStatusChannel - взять статус счетчика.
                         deleteMeterFromDB - удалить счетчик из базы данных.
                         GetSerialPortNames - взять имена портов.
                         */
                        switch (cmd) {
                            case "StopServer":
                                StopServer();
                                break;
                            case "getStatus":
                                getStatus(sock);
                                break;
                            case "SaveAndConnect":
                                SaveAndConnect(sock);
                                break;
                            case "AddNewServerInstance":
                                AddNewServerInstance(sock);
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
                    Socket tempsock = NetServer.GetNewSocket();
                    NetServer.SendToSrv(tempsock, "");
                    NetServer.CloseSocket(tempsock);
                }

                private void getStatus(Socket socket) {
                    String status = Vars.Version + "\n";
                    status += "Server name: " + Vars.prop.getProperty("Servername") + "\n";
                    status += "Meters registred: " + Vars.meters.size();
                    NetServer.SendToSrv(socket, status);
                }

                private void SaveAndConnect(Socket socket) {
                    String resp = "Ok";

                    String paramstring = NetServer.GetRespFromSrv(socket);
                    String[] parameters = paramstring.split(";");
                    if (parameters.length != 4) {
                        resp = "Err";
                    }

                    if (!resp.equals("Err")) {

                        String servername = parameters[0];
                        String dburl = parameters[1];
                        String username = parameters[2];
                        String password = parameters[3];

                        PDM pdm = new PDM("em", username, password, dburl);

                        Boolean isNewServer = true;
                        ResultSet rs;
                        try {
                            rs = pdm.getResultSet("em", "SELECT k, `name` FROM servers WHERE hide = 0;");
                            while (rs.next()) {
                                String srvn = PDM.getStringFromHex(rs.getString("name"));
                                if (srvn.equals(servername)) {
                                    isNewServer = false;
                                    Vars.serverID = rs.getInt("k");
                                }
                            }
                        } catch (SQLException ex) {
                            Logger.getLogger(NetServer.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        pdm.closeResultSet();

                        if (isNewServer) {
                            resp = "NewServer";
                        } else {
                            Vars.prop.setProperty("DB_URL", dburl);
                            Vars.prop.setProperty("Username", username);
                            Vars.prop.setProperty("Userpass", password);
                            Vars.prop.setProperty("Servername", servername);
                            Vars.SaveProperties();
                        }
                    }
                    NetServer.SendToSrv(socket, resp);
                }

                private void AddNewServerInstance(Socket socket) {
                    String paramstring = NetServer.GetRespFromSrv(socket);
                    String[] parameters = paramstring.split(";");
                    if (parameters.length == 4) {
                        String servername = parameters[0];
                        String dburl = parameters[1];
                        String username = parameters[2];
                        String password = parameters[3];

                        PDM pdm = new PDM("em", username, password, dburl);
                        Vars.serverID = pdm.executeNonQueryAI("em", "INSERT INTO servers (`name`, hide) VALUES ('" + PDM.getHexString(servername) + "', 0)");

                        Vars.prop.setProperty("DB_URL", dburl);
                        Vars.prop.setProperty("Username", username);
                        Vars.prop.setProperty("Userpass", password);
                        Vars.prop.setProperty("Servername", servername);
                        Vars.SaveProperties();
                    }
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
                    Vars.meters.clear();

                    String resp;

                    if (Vars.serverID > -1) {
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
                                Vars.meters.put(emeter.getIdInDB(), emeter);
                            }
                        } catch (SQLException ex) {
                            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        pdm.closeResultSet();
                        resp = "Ok";
                    } else {
                        resp = "Err";
                    }
                    NetServer.SendToSrv(sock, resp);
                }

                private void getMetersData(Socket sock) {
                    String mdstr = "";
                    for (EMeter em : Vars.meters.values()) {
                        mdstr += em.getIdInDB();
                        mdstr += "\001";
                        mdstr += em.getMeterName();
                        mdstr += "\001";
                        mdstr += em.getGroupName();
                        mdstr += "\001";
                        mdstr += em.getMeterComPort();
                        mdstr += "\001";
                        mdstr += (em.getMeterAddress() & 0xFF);
                        mdstr += "\001";
                        mdstr += em.getMeterKi();
                        mdstr += "\001";
                        mdstr += "\n";
                    }
                    NetServer.SendToSrv(sock, PDM.getHexString(mdstr));
                }

                private void newMeterStatusChannel(Socket sock) {
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
                }

                private void deleteMeterFromDB(Socket sock) {
                    String ki = NetServer.GetRespFromSrv(sock);
                    PDM pdm = new PDM();
                    pdm.executeNonQueryUpdate("em", "UPDATE meters SET hide = 1 WHERE k = " + ki);
                }

                private void GetSerialPortNames(Socket sock) {
                    String[] strs;
                    if (System.getProperty("os.name").startsWith("Win")) {
                        strs = SerialPortList.getPortNames();
                    } else {
                        strs = Vars.getNixPortNames();
                    }

                    String resp = "";
                    for (String pn : strs) {
                        resp += pn + "\n";
                    }

                    NetServer.SendToSrv(sock, PDM.getHexString(resp));
                }
            }
            );
            ssrv.start();

            LastOpenedSocket++;
        }

        System.out.println("NetServer is closed!");
        System.exit(0);
    }
}
