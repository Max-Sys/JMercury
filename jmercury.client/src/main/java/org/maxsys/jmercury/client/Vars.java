package org.maxsys.jmercury.client;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Vars {

    public static String Version = "JMercury Client 0.10-00 alpha";
    public static Properties prop = new Properties();
    public static String PropFileName = "";
    public static String PropPath = "";

    public static void SaveProperties() {
        try {
            prop.storeToXML(new FileOutputStream(PropFileName, false), null);
        } catch (IOException ex) {
            Logger.getLogger(Vars.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void LoadProperties() {
        try {
            Vars.prop.loadFromXML(new FileInputStream(PropFileName));
        } catch (IOException ex) {
            Logger.getLogger(Vars.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
