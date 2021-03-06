package org.maxsys.jmercury.client;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.maxsys.dblib.PDM;

public class NetClient {

    public static Socket GetNewSocket() {
        String srv = Vars.prop.getProperty("ServerAddr");
        if (srv == null) {
            return null;
        }
        int port = Integer.valueOf(Vars.prop.getProperty("ServerPort"));
        try {
            Socket newsocket = new Socket(srv, port);
            return newsocket;
        } catch (IOException ex) {
            Logger.getLogger(NetClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static void CloseSocket(Socket socket) {
        try {
            socket.close();
        } catch (NullPointerException | IOException ex) {
            Logger.getLogger(NetClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void SendToSrv(Socket socket, String data) {
        try {
            socket.getOutputStream().write((data + "\000").getBytes());
        } catch (IOException | NullPointerException ex) {
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
        } catch (IOException | NullPointerException ex) {
            return "";
        }
        return si.toString();
    }

    public static String GetRespFromSrvBig(Socket socket) {
        StringBuilder strbld = new StringBuilder();

        try {
            try (InputStream is = socket.getInputStream(); BufferedInputStream bis = new BufferedInputStream(is, 4096); InputStreamReader isr = new InputStreamReader(bis); BufferedReader br = new BufferedReader(isr)) {
                int r;
                while ((r = br.read()) > 0) {
                    strbld.append((char) r);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(NetClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        return strbld.toString();
    }

    public static String sendGetServerStatus() {
        Socket socket = GetNewSocket();
        SendToSrv(socket, "getStatus");
        String resp = GetRespFromSrv(socket);
        CloseSocket(socket);
        return resp;
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

    public static ArrayList<AvgAR> sendGetAvgArs(int IdInDB, Calendar caFrom, Calendar caTo) {
        StringBuilder params = new StringBuilder();
        params.append(IdInDB);
        params.append("\n");
        params.append(caFrom.getTimeInMillis());
        params.append("\n");
        params.append(caTo.getTimeInMillis());
        params.append("\n");

        Socket socket = GetNewSocket();
        SendToSrv(socket, "GetAvgArs");
        SendToSrv(socket, PDM.getHexString(params.toString()));
        String resp = PDM.getStringFromHex(GetRespFromSrvBig(socket));
        CloseSocket(socket);

        ArrayList<AvgAR> avgars = new ArrayList<>();
        if (!resp.isEmpty()) {
            String[] aarss = resp.split("\n");
            for (String aarstr : aarss) {
                String[] aarparams = aarstr.split("\001");
                double Aplus = Double.valueOf(aarparams[0]);
                double Aminus = Double.valueOf(aarparams[1]);
                double Rplus = Double.valueOf(aarparams[2]);
                double Rminus = Double.valueOf(aarparams[3]);
                Calendar arDT = new GregorianCalendar();
                long arDTlong = Long.valueOf(aarparams[4]);
                arDT.setTimeInMillis(arDTlong);
                int arP = Integer.valueOf(aarparams[5]);
                AvgAR avgar = new AvgAR(Aplus, Aminus, Rplus, Rminus, arDT, arP);
                avgars.add(avgar);
            }
        }

        return avgars;
    }

    public static ArrayList<AplusRplusD> sendGetApRpDays(int IdInDB) {
        Socket socket = GetNewSocket();
        SendToSrv(socket, "GetApRpDays");
        SendToSrv(socket, PDM.getHexString(String.valueOf(IdInDB)));
        String resp = PDM.getStringFromHex(GetRespFromSrvBig(socket));
        CloseSocket(socket);

        ArrayList<AplusRplusD> aprpds = new ArrayList<>();
        if (!resp.isEmpty()) {
            for (String aprpdstr : resp.split("\n")) {
                double Aplus = Double.valueOf(aprpdstr.split("\001")[0]);
                double Rplus = Double.valueOf(aprpdstr.split("\001")[1]);
                Calendar dayDT = new GregorianCalendar();
                long dayDTlong = Long.valueOf(aprpdstr.split("\001")[2]);
                dayDT.setTimeInMillis(dayDTlong);
                AplusRplusD aprpd = new AplusRplusD(dayDT, Aplus, Rplus, 0, 0);
                aprpds.add(aprpd);
            }
        }

        return aprpds;
    }

    public static ArrayList<AplusRplusD> sendGetApRpDays(int IdInDB, Calendar caFrom, Calendar caTo) {
        StringBuilder params = new StringBuilder();
        params.append(IdInDB);
        params.append("\n");
        params.append(caFrom.getTimeInMillis());
        params.append("\n");
        params.append(caTo.getTimeInMillis());
        params.append("\n");

        Socket socket = GetNewSocket();
        SendToSrv(socket, "GetApRpDaysFT");
        SendToSrv(socket, PDM.getHexString(params.toString()));
        String resp = PDM.getStringFromHex(GetRespFromSrvBig(socket));
        CloseSocket(socket);

        ArrayList<AplusRplusD> aprpds = new ArrayList<>();
        if (!resp.isEmpty()) {
            for (String aprpdstr : resp.split("\n")) {
                double Aplus = Double.valueOf(aprpdstr.split("\001")[0]);
                double Rplus = Double.valueOf(aprpdstr.split("\001")[1]);
                double AplusOnBeg = Double.valueOf(aprpdstr.split("\001")[3]);
                double RplusOnBeg = Double.valueOf(aprpdstr.split("\001")[4]);
                Calendar dayDT = new GregorianCalendar();
                long dayDTlong = Long.valueOf(aprpdstr.split("\001")[2]);
                dayDT.setTimeInMillis(dayDTlong);
                AplusRplusD aprpd = new AplusRplusD(dayDT, Aplus, Rplus, AplusOnBeg, RplusOnBeg);
                aprpds.add(aprpd);
            }
        }

        return aprpds;
    }

    public static ArrayList<AplusRplusM> sendGetApRpMonths(int IdInDB) {
        Socket socket = GetNewSocket();
        SendToSrv(socket, "GetApRpMonths");
        SendToSrv(socket, PDM.getHexString(String.valueOf(IdInDB)));
        String resp = PDM.getStringFromHex(GetRespFromSrvBig(socket));
        CloseSocket(socket);

        ArrayList<AplusRplusM> aprpds = new ArrayList<>();
        if (!resp.isEmpty()) {
            for (String aprpdstr : resp.split("\n")) {
                double Aplus = Double.valueOf(aprpdstr.split("\001")[0]);
                double Rplus = Double.valueOf(aprpdstr.split("\001")[1]);
                double AplusOnBeg = Double.valueOf(aprpdstr.split("\001")[2]);
                double RplusOnBeg = Double.valueOf(aprpdstr.split("\001")[3]);
                double AplusOnEnd = Double.valueOf(aprpdstr.split("\001")[4]);
                double RplusOnEnd = Double.valueOf(aprpdstr.split("\001")[5]);
                Calendar dayDT = new GregorianCalendar();
                long dayDTlong = Long.valueOf(aprpdstr.split("\001")[6]);
                dayDT.setTimeInMillis(dayDTlong);
                AplusRplusM aprpm = new AplusRplusM(dayDT, Aplus, Rplus, AplusOnBeg, RplusOnBeg, AplusOnEnd, RplusOnEnd);
                aprpds.add(aprpm);
            }
        }

        return aprpds;
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

    public static String sendGetLog(String filter) {
        Socket socket = GetNewSocket();
        SendToSrv(socket, "GetLog");
        SendToSrv(socket, PDM.getHexString(filter));
        String resp = PDM.getStringFromHex(GetRespFromSrvBig(socket));
        CloseSocket(socket);
        return resp;
    }

    public static Calendar[] sendGetMinMaxMonth() {
        Socket socket = GetNewSocket();
        SendToSrv(socket, "GetMinMaxMonth");
        String resp = PDM.getStringFromHex(GetRespFromSrv(socket));
        CloseSocket(socket);
        if (resp.isEmpty()) {
            return null;
        }
        String[] longs = resp.split("\n");
        if (longs.length != 2) {
            return null;
        }

        Calendar ca0 = new GregorianCalendar();
        ca0.setTimeInMillis(Long.valueOf(longs[0]));
        Calendar ca1 = new GregorianCalendar();
        ca1.setTimeInMillis(Long.valueOf(longs[1]));

        Calendar[] cas = new Calendar[2];
        cas[0] = ca0;
        cas[1] = ca1;

        return cas;
    }

    public static ForReport[] sendGetForReport(int Year, int Month) {
        Socket socket = GetNewSocket();
        SendToSrv(socket, "GetForReport");
        SendToSrv(socket, String.valueOf(Year));
        SendToSrv(socket, String.valueOf(Month));
        String resp = PDM.getStringFromHex(GetRespFromSrv(socket));
        CloseSocket(socket);

        if (resp == null || resp.isEmpty()) {
            return null;
        }

        String[] resps = resp.split("\n");
        if (resps.length == 0) {
            return null;
        }

        ForReport[] frs = new ForReport[resps.length];
        int frsk = 0;
        for (String frss : resps) {
            String[] frssf = frss.split("\001");
            String GroupName = frssf[0];
            String MeterName = frssf[1];
            String MeterSN = frssf[2];
            String Aplus1 = frssf[3];
            String Aplus2 = frssf[4];
            String Aplus21 = frssf[5];
            String MeterKi = frssf[6];
            String Aplus21Ki = frssf[7];
            frs[frsk] = new ForReport(GroupName, MeterName, MeterSN, Aplus1, Aplus2, Aplus21, MeterKi, Aplus21Ki, "0");
            frsk++;
        }

        DecimalFormat df = new DecimalFormat("#.##");
        for (ForReport fr : frs) {
            double AplusGroupSum = 0;
            for (ForReport subfr : frs) {
                if (fr.getGroupName().equals(subfr.getGroupName())) {
                    double Aplus21Ki = Double.valueOf(subfr.getAplus21Ki().replace(',', '.'));
                    AplusGroupSum += Aplus21Ki;
                }
            }
            fr.setAplusGroupSum(df.format(AplusGroupSum));
        }

        return frs;
    }

    public static int sendGetUpdateNumber() {
        Socket socket = GetNewSocket();
        SendToSrv(socket, "GetUpdateNumber");
        String resp = GetRespFromSrv(socket);
        CloseSocket(socket);
        return Integer.valueOf(resp);
    }

    public static boolean sendAddMarker(int idInDB, String name, Calendar when) {
        StringBuilder params = new StringBuilder();
        params.append(String.valueOf(idInDB)).append("\n");
        params.append(name).append("\n");
        params.append(String.valueOf(when.getTimeInMillis())).append("\n");

        Socket socket = GetNewSocket();
        SendToSrv(socket, "AddMarker");
        SendToSrv(socket, PDM.getHexString(params.toString()));
        String resp = GetRespFromSrv(socket);
        CloseSocket(socket);

        return resp.equals("ok");
    }

    public static String[] sendGetMarkerTasks(int idInDB) {
        String params = String.valueOf(idInDB);
        Socket socket = GetNewSocket();
        SendToSrv(socket, "GetMarkerTasks");
        SendToSrv(socket, PDM.getHexString(params));

        String[] retstr = new String[2];
        retstr[0] = PDM.getStringFromHex(GetRespFromSrv(socket));
        retstr[1] = PDM.getStringFromHex(GetRespFromSrv(socket));

        CloseSocket(socket);

        return retstr;
    }

    public static void sendRemoveMarker(int kInDB) {
        String params = String.valueOf(kInDB);
        Socket socket = GetNewSocket();
        SendToSrv(socket, "RemoveMarker");
        SendToSrv(socket, params);
        GetRespFromSrv(socket);
        CloseSocket(socket);
    }

    public static void sendRenameGroup(int kInDB, String name) {
        StringBuilder params = new StringBuilder();
        params.append(String.valueOf(kInDB)).append("\001");
        params.append(name);
        Socket socket = GetNewSocket();
        SendToSrv(socket, "RenameGroup");
        SendToSrv(socket, PDM.getHexString(params.toString()));
        GetRespFromSrv(socket);
        CloseSocket(socket);
    }

    public static double sendGetAplusSumFromAvgArs(int IdInDB, Calendar caFrom, Calendar caTo) {
        StringBuilder params = new StringBuilder();
        params.append(IdInDB);
        params.append("\n");
        params.append(caFrom.getTimeInMillis());
        params.append("\n");
        params.append(caTo.getTimeInMillis());
        params.append("\n");

        Socket socket = GetNewSocket();
        SendToSrv(socket, "GetAplusSumFromAvgArs");
        SendToSrv(socket, PDM.getHexString(params.toString()));
        String resp = PDM.getStringFromHex(GetRespFromSrvBig(socket));
        CloseSocket(socket);

        if (resp != null && !resp.isEmpty()) {
            return Double.valueOf(resp);
        } else {
            return 0;
        }
    }

    public static ForReport[] sendGetForReportDays(int Year, int Month, int Day) {
        Socket socket = GetNewSocket();
        SendToSrv(socket, "GetForReportDays");
        SendToSrv(socket, String.valueOf(Year));
        SendToSrv(socket, String.valueOf(Month));
        SendToSrv(socket, String.valueOf(Day));
        String resp = PDM.getStringFromHex(GetRespFromSrv(socket));
        CloseSocket(socket);

        if (resp == null || resp.isEmpty()) {
            return null;
        }

        String[] resps = resp.split("\n");
        if (resps.length == 0) {
            return null;
        }

        ForReport[] frs = new ForReport[resps.length];
        int frsk = 0;
        for (String frss : resps) {
            String[] frssf = frss.split("\001");
            String GroupName = frssf[0];
            String MeterName = frssf[1];
            String MeterSN = frssf[2];
            String Aplus1 = frssf[3];
            String Aplus2 = frssf[4];
            String Aplus21 = frssf[5];
            String MeterKi = frssf[6];
            String Aplus21Ki = frssf[7];
            frs[frsk] = new ForReport(GroupName, MeterName, MeterSN, Aplus1, Aplus2, Aplus21, MeterKi, Aplus21Ki, "0");
            frsk++;
        }

        DecimalFormat df = new DecimalFormat("#.##");
        for (ForReport fr : frs) {
            double AplusGroupSum = 0;
            for (ForReport subfr : frs) {
                if (fr.getGroupName().equals(subfr.getGroupName())) {
                    double Aplus21Ki = Double.valueOf(subfr.getAplus21Ki().replace(',', '.'));
                    AplusGroupSum += Aplus21Ki;
                }
            }
            fr.setAplusGroupSum(df.format(AplusGroupSum));
        }

        return frs;
    }

    static ForReport[] sendGetForReportPeriod(int year0, int month0, int day0, int year, int month, int day) {
        Socket socket = GetNewSocket();
        SendToSrv(socket, "GetForReportPeriod");
        SendToSrv(socket, String.valueOf(year0));
        SendToSrv(socket, String.valueOf(month0));
        SendToSrv(socket, String.valueOf(day0));
        SendToSrv(socket, String.valueOf(year));
        SendToSrv(socket, String.valueOf(month));
        SendToSrv(socket, String.valueOf(day));
        String resp = PDM.getStringFromHex(GetRespFromSrv(socket));
        CloseSocket(socket);

        if (resp == null || resp.isEmpty()) {
            return null;
        }

        String[] resps = resp.split("\n");
        if (resps.length == 0) {
            return null;
        }

        ForReport[] frs = new ForReport[resps.length];
        int frsk = 0;
        for (String frss : resps) {
            String[] frssf = frss.split("\001");
            String GroupName = frssf[0];
            String MeterName = frssf[1];
            String MeterSN = frssf[2];
            String Aplus1 = frssf[3];
            String Aplus2 = frssf[4];
            String Aplus21 = frssf[5];
            String MeterKi = frssf[6];
            String Aplus21Ki = frssf[7];
            frs[frsk] = new ForReport(GroupName, MeterName, MeterSN, Aplus1, Aplus2, Aplus21, MeterKi, Aplus21Ki, "0");
            frsk++;
        }

        DecimalFormat df = new DecimalFormat("#.##");
        for (ForReport fr : frs) {
            double AplusGroupSum = 0;
            for (ForReport subfr : frs) {
                if (fr.getGroupName().equals(subfr.getGroupName())) {
                    double Aplus21Ki = Double.valueOf(subfr.getAplus21Ki().replace(',', '.'));
                    AplusGroupSum += Aplus21Ki;
                }
            }
            fr.setAplusGroupSum(df.format(AplusGroupSum));
        }

        return frs;
    }
}
