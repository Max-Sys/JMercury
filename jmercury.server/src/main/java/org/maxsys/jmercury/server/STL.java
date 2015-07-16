package org.maxsys.jmercury.server;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

public class STL {

    private static BufferedWriter bw = null;

    public STL() {
        String nowTimeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(Calendar.getInstance().getTime());
        try {
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(nowTimeStamp + ".log"), "UTF-8"));
        } catch (UnsupportedEncodingException | FileNotFoundException ex) {
            Logger.getLogger(STL.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void Log(String logText) {
        if (bw != null) {
            String nowTimeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Calendar.getInstance().getTime());
            try {
                bw.write(nowTimeStamp + ": " + logText);
                bw.newLine();
                bw.flush();
            } catch (IOException ex) {
                Logger.getLogger(STL.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void Close() {
        if (bw != null) {
            try {
                bw.close();
            } catch (IOException ex) {
                Logger.getLogger(STL.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
