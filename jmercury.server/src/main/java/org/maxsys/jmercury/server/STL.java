package org.maxsys.jmercury.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

public class STL {

    private static String fileTimeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(Calendar.getInstance().getTime());
    private static int LineCounter = 0;

    public synchronized static void Log(String logText) {
        if (LineCounter > 1000) {
            fileTimeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(Calendar.getInstance().getTime());
            LineCounter = 0;
        }

        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileTimeStamp + ".log", true), "UTF-8"));
        } catch (UnsupportedEncodingException | FileNotFoundException ex) {
            Logger.getLogger(STL.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (bw != null) {
            String nowTimeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Calendar.getInstance().getTime());
            try {
                bw.write(nowTimeStamp + ": " + logText);
                bw.newLine();
                bw.close();
            } catch (IOException ex) {
                Logger.getLogger(STL.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        LineCounter++;
    }

    public synchronized static String getLog(String filter) {
        StringBuilder log = new StringBuilder();
        try {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileTimeStamp + ".log"), "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (filter.startsWith("-")) {
                        if (!line.toLowerCase().contains(filter.substring(1).toLowerCase())) {
                            log.append(line);
                            log.append("\n");
                        }
                    } else {
                        if (line.toLowerCase().contains(filter.toLowerCase())) {
                            log.append(line);
                            log.append("\n");
                        }
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(STL.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(STL.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(STL.class.getName()).log(Level.SEVERE, null, ex);
        }
        return log.toString();
    }
}
