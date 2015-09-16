package org.maxsys.jmercury.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Vars {

    public static String Version = "JMercury Server 0.96-04";
    public static Integer serverID = -1;
    public static Properties prop = new Properties();
    public static String PropFileName = "";
    public static String PropPath = "";
    public static String SrvAddr = "localhost";
    public static boolean isLocal = true;
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
        } catch (IOException ex) {
            return false;
        }
        if (!Vars.isLocal) {
            Vars.prop.remove("Username");
            Vars.prop.remove("Userpass");
            Vars.prop.remove("Servername");
            Vars.prop.remove("DB_URL");
        }
        return true;
    }

    public static String[] getNixPortNames() {
        File devdir = new File("/dev");
        String[] devs = devdir.list(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.matches("tty[a-zA-Z]+[0-9]+[a-zA-Z]*");
            }
        });

        for (int devn = 0; devn < devs.length; devn++) {
            devs[devn] = "/dev/" + devs[devn];
        }

        return devs;
    }
}
