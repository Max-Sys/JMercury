package org.maxsys.jmercury.server;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import org.maxsys.dblib.PDM;

public class MainFrame extends javax.swing.JFrame {

    private class TableWatcher implements Runnable {

        private boolean Running = true;
        private boolean Paused = false;

        @Override
        public void run() {
            Socket sock = NetClient.GetNewSocket();
            NetServer.SendToSrv(sock, "newMeterStatusChannel");
            while (this.Running) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                }

                if (this.Paused) {
                    continue;
                }

                NetServer.SendToSrv(sock, "get");
                String statusstr = NetServer.GetRespFromSrv(sock);
                String[] statuses = PDM.getStringFromHex(statusstr).split("\n");
                for (String status : statuses) {
                    String[] statusfields = status.split("\001");
                    int idInDB = Integer.valueOf(statusfields[0]);
                    String statusval = statusfields[1];
                    if (idInDB == -1) {
                        if (statusval.equals("MsvrRunning")) {
                            jButton2.setEnabled(false);
                            jButton4.setEnabled(true);
                            jButton5.setEnabled(false);
                            jButton6.setEnabled(false);
                            jButton7.setEnabled(false);
                            jButton9.setEnabled(false);
                            jButton11.setEnabled(false);
                        } else {
                            jButton2.setEnabled(true);
                            jButton4.setEnabled(false);
                            jButton5.setEnabled(true);
                            jButton6.setEnabled(true);
                            jButton7.setEnabled(true);
                            jButton9.setEnabled(true);
                            jButton11.setEnabled(true);
                        }
                    }
                    if (idInDB == -2) {
                        jLabel1.setText(statusval);
                    }
                    if (idInDB == -3) {
                        jLabel2.setText("Server time: " + statusval);
                    }
                    if (idInDB > -1) {
                        for (int r = 0; r < jTable1.getRowCount(); r++) {
                            IntString is = (IntString) jTable1.getValueAt(r, 0);
                            if (is.getInt() == idInDB) {
                                jTable1.setValueAt(statusval, r, 1);
                            }
                        }
                    }
                }
            }
            NetServer.SendToSrv(sock, "close");
        }

        public void Stop() {
            this.Running = false;
        }

        public boolean isPaused() {
            return Paused;
        }

