package org.maxsys.jmercury.client;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.apache.commons.io.FileUtils;

public class App {

    public static void main(String[] args) {
        // After update
        if (System.getProperty("user.dir").endsWith("update")) {
            String wp = System.getProperty("user.dir");
            File dep = new File(wp + "/dependency");
            File depto = new File(wp.substring(0, wp.length() - 7) + "/dependency");

            if (depto.exists()) {
                try {
                    FileUtils.deleteDirectory(depto);
                } catch (IOException ex) {
                    Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            try {
                Files.copy(dep.toPath(), depto.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            }

            for (String depfilen : dep.list()) {
                try {
                    Files.copy(dep.toPath().resolve(depfilen), depto.toPath().resolve(depfilen), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ex) {
                    Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            URI myuri = null;
            try {
                myuri = App.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            } catch (URISyntaxException ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            }
            File me = new File(myuri);
            File meto = new File(wp.substring(0, wp.length() - 7) + "/" + me.getName());
            if (meto.exists()) {
                try {
                    Files.delete(meto.toPath());
                } catch (IOException ex) {
                    Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            try {
                Files.copy(me.toPath(), meto.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                Runtime.getRuntime().exec("java -jar " + meto.getPath(), null, new File(wp.substring(0, wp.length() - 7)));
            } catch (IOException ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            }

            return;
        }

        File upfolder = new File("update");
        if (upfolder.exists()) {
            try {
                FileUtils.deleteDirectory(upfolder);
            } catch (IOException ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

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

            //Check update
            String myunstr = Vars.prop.getProperty("MyUN") == null ? "0" : Vars.prop.getProperty("MyUN");
            int myun = Integer.valueOf(myunstr);
            int updateNumber = NetClient.sendGetUpdateNumber();

            if (updateNumber > myun) {
                javax.swing.JOptionPane.showMessageDialog(null, "Появилась новая версия программы. Загляните в \"Сервис -> Обновление программы...\"");
            }

            MainFrame frm = new MainFrame();
            frm.setLocationRelativeTo(null);
            frm.setVisible(true);
        }
    }
}
