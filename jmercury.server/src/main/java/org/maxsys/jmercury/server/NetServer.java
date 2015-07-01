package org.maxsys.jmercury.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.maxsys.dblib.PDM;

public class NetServer implements Runnable {

    private boolean isCancelled = false;
    private final HashMap<Integer, Socket> sockets = new HashMap<>();
    private int LastOpenedSocket = 0;
    private Thread msrvt;
    private MeterServer msrv;

    public static Socket GetNewSocket() {
        try {
            return new Socket(Vars.SrvAddr, 4545);
        } catch (IOException ex) {
            Logger.getLogger(NetServer.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
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
            Logger.getLogger(NetServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return si;
    }

    public static boolean isMsrvPaused() {
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

                private String readStringFromSock(Socket sock) {
                    String resp = "";
                    try {
                        int ci;
                        while ((ci = sock.getInputStream().read()) != -1 && ci != 0 && (char) ci != '!') {
                            resp += (char) ci;
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(NetServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return resp;
                }

                @Override
                public void run() {
                    Socket sock = sockets.get(mySocket);

                    while (!sock.isClosed()) {

                        // Read command
                        String cmd = readStringFromSock(sock);

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
                         GetMsvrStatus - взять статус сервера счетчиков
                        
                         */
                        if (cmd.equals("StopServer")) {
                            StopServer();
                        }

                        if (cmd.equals("getStatus")) {
                            getStatus(sock);
                        }

                        if (cmd.equals("SaveAndConnect")) {
                            SaveAndConnect(sock);
                        }

                        if (cmd.equals("AddNewServerInstance")) {
                            AddNewServerInstance(sock);
                        }

                        if (cmd.equals("MsvrRun")) {
                            MsvrRun();
                        }

                        if (cmd.equals("MsvrPause")) {
                            MsvrPause();
                        }

                        if (cmd.equals("GetMsvrStatus")) {
                            GetMsvrStatus(sock);
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
                    status += "\000";
                    try {
                        socket.getOutputStream().write(status.getBytes());
                    } catch (IOException ex) {
                        Logger.getLogger(NetServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                private void SaveAndConnect(Socket socket) {
                    String resp = "Ok\000";

                    String paramstring = readStringFromSock(socket);
                    String[] parameters = paramstring.split(";");
                    if (parameters.length != 4) {
                        resp = "Err\000";
                    }

                    if (!resp.equals("Err\000")) {

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
                            resp = "NewServer\000";
                        } else {
                            Vars.prop.setProperty("DB_URL", dburl);
                            Vars.prop.setProperty("Username", username);
                            Vars.prop.setProperty("Userpass", password);
                            Vars.prop.setProperty("Servername", servername);
                            Vars.SaveProperties();
                        }
                    }

                    try {
                        socket.getOutputStream().write(resp.getBytes());
                    } catch (IOException ex) {
                        Logger.getLogger(NetServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                private void AddNewServerInstance(Socket socket) {
                    String paramstring = readStringFromSock(socket);
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
                    status += "\000";
                    try {
                        sock.getOutputStream().write(status.getBytes());
                    } catch (IOException ex) {
                        Logger.getLogger(NetServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
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
