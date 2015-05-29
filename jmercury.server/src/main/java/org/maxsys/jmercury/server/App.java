package org.maxsys.jmercury.server;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import org.maxsys.dblib.PDM;

public class App extends javax.swing.JFrame {

    Thread msrvt;
    MeterServer msrv;
    Thread nsrvt;
    NetServer nsrv;

    public App() {
        initComponents();
        setMinimumSize(getSize());

        Image icon = new javax.swing.ImageIcon(getClass().getResource("/org/maxsys/jmercury/server/resources/icon_1_1.png")).getImage();
        setIconImage(icon);
        setTitle(Vars.Version);

        TrayIcon trayIcon = new TrayIcon(icon);
        trayIcon.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (isVisible()) {
                        setVisible(false);
                    } else {
                        setVisible(true);
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });
        trayIcon.setPopupMenu(popupMenu1);
        try {
            SystemTray.getSystemTray().add(trayIcon);
        } catch (AWTException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }

        jTable1.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{"Meter", "Status"}) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                });

        jTable1.getColumnModel().getColumn(0).setPreferredWidth(700);
        jTable1.getColumnModel().getColumn(1).setPreferredWidth(300);

        jTextField1.setText(Vars.prop.getProperty("DB_URL"));
        jTextField2.setText(Vars.prop.getProperty("Username"));
        jPasswordField2.setText(Vars.prop.getProperty("Userpass"));
        jTextField3.setText(Vars.prop.getProperty("Servername"));

        nsrv = new NetServer();
        nsrvt = new Thread(nsrv);

        msrv = new MeterServer(jTable1);
        msrvt = new Thread(msrv);

        SaveAndRun();
        SrvStop();
    }

    private Boolean SaveAndConnect() {
        String dburl = jTextField1.getText();
        String username = jTextField2.getText().trim();
        String password = String.valueOf(jPasswordField2.getPassword());
        String servername = jTextField3.getText().trim();

        Vars.serverID = -1;

        if (servername.length() == 0) {
            return false;
        }
        
        PDM pdm = new PDM("em", username, password, dburl);

        Boolean isNewServer = true;
        ResultSet rs = null;
        try {
            rs = pdm.getResultSet("em", "SELECT k, `name` FROM servers WHERE hide = 0;");
            while (rs.next()) {
                String srvn = PDM.getStringFromHex(rs.getString("name"));
                if (srvn.equals(servername)) {
                    isNewServer = false;
                    Vars.serverID = rs.getInt("k");
                }
            }
            pdm.closeResultSet();
        } catch (Exception e) {
        }
        if (rs == null) {
            return false;
        }

        if (isNewServer) {
            if (JOptionPane.showConfirmDialog(this, "Add new server instance? (" + servername + ")", "Server configuration", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                Vars.serverID = pdm.executeNonQueryAI("em", "INSERT INTO servers (`name`, hide) VALUES ('" + PDM.getHexString(servername) + "', 0)");
            } else {
                return false;
            }
        }

        if (Vars.serverID == -1) {
            return false;
        }

        Vars.prop.setProperty("DB_URL", dburl);
        Vars.prop.setProperty("Username", username);
        Vars.prop.setProperty("Userpass", password);
        Vars.prop.setProperty("Servername", servername);
        Vars.SaveProperties();

        return true;
    }

    private void RefreshTable() {
        DefaultTableModel tm = (DefaultTableModel) jTable1.getModel();
        while (tm.getRowCount() > 0) {
            tm.removeRow(0);
        }

        Vars.meters.clear();

        if (Vars.serverID == -1) {
            return;
        }

        PDM pdm = new PDM();
        ResultSet rs = pdm.getResultSet("em", "SELECT meters.k, meters.`name`, meters.group_id, meters.comport, meters.rsaddr, meters.serial, meters.ki, meters.flags, metergroups.groupname FROM meters LEFT JOIN metergroups ON meters.group_id = metergroups.k WHERE meters.server_id = " + Vars.serverID + " AND meters.hide = 0 ORDER BY meters.group_id, meters.`name`");
        try {
            while (rs.next()) {
                Integer k = rs.getInt("meters.k");
                String metername = PDM.getStringFromHex(rs.getString("meters.name"));
                String groupname = PDM.getStringFromHex(rs.getString("metergroups.groupname"));
                String comport = rs.getString("meters.comport");
                Integer rsaddr = rs.getInt("meters.rsaddr");
                String serial = rs.getString("meters.serial");
                Integer ki = rs.getInt("meters.ki");
                String flags = rs.getString("meters.flags");
                EMeter emeter = new EMeter(metername, groupname, ki, comport, rsaddr, k);
                emeter.setMeterSN(serial);
                emeter.setMeterFlags(flags);
                emeter.setMeterFlag("busy", "no");

                Vars.meters.put(emeter.getIdInDB(), emeter);

                Object[] rowdata = new Object[2];
                String intab = "<html><b>" + emeter.getMeterName() + "</b>/" + emeter.getGroupName() + ", "
                        + emeter.getMeterComPort() + "/" + (emeter.getMeterAddress() & 0xFF)
                        + ", Ki:" + emeter.getMeterKi()
                        + "</html>";
                rowdata[0] = new IntString(emeter.getIdInDB(), intab);
                rowdata[1] = "Ok";

                tm.addRow(rowdata);
            }
        } catch (SQLException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
        pdm.closeResultSet();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        popupMenu1 = new java.awt.PopupMenu();
        menuItem1 = new java.awt.MenuItem();
        jButton1 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jPasswordField2 = new javax.swing.JPasswordField();
        jLabel6 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();

        popupMenu1.setLabel(Vars.Version);

        menuItem1.setLabel("Shut down JMercury Server");
        menuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItem1ActionPerformed(evt);
            }
        });
        popupMenu1.add(menuItem1);

        jButton1.setText("Shut down");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Database connection"));

        jLabel1.setText("URL:");

        jTextField1.setText("jdbc:mysql://server:3306/database");

        jLabel4.setText("User:");

        jLabel5.setText("Password:");

        jLabel6.setText("Server name:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 404, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField2))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPasswordField2))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField3)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jPasswordField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(15, Short.MAX_VALUE))
        );

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

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 442, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jButton5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 333, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton5)
                    .addComponent(jButton6)
                    .addComponent(jButton7)
                    .addComponent(jButton9))
                .addContainerGap())
        );

        jButton2.setText("Save & Run");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("test");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setText("Stop");
        jButton4.setEnabled(false);
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton8.setText(" test 1");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton1)))
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
                    .addComponent(jButton3)
                    .addComponent(jButton4)
                    .addComponent(jButton8))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        dispose();
        msrv.setMsvrRunning(false);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void menuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItem1ActionPerformed
        dispose();
        msrv.setMsvrRunning(false);
    }//GEN-LAST:event_menuItem1ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        if (Vars.serverID == -1) {
            JOptionPane.showMessageDialog(this, "The server name (" + jTextField3.getText().trim() + ") is not registered!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        NewMeterDialog dlg = new NewMeterDialog(this, true);
        dlg.setLocationRelativeTo(null);
        dlg.setVisible(true);
        RefreshTable();
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        SaveAndRun();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void SaveAndRun() {
        if (!msrvt.isAlive()) {
            msrvt.start();
        }
        if (!nsrvt.isAlive()) {
            nsrvt.start();
        }
        if (SaveAndConnect()) {
            jTextField1.setEnabled(false);
            jTextField2.setEnabled(false);
            jTextField3.setEnabled(false);
            jPasswordField2.setEnabled(false);
            jButton2.setEnabled(false);
            jButton4.setEnabled(true);
            jButton5.setEnabled(false);
            jButton6.setEnabled(false);
            jButton7.setEnabled(false);
            RefreshTable();
            msrv.setMsvrPaused(false);
        }
    }

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        SrvStop();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void SrvStop() {
        jTextField1.setEnabled(true);
        jTextField2.setEnabled(true);
        jTextField3.setEnabled(true);
        jPasswordField2.setEnabled(true);
        jButton2.setEnabled(true);
        jButton4.setEnabled(false);
        jButton5.setEnabled(true);
        jButton6.setEnabled(true);
        jButton7.setEnabled(true);
        msrv.setMsvrPaused(true);
    }

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed

        BufferedWriter bw = null;
        try {
            try {
                bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("outfile.txt"), "UTF-8"));
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }

        EMeter[] objects = new EMeter[34];

        objects[0] = new EMeter("Котельная 1", "Котельная", 60, "COM2", 99, 0);
        objects[1] = new EMeter("Котельная 2", "Котельная", 60, "COM3", 148, 0);
        objects[2] = new EMeter("Коттедж 1.1", "Коттедж 1", 20, "COM4", 42, 0);
        objects[3] = new EMeter("Коттедж 1.2", "Коттедж 1", 20, "COM5", 37, 0);
        objects[4] = new EMeter("Коттедж 2.1", "Коттедж 2", 20, "COM8", 43, 0);
        objects[5] = new EMeter("Коттедж 2.2", "Коттедж 2", 20, "COM9", 86, 0);
        objects[6] = new EMeter("Коттедж 3.1", "Коттедж 3", 20, "COM6", 57, 0);
        objects[7] = new EMeter("Коттедж 3.2", "", 20, "COM7", 54, 0);
        objects[8] = new EMeter("Коттедж 5.1", "", 20, "COM10", 64, 0);
        objects[9] = new EMeter("Коттедж 5.2", "", 20, "COM11", 66, 0);
        objects[10] = new EMeter("Коттедж 10.1", "", 20, "COM12", 56, 0);
        objects[11] = new EMeter("Коттедж 10.2", "", 20, "COM13", 18, 0);
        objects[12] = new EMeter("Павильон 1", "", 50, "COM14", 78, 0);
        objects[13] = new EMeter("Павильон 2", "", 50, "COM15", 79, 0);
        objects[14] = new EMeter("КД Сила 1", "", 200, "COM16", 49, 0);
        objects[15] = new EMeter("КД Сила 2", "", 200, "COM17", 38, 0);
        objects[16] = new EMeter("КД Осв 1", "", 160, "COM18", 87, 0);
        objects[17] = new EMeter("КД Осв 2", "", 160, "COM19", 13, 0);
        objects[18] = new EMeter("ПЦ 1", "", 200, "COM27", 26, 0);
        objects[19] = new EMeter("ПЦ 2", "", 200, "COM28", 1, 0);
        objects[20] = new EMeter("Гостиница 1", "", 200, "COM23", 20, 0);
        objects[21] = new EMeter("Гостиница 2", "", 200, "COM22", 21, 0);
        objects[22] = new EMeter("АХК 1", "", 200, "COM30", 2, 0);
        objects[23] = new EMeter("АХК 2", "", 200, "COM29", 217, 0);
        objects[24] = new EMeter("Коттедж 15.1", "", 20, "COM42", 11, 0);
        objects[25] = new EMeter("Коттедж 15.2", "", 20, "COM41", 12, 0);
        objects[26] = new EMeter("Коттедж 16.1", "", 20, "COM32", 87, 0);
        objects[27] = new EMeter("Коттедж 16.2", "", 20, "COM31", 69, 0);
        objects[28] = new EMeter("Коттедж 17.1", "", 20, "COM34", 71, 0);
        objects[29] = new EMeter("Коттедж 17.2", "", 20, "COM33", 83, 0);
        objects[30] = new EMeter("Коттедж 18.1", "", 20, "COM36", 96, 0);
        objects[31] = new EMeter("Коттедж 18.2", "", 20, "COM35", 8, 0);
        objects[32] = new EMeter("Коттедж 19.1", "", 20, "COM40", 61, 0);
        objects[33] = new EMeter("Коттедж 19.2", "", 20, "COM39", 31, 0);
        //objects[34] = new EMeter("Коттедж 20.1", "", 20, "COM38", 38, 0);
        //objects[35] = new EMeter("Коттедж 20.2", "", 20, "COM37", 28, 0);

        for (EMeter o : objects) {
            if (o == null) {
                continue;
            }

            EMeter meter = o;

            AplusRplus aprpb = null;
            AplusRplus aprpe = null;
            String metersn = null;

            int errs = 0;
            while (errs < 10) {
                try {
                    aprpb = meter.getAplusRplusMonthBegining(5);
                    //aprpe = meter.getAplusRplusMonthBegining(5);
                    aprpe = meter.getAplusRplusFromReset();
                    metersn = meter.getMeterSN();

                    if (aprpb == null || aprpe == null) {
                        errs++;
                        //System.out.println(meter.getMeterName() + ": ERR NULL!!! " + errs);
                        continue;
                    }
                    errs = 0;
                    break;
                } catch (Exception ex) {
                    errs++;
                    //System.out.println(meter.getMeterName() + ": ERR!!! " + errs);
                }
            }
//            if (errs != 0) {
//                System.out.println(meter.getMeterName() + ": ERR!!!ERR!!!ERR!!!");
//                return;
//            }

            String ostr;
            if (errs == 0) {
                ostr = metersn + ";" + aprpb.getAplus() / meter.getMeterKi() + ";" + aprpe.getAplus() / meter.getMeterKi();
            } else {
                ostr = "Err;Err;Err";
            }

            System.out.println(ostr);
            try {
                bw.write(ostr);
                bw.newLine();
            } catch (IOException ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        try {
            bw.close();
        } catch (IOException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        if (jTable1.getSelectedRow() == -1) {
            return;
        }

        EditMeterDialog dlg = new EditMeterDialog(this, true, ((IntString) jTable1.getValueAt(jTable1.getSelectedRow(), 0)).getInt());
        dlg.setLocationRelativeTo(null);
        dlg.setVisible(true);

        RefreshTable();
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        if (jTable1.getSelectedRow() == -1) {
            return;
        }

        Integer ki = ((IntString) jTable1.getValueAt(jTable1.getSelectedRow(), 0)).getInt();

        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to remove this item?", "Confirm item remove", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
            PDM pdm = new PDM();
            pdm.executeNonQueryUpdate("em", "UPDATE meters SET hide = 1 WHERE k = " + ki);
            RefreshTable();
        }
    }//GEN-LAST:event_jButton7ActionPerformed


    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed

//        Socket socket;
//        try {
//            socket = new Socket("localhost", 4545);
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
    }//GEN-LAST:event_jButton8ActionPerformed

    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseClicked
        if (evt.getButton() == 1 && evt.getClickCount() == 2 && msrv.isIsMsvrPaused()) {
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

        TasksDialog dlg = new TasksDialog(this, true, ((IntString) jTable1.getValueAt(jTable1.getSelectedRow(), 0)).getInt());
        dlg.setLocationRelativeTo(null);
        dlg.setVisible(true);
    }//GEN-LAST:event_jButton9ActionPerformed

    public static void main(String args[]) {
        // Logger
        try {
            Logger.getLogger("").addHandler(new FileHandler("errorlog.xml"));
        } catch (IOException | SecurityException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }

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
        Vars.PropPath = System.getProperty("user.home") + "/.JMercuryServer";
        Vars.PropFileName = Vars.PropPath + "/Options.xml";
        File dir = new File(Vars.PropPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        Vars.LoadProperties();

        App app = new App();
        app.setLocationRelativeTo(null);
        app.setVisible(true);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPasswordField jPasswordField2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private java.awt.MenuItem menuItem1;
    private java.awt.PopupMenu popupMenu1;
    // End of variables declaration//GEN-END:variables
}