//        public void Pause() {
//            this.Paused = true;
//            for (int r = 0; r < jTable1.getRowCount(); r++) {
//                jTable1.setValueAt("---", r, 1);
//            }
//        }
        public void Go() {
            this.Paused = false;
        }

    }
    private TableWatcher tableWatcher = new TableWatcher();
    private Thread TableWatcherT = new Thread(tableWatcher);

    public MainFrame() {
        initComponents();
        setMinimumSize(getSize());

        Image icon = new javax.swing.ImageIcon(getClass().getResource("/org/maxsys/jmercury/server/resources/icon_1_1.png")).getImage();
        setIconImage(icon);

        jTable1.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{"Meter", "Status"}) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });

        jTable1.getColumnModel().getColumn(0).setPreferredWidth(750);
        jTable1.getColumnModel().getColumn(1).setPreferredWidth(250);

        if (Vars.isLocal) {
            setTitle(Vars.Version + " - " + Vars.prop.getProperty("Servername") + " (local mode)");
        } else {
            Properties props = NetClient.sendGetServerProps();
            Vars.serverID = Integer.valueOf(props.getProperty("ServerID"));
            setTitle(Vars.Version + " - " + props.getProperty("Servername") + " (remote mode)");
            jButton1.setText("Exit / Shut down");
            menuItem1.setLabel("Exit / Shut down JMercury Server");
        }

        RefreshTable();

        if (!TableWatcherT.isAlive()) {
            TableWatcherT.start();
        }
    }

    private void RefreshTable() {
        DefaultTableModel tm = (DefaultTableModel) jTable1.getModel();
        while (tm.getRowCount() > 0) {
            tm.removeRow(0);
        }

        if (NetClient.sendRefreshMeters()) {
            String nsgmd = NetClient.sendGetMetersData();
            if (!nsgmd.isEmpty()) {
                String[] metersData = nsgmd.split("\n");

                for (String meterdata : metersData) {
                    String[] mds = meterdata.split("\001");
                    Object[] rowdata = new Object[2];
                    String intab = "<html><b>" + mds[1] + "</b>/" + mds[2] + ", "
                            + mds[3] + "/" + mds[4]
                            + ", Ki:" + mds[5]
                            + "</html>";
                    rowdata[0] = new IntString(Integer.valueOf(mds[0]), intab);
                    rowdata[1] = "---";

                    tm.addRow(rowdata);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        popupMenu1 = new java.awt.PopupMenu();
        menuItem1 = new java.awt.MenuItem();
        jButton1 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        popupMenu1.setLabel(Vars.Version);

        menuItem1.setLabel("Shut down JMercury Server");
        menuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItem1ActionPerformed(evt);
            }
        });
        popupMenu1.add(menuItem1);

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);

        jButton1.setText("Shut down");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Meters"));

        jTable1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable1MouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jTable1);

        jButton5.setText("Add");
        jButton5.setEnabled(false);
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jButton6.setText("Edit");
        jButton6.setEnabled(false);
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jButton7.setText("Remove");
        jButton7.setEnabled(false);
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jButton9.setText("Tasks");
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });

        jButton10.setText("Refresh");
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });

        jButton8.setText("Log");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        jButton11.setText("Reset timers");
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jButton5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 519, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton5)
                    .addComponent(jButton6)
                    .addComponent(jButton7)
                    .addComponent(jButton9)
                    .addComponent(jButton10)
                    .addComponent(jButton8)
                    .addComponent(jButton11))
                .addContainerGap())
        );

        jButton2.setText("Run");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton4.setText("Stop");
        jButton4.setEnabled(false);
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Server status"));

        jLabel1.setText("PDM:");

        jLabel2.setText("Server time:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton1))
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2)
                    .addComponent(jButton4))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        dispose();
        if (Vars.SrvAddr.equals("localhost")) {
            NetClient.sendStopServer();
        } else {
//            if (JOptionPane.showConfirmDialog(this, "Do you want to shut down remote server?", "Confirm shuting down server", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
//                NetClient.sendStopServer();
//            }
            System.exit(0);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void menuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItem1ActionPerformed
        dispose();
        if (Vars.SrvAddr.equals("localhost")) {
            NetClient.sendStopServer();
        } else {
            if (JOptionPane.showConfirmDialog(this, "Do you want to shut down remote server?", "Confirm shuting down server", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                NetClient.sendStopServer();
            }
            System.exit(0);
        }
    }//GEN-LAST:event_menuItem1ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        NewMeterDialog dlg = new NewMeterDialog(this, true);
        dlg.setLocationRelativeTo(null);
        dlg.setVisible(true);
        RefreshTable();
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        NetClient.sendMsvrRun();
        RefreshTable();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        NetClient.sendMsvrPause();
        RefreshTable();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        if (jTable1.getSelectedRow() == -1) {
            return;
        }

        String status = (String) jTable1.getValueAt(jTable1.getSelectedRow(), 1);
        if (status.equals("---")) {
            EditMeterDialog dlg = new EditMeterDialog(this, true, ((IntString) jTable1.getValueAt(jTable1.getSelectedRow(), 0)).getInt());
            dlg.setLocationRelativeTo(null);
            dlg.setVisible(true);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            }

            RefreshTable();
        }
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        if (jTable1.getSelectedRow() == -1) {
            return;
        }

        String status = (String) jTable1.getValueAt(jTable1.getSelectedRow(), 1);
        if (status.equals("---")) {
            int ki = ((IntString) jTable1.getValueAt(jTable1.getSelectedRow(), 0)).getInt();
            if (JOptionPane.showConfirmDialog(this, "Are you sure you want to remove this item?", "Confirm item remove", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                NetClient.sendDeleteMeterFromDB(ki);
                RefreshTable();
            }
        }
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseClicked
        if (Vars.isLocal && evt.getButton() == 1 && evt.getClickCount() == 2 && NetClient.sendIsMsrvPaused()) {
            if (jTable1.getSelectedRow() == -1) {
                return;
            }

            ViewMeterDialog dlg = new ViewMeterDialog(this, false, ((IntString) jTable1.getValueAt(jTable1.getSelectedRow(), 0)).getInt());
            dlg.setLocationRelativeTo(null);
            dlg.setVisible(true);
        }
    }//GEN-LAST:event_jTable1MouseClicked

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        if (jTable1.getSelectedRow() == -1) {
            return;
        }

        String status = (String) jTable1.getValueAt(jTable1.getSelectedRow(), 1);
        if (status.equals("---")) {
            TasksDialog dlg = new TasksDialog(this, true, ((IntString) jTable1.getValueAt(jTable1.getSelectedRow(), 0)).getInt());
            dlg.setLocationRelativeTo(null);
            dlg.setVisible(true);
        }
    }//GEN-LAST:event_jButton9ActionPerformed

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        RefreshTable();
    }//GEN-LAST:event_jButton10ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        LogViewDialog dlg = new LogViewDialog(this, true);
        dlg.setLocationRelativeTo(null);
        dlg.setVisible(true);
    }//GEN-LAST:event_jButton8ActionPerformed

    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
        boolean resetosv = false;
        if (JOptionPane.showConfirmDialog(this, "Do you want to reset OSV flags?", "", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
            resetosv = true;
        }

        for (int rc = 0; rc < jTable1.getRowCount(); rc++) {
            int k = ((IntString) jTable1.getValueAt(rc, 0)).getInt();
            String nflags = NetClient.sendGetMeterFlags(k);
            String[] flags = nflags.split("\n");

            HashMap<String, String> meterFlags = new HashMap<>();

            String[] vks = flags[1].split(";");
            for (String vk : vks) {
                String[] valkey = vk.split(":");
                if (valkey.length == 2) {
                    meterFlags.put(valkey[0], valkey[1]);
                }
            }

            long l1 = Long.valueOf(meterFlags.get("AvgARsTask_t"));
            long l2 = Long.valueOf(meterFlags.get("AvgARsTask_i"));
            l1 -= l2;
            meterFlags.put("AvgARsTask_t", String.valueOf(l1));

            l1 = Long.valueOf(meterFlags.get("DaysTask_t"));
            l2 = Long.valueOf(meterFlags.get("DaysTask_i"));
            l1 -= l2;
            meterFlags.put("DaysTask_t", String.valueOf(l1));

            l1 = Long.valueOf(meterFlags.get("MonthTask_t"));
            l2 = Long.valueOf(meterFlags.get("MonthTask_i"));
            l1 -= l2;
            meterFlags.put("MonthTask_t", String.valueOf(l1));

            if (resetosv && meterFlags.get("osv").equals("yes")) {
                meterFlags.put("osv", "no");
            }

            StringBuilder flagsString = new StringBuilder();
            for (Map.Entry<String, String> kvp : meterFlags.entrySet()) {
                flagsString.append(kvp.getKey()).append(":").append(kvp.getValue()).append(";");
            }

            NetClient.sendSetMeterFlags(k, flagsString.toString());
        }
    }//GEN-LAST:event_jButton11ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private java.awt.MenuItem menuItem1;
    private java.awt.PopupMenu popupMenu1;
    // End of variables declaration//GEN-END:variables
}
