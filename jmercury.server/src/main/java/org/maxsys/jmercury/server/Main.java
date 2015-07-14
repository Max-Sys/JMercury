package org.maxsys.jmercury.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.maxsys.dblib.PDM;

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

                String servername = args[1];
                String dburl = args[2];
                String username = args[3];
                String password = args[4];

                PDM pdm = new PDM("em", username, password, dburl);

                Boolean isNewServer = true;
                ResultSet rs;
                try {
                    rs = pdm.getResultSet("em", "SELECT k, `name` FROM servers WHERE hide = 0;");
                    while (rs.next()) {
                        String srvn = PDM.getStringFromHex(rs.getString("name"));
                        if (srvn.equals(servername)) {
                            isNewServer = false;
                            Vars.serverID = rs.getInt("k");
                        }
                    }
                } catch (SQLException ex) {
                }
                pdm.closeResultSet();

                if (isNewServer) {
                    System.out.print("Server \"" + servername + "\" is not registred. Register now? [y|n]");
                    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                    String ans = "";
                    try {
                        ans = br.readLine();
                    } catch (IOException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    if (ans.toLowerCase().equals("y")) {
                        System.out.println("NEW!!!");
                    } else {
                        System.out.println("n");
                    }
                } else {
                    Vars.prop.setProperty("DB_URL", dburl);
                    Vars.prop.setProperty("Username", username);
                    Vars.prop.setProperty("Userpass", password);
                    Vars.prop.setProperty("Servername", servername);
                    Vars.SaveProperties();
                }
            }

            if (args[0].equals("--remote")) {
                Vars.isLocal = false;
                if (args.length < 2) {
                    System.out.println("using:");
                    System.out.println("jmercury.server --remote ServerAddress [Stop]");
                    return;
                }
                if (args.length == 2) {
                    Vars.SrvAddr = args[1];
                }
                if (args.length == 3 && args[2].toLowerCase().equals("stop")) {
                    Vars.SrvAddr = args[1];
                    System.out.println("Trying to stop remote server: " + Vars.SrvAddr + "...");
                    Socket socket;
                    try {
                        socket = new Socket(Vars.SrvAddr, 4545);
                    } catch (IOException ex) {
                        System.out.println("Error connecting to " + Vars.SrvAddr + ".");
                        return;
                    }
                    NetServer.SendToSrv(socket, "StopServer");
                    NetServer.CloseSocket(socket);
                    System.out.println("Ok");
                    return;
                }
            }
        }

        if (java.awt.GraphicsEnvironment.isHeadless()) {
            Vars.isConsole = true;
            System.out.println("Starting console mode...");
        }

        if (Vars.isLocal && !Vars.isConsole) {
            LocalNeLocal lnl = new LocalNeLocal(null, true);
            lnl.setLocationRelativeTo(null);
            lnl.setVisible(true);
        }

        if (!Vars.LoadProperties() && Vars.isLocal && Vars.isConsole) {
            System.out.println("This server is not configured properly!");
            System.out.println("Use --configure switch.");
            return;
        }

        // Database connection if local
        if (Vars.isLocal) {
            String servername = Vars.prop.getProperty("Servername");
            String dburl = Vars.prop.getProperty("DB_URL");
            String username = Vars.prop.getProperty("Username");
            String password = Vars.prop.getProperty("Userpass");

            if (servername == null || dburl == null || username == null || password == null) {
                ServerInstanceDialog dlg = new ServerInstanceDialog(null, true);
                dlg.setLocationRelativeTo(null);
                dlg.setVisible(true);
                servername = Vars.prop.getProperty("Servername");
                dburl = Vars.prop.getProperty("DB_URL");
                username = Vars.prop.getProperty("Username");
                password = Vars.prop.getProperty("Userpass");
                if (servername == null || dburl == null || username == null || password == null) {
                    return;
                }
            } else {
                PDM pdm = new PDM("em", username, password, dburl);

                Boolean isNewServer = true;
                ResultSet rs;
                try {
                    rs = pdm.getResultSet("em", "SELECT k, `name` FROM servers WHERE hide = 0;");
                    if (rs == null) {
                        System.out.println("Error connecting to database!");
                        return;
                    }
                    while (rs.next()) {
                        String srvn = PDM.getStringFromHex(rs.getString("name"));
                        if (srvn.equals(servername)) {
                            isNewServer = false;
                            Vars.serverID = rs.getInt("k");
                        }
                    }
                } catch (SQLException ex) {
                }
                pdm.closeResultSet();

                if (isNewServer || Vars.serverID == -1) {
                    System.out.println("This server is not configured properly!");
                    System.out.println("Use --configure switch.");
                    System.exit(-1);
                    return;
                }
            }
        } else {
            System.out.println("Connecting to remote server: " + Vars.SrvAddr + "...");
            Socket socket;
            try {
                socket = new Socket(Vars.SrvAddr, 4545);
            } catch (IOException ex) {
                System.out.println("Error connecting to " + Vars.SrvAddr + ".");
                return;
            }
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

        if (Vars.isConsole) {
            if (Vars.isLocal) {
                Thread nsrvt = new Thread(new NetServer());
                nsrvt.start();
            } else {
                System.out.println("Cannot connect to remote server in console mode! Closing...");
            }
        } else {
            if (Vars.isLocal) {
                Thread nsrvt = new Thread(new NetServer());
                nsrvt.start();
            }
            MainFrame frm = new MainFrame();
            frm.setLocationRelativeTo(null);
            frm.setVisible(true);
        }
    }
}
