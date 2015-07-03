package org.maxsys.jmercury.server;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.maxsys.dblib.PDM;

public class Vars {

    public static String Version = "JMercury Server 0.10-00";
    public static Integer serverID = -1;
    public static Properties prop = new Properties();
    public static String PropFileName = "";
    public static String PropPath = "";
    public static String SrvAddr = "localhost";
    public static boolean isConsole = false;
    public static HashMap<Integer, EMeter> meters = new HashMap<>();

    public static void SaveProperties() {
        try {
            prop.storeToXML(new FileOutputStream(PropFileName, false), null);
        } catch (IOException ex) {
            Logger.getLogger(Vars.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static boolean LoadProperties() {
        try {
            Vars.prop.loadFromXML(new FileInputStream(PropFileName));
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    public static void SaveMeterState(EMeter meter) {
        PDM pdm = new PDM();
        pdm.executeNonQueryUpdate("em", "UPDATE meters SET flags = '" + meter.getMeterFlags() + "' WHERE k = " + meter.getIdInDB());
    }
}
