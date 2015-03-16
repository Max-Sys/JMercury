package org.maxsys.jmercury.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NetServer implements Runnable {

    private boolean isCancelled = false;
    private final HashMap<Integer, Socket> sockets = new HashMap<>();
    private int LastOpenedSocket = 0;

    private void StopNetServer() {
        isCancelled = true;
    }

    @Override
    public void run() {
        System.out.println("NetServer is running!");

        ServerSocket ssocket = null;

        try {
            ssocket = new ServerSocket(4545);
        } catch (IOException ex) {
            Logger.getLogger(NetServer.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (ssocket == null) {
            return;
        }

        while (!isCancelled) {
            Socket socket = null;
            try {
                socket = ssocket.accept();
            } catch (IOException ex) {
                Logger.getLogger(NetServer.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (socket == null) {
                continue;
            }

            sockets.put(LastOpenedSocket, socket);

            Thread ssrv;
            ssrv = new Thread(new Runnable() {

                private final int mySocket = LastOpenedSocket;

                @Override
                public void run() {
                    System.out.println("socket " + mySocket + " = " + sockets.get(mySocket));
                    Socket sock = sockets.get(mySocket);

                    while (!sock.isClosed()) {

                        // Read command
                        String cmd = "";
                        try {
                            int ci;
                            while ((ci = sock.getInputStream().read()) != -1 && ci != 0 && (char) ci != '!') {
                                cmd += (char) ci;
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(NetServer.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        if (cmd.isEmpty()) {
                            try {
                                sock.close();
                            } catch (IOException ex) {
                                Logger.getLogger(NetServer.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        } else {
                            System.out.println("socket " + mySocket + ", cmd: " + cmd);
                        }

                        // Commands processing
                        /*
                         Команды сервера:
                         getStatus - возвращает информацию о работе сервера.
                        
                        
                        
                         */
                        if (cmd.equals("StopServer")) {
                            System.out.println("socket " + mySocket + " закрываем сервер.");
                            StopNetServer();
                            System.exit(0);
                            break;
                        }
                        if (cmd.equals("getStatus")) {
                            String status = Vars.Version + "\n";
                            status += "Server name: " + Vars.prop.getProperty("Servername") + "\n";
                            status += "Meters registred: " + Vars.meters.size();
                            status += "\000";
                            try {
                                sock.getOutputStream().write(status.getBytes());
                            } catch (IOException ex) {
                                Logger.getLogger(NetServer.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }

                    }
                    sockets.remove(mySocket);
                }
            });
            ssrv.start();

            LastOpenedSocket++;
        }

        System.out.println("NetServer is closed!");
    }
}
