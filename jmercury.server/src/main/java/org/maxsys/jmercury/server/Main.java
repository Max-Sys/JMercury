package org.maxsys.jmercury.server;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) {
        // Logger
        try {
            Logger.getLogger("").addHandler(new FileHandler("errorlog.xml"));
        } catch (IOException | SecurityException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        // Initialisation and Properties
        Vars.PropPath = System.getProperty("user.home") + "/.JMercuryServer";
        Vars.PropFileName = Vars.PropPath + "/Options.xml";
        File dir = new File(Vars.PropPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        if (args.length > 0) {

            if (args[0].equals("--help")) {
                System.out.println("using:");
                System.out.println("jmercury.server --clearconfig");
                System.out.println("jmercury.server --configure Servername jdbc:mysql://address:port/dbname Username Userpass");
                System.out.println("jmercury.server --remote ServerAddress [Stop]");
                return;
            }

            if (args[0].equals("--clearconfig")) {
                System.out.println("Clearing config...");
                File file = new File(Vars.PropFileName);
                file.delete();
                return;
            }

            if (args[0].equals("--configure")) {
                if (args.length != 5) {
                    System.out.println("using:");
                    System.out.println("jmercury.server --configure Servername jdbc:mysql://address:port/dbname Username Userpass");
                    return;
                }
                Vars.prop.setProperty("Servername", args[1]);
                Vars.prop.setProperty("DB_URL", args[2]);
                Vars.prop.setProperty("Username", args[3]);
                Vars.prop.setProperty("Userpass", args[4]);
                Vars.SaveProperties();
            }

            if (args[0].equals("--remote")) {
                if (args.length == 2) {
                    Vars.SrvAddr = args[1];
                    System.out.println("Connecting to remote server: " + Vars.SrvAddr + "...");
                    Socket socket = NetServer.GetNewSocket();
                    NetServer.SendToSrv(socket, "getStatus");
                    String resp = NetServer.GetRespFromSrv(socket);
                    NetServer.CloseSocket(socket);
                    if (resp == null || resp.length() == 0) {
                        return;
                    }
                    String[] resps = resp.split("\n");
                    for (String s : resps) {
                        System.out.println(s);
                    }
                }
                if (args.length == 3 && args[2].toLowerCase().equals("stop")) {
                    Vars.SrvAddr = args[1];
                    System.out.println("Trying to stop remote server: " + Vars.SrvAddr + "...");
                    Socket socket = NetServer.GetNewSocket();
                    NetServer.SendToSrv(socket, "StopServer");
                    NetServer.CloseSocket(socket);
                    System.out.println("Ok");
                    return;
                }
            }
        }

        if (!Vars.LoadProperties()) {
            System.out.println("This server is not configured properly!");
            System.out.println("Use --configure switch.");
            return;
        }

        if (Vars.SrvAddr.equals("localhost")) {
            Thread nsrvt = new Thread(new NetServer());
            nsrvt.start();
        }

        if (java.awt.GraphicsEnvironment.isHeadless()) {
            System.out.println("Starting console mode...");
        } else {
            MainFrame frm = new MainFrame();
            frm.setLocationRelativeTo(null);
            frm.setVisible(true);
        }
    }

}
