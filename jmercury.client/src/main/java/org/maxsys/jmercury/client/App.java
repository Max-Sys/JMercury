package org.maxsys.jmercury.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App {

    public static void main(String[] args) {

        // Logger
        String nowTimeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(Calendar.getInstance().getTime());
        try {
            Logger.getLogger("").addHandler(new FileHandler(nowTimeStamp + ".xml"));
        } catch (IOException | SecurityException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
//        try {
//            System.setOut(new PrintStream(new File(nowTimeStamp + ".log")));
//        } catch (FileNotFoundException ex) {
//            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        System.out.println(Vars.Version + " started.");

        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(App.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        // Initialisation and Properties
        Vars.PropPath = System.getProperty("user.home") + "/.JMercuryClient";
        Vars.PropFileName = Vars.PropPath + "/Options.xml";
        File dir = new File(Vars.PropPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        Vars.LoadProperties();
        
        MainFrame frm = new MainFrame();
        frm.setLocationRelativeTo(null);
        frm.setVisible(true);

//        Socket socket;
//        try {
//            socket = new Socket(args[0], 4545);
//
//            socket.getOutputStream().write("getStatus\000".getBytes());
//            try {
//                Thread.sleep(50);
//            } catch (InterruptedException ex) {
//                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
//            }
//
//            int ci;
//            String si = "";
//            while ((ci = socket.getInputStream().read()) >= 0 && ci != 0) {
//                si += (char) ci;
//            }
//            System.out.println(si.length());
//            String[] sss = si.split("\n");
//            System.out.println(sss.length);
//
//            System.out.println(si);
//
//            System.out.println("===");
//
//            for (String s : sss) {
//                System.out.println("-> " + s);
//            }
//
//            System.out.println("===");
//
//            socket.close();
//        } catch (IOException ex) {
//            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }
}
