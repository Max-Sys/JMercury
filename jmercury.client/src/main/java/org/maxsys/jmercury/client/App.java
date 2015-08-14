package org.maxsys.jmercury.client;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

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

        if (Vars.prop.getProperty("ServerAddr") == null || Vars.prop.getProperty("ServerPort") == null) {
            OptionsDialog dlg = new OptionsDialog(null, true);
            dlg.setLocationRelativeTo(null);
            dlg.setVisible(true);
        }

        String status = NetClient.sendGetServerStatus();
        if (status.isEmpty()) {
            if (JOptionPane.showConfirmDialog(null, "Невозможно подключиться к серверу! Удалить сервер из настроек?", "Ошибка", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE) == JOptionPane.YES_OPTION) {
                Vars.prop.remove("ServerAddr");
                Vars.prop.remove("ServerPort");
                Vars.SaveProperties();
            }
        } else {
            System.out.println("Подключаемся:");
            System.out.println(status);
            MainFrame frm = new MainFrame();
            frm.setLocationRelativeTo(null);
            frm.setVisible(true);
        }
    }
}
