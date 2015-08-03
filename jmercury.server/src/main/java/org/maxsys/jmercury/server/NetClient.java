package org.maxsys.jmercury.server;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.maxsys.dblib.PDM;

public class NetClient {

    public static Socket GetNewSocket() {
        try {
            Socket newsocket = new Socket(Vars.SrvAddr, 4545);
            return newsocket;
        } catch (IOException ex) {
            Logger.getLogger(NetClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static void CloseSocket(Socket socket) {
        try {
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(NetClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void SendToSrv(Socket socket, String data) {
        try {
            socket.getOutputStream().write((data + "\000").getBytes());
        } catch (IOException ex) {
            Logger.getLogger(NetClient.class.getName()).log(Level.SEVERE, null, ex);
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

    public static String GetRespFromSrvBig(Socket socket) {
        StringBuilder strbld = new StringBuilder();
        try {
            try (InputStream is = socket.getInputStream(); BufferedInputStream bis = new BufferedInputStream(is, 4096); InputStreamReader isr = new InputStreamReader(bis); BufferedReader br = new BufferedReader(isr)) {
                strbld.append(br.readLine());
            }
        } catch (IOException ex) {
            Logger.getLogger(NetClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return strbld.toString();
    }

    public static Properties sendGetServerProps() {
        Socket socket = GetNewSocket();
        SendToSrv(socket, "GetServerProps");
        String resp = GetRespFromSrv(socket);
        CloseSocket(socket);

        String[] parameters = resp.split("\n");
        String servername = parameters[0];
        String serverid = parameters[1];

        Properties props = new Properties();
        props.setProperty("Servername", servername);
        props.setProperty("ServerID", serverid);

        return props;
    }

    public static boolean sendIsMsrvPaused() {
        Socket socket = GetNewSocket();
        SendToSrv(socket, "GetMsvrStatus");
        String resp = GetRespFromSrv(socket);
        CloseSocket(socket);
        return resp.equals("Paused");
    }

    public static void sendMsvrRun() {
        Socket socket = GetNewSocket();
        SendToSrv(socket, "MsvrRun");
        CloseSocket(socket);
    }

    public static void sendMsvrPause() {
        Socket socket = GetNewSocket();
        SendToSrv(socket, "MsvrPause");
        CloseSocket(socket);
    }

    public static void sendStopServer() {
        Socket socket = GetNewSocket();
        SendToSrv(socket, "StopServer");
        CloseSocket(socket);
    }

    public static boolean sendRefreshMeters() {
        Socket socket = GetNewSocket();
        SendToSrv(socket, "RefreshMeters");
        String resp = GetRespFromSrv(socket);
        CloseSocket(socket);
        return resp.equals("Ok");
    }

    public static String sendGetMetersData() {
        Socket socket = GetNewSocket();
        SendToSrv(socket, "getMetersData");
        String resp = GetRespFromSrv(socket);
        CloseSocket(socket);
        String metersData = PDM.getStringFromHex(resp);
        return metersData;
    }

    public static Byte sendGetMeterAddress(String portName) {
        Socket socket = GetNewSocket();
        SendToSrv(socket, "GetMeterAddress");
        SendToSrv(socket, PDM.getHexString(portName));
        String resp = GetRespFromSrv(socket);
        CloseSocket(socket);
        if (resp.equals("null")) {
            return null;
        } else {
            return Byte.valueOf(resp);
        }
    }

    public static String sendGetMeterSN(String portName) {
        Socket socket = GetNewSocket();
        SendToSrv(socket, "GetMeterSN");
        SendToSrv(socket, PDM.getHexString(portName));
        String resp = GetRespFromSrv(socket);
        CloseSocket(socket);
        if (resp.equals("null")) {
            return null;
        } else {
            return resp;
        }
    }

    public static String sendGetMeterInfo(int idInDB) {
        Socket socket = GetNewSocket();
        SendToSrv(socket, "GetMeterInfo");
        SendToSrv(socket, String.valueOf(idInDB));
        String resp = PDM.getStringFromHex(GetRespFromSrv(socket));
        CloseSocket(socket);
        return resp;
    }

    public static String sendGetMeterFlags(int idInDB) {
        Socket socket = GetNewSocket();
        SendToSrv(socket, "GetMeterFlags");
        SendToSrv(socket, String.valueOf(idInDB));
        String resp = PDM.getStringFromHex(GetRespFromSrv(socket));
        CloseSocket(socket);
        return resp;
    }

    public static void sendSetMeterFlags(int idInDB, String flags) {
        Socket socket = GetNewSocket();
        SendToSrv(socket, "SetMeterFlags");
        SendToSrv(socket, String.valueOf(idInDB));
        SendToSrv(socket, PDM.getHexString(flags));
        CloseSocket(socket);
    }

    public static void sendDeleteMeterFromDB(int idInDB) {
        Socket socket = GetNewSocket();
        SendToSrv(socket, "deleteMeterFromDB");
        SendToSrv(socket, String.valueOf(idInDB));
        CloseSocket(socket);
    }

    public static void sendUpdateMeterInDB(int idInDB, String sql) {
        Socket socket = GetNewSocket();
        SendToSrv(socket, "UpdateMeterInDB");
        SendToSrv(socket, String.valueOf(idInDB));
        SendToSrv(socket, PDM.getHexString(sql));
    }

    public static String[] sendGetSerialPortNames() {
        Socket socket = GetNewSocket();
        SendToSrv(socket, "GetSerialPortNames");
        String resp = PDM.getStringFromHex(GetRespFromSrv(socket));
        CloseSocket(socket);
        return resp.split("\n");
    }

    public static IntString[] sendGetMeterGroupNames() {
        Socket socket = GetNewSocket();
        SendToSrv(socket, "GetMeterGroupNames");
        String resp = PDM.getStringFromHex(GetRespFromSrv(socket));
        CloseSocket(socket);

        String[] respiss = resp.split("\n");
        IntString[] iss = new IntString[respiss.length];

        for (int i = 0; i < iss.length; i++) {
            String[] respis = respiss[i].split("\001");
            iss[i] = new IntString(Integer.valueOf(respis[0]), PDM.getStringFromHex(respis[1]));
        }

        return iss;
    }

    public static int sendNonQuerySQL(String sql, boolean ai) {
        Socket socket = GetNewSocket();
        if (ai) {
            SendToSrv(socket, "NonQuerySQLai");
            SendToSrv(socket, PDM.getHexString(sql));
            return Integer.valueOf(GetRespFromSrv(socket));
        } else {
            SendToSrv(socket, "NonQuerySQL");
            SendToSrv(socket, PDM.getHexString(sql));
            CloseSocket(socket);
            return 0;
        }
    }

    public static String sendGetLog(String filter) {
        Socket socket = GetNewSocket();
        SendToSrv(socket, "GetLog");
        SendToSrv(socket, PDM.getHexString(filter));
        String resphs = GetRespFromSrvBig(socket);
        String resp = PDM.getStringFromHex(resphs);
        if (resp.contains("HEX parsing error")) {
            System.out.println("HEX parsing error!!!");
        }
        return resp;
    }
}
