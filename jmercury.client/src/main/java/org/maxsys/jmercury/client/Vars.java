package org.maxsys.jmercury.client;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Vars {

    public static String Version = "JMercury Client 0.14-00 alpha";
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

    public static String AddNullsInString(String string) {
        String s1 = string + " ";
        StringBuilder sb1 = new StringBuilder();
        StringBuilder sbi1 = new StringBuilder();
        for (char c : s1.toCharArray()) {
            if ((int) c > 47 && (int) c < 58) {
                sbi1.append(c);
            } else {
                if (sbi1.length() == 0) {
                    sb1.append(c);
                } else {
                    if (sbi1.length() == 1) {
                        sb1.append("0").append(sbi1.toString()).append(c);
                        sbi1 = new StringBuilder();
                    } else {
                        sb1.append(sbi1.toString()).append(c);
                        sbi1 = new StringBuilder();
                    }
                }
            }
        }
        return sb1.toString().trim();
    }
}
