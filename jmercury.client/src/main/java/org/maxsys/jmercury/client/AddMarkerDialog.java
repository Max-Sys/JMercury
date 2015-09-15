package org.maxsys.jmercury.client;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import org.maxsys.calendarlib.CalendarDialog;
import org.maxsys.calendarlib.TimeDialog;

public class AddMarkerDialog extends javax.swing.JDialog {

    private int idInDB;

    public AddMarkerDialog(java.awt.Frame parent, int idInDB) {
        super(parent, true);
        initComponents();

        this.idInDB = idInDB;

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        jLabel4.setText(" " + sdf.format(Calendar.getInstance().getTime().getTime()) + " ");

        SimpleDateFormat sdfhm = new SimpleDateFormat("HH:mm");
        jLabel5.setText(" " + sdfhm.format(Calendar.getInstance().getTime()) + " ");
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Новая отсечка");
        setResizable(false);

        jButton1.setText("Отмена");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel1.setText("Название:");

        jLabel2.setText("например, \"начало мероприятия ...\" или \"гости съехали\"...");
        jLabel2.setEnabled(false);

        jButton2.setText("Ok");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jLabel3.setText("Дата и время:");

        jLabel4.setText(" 01.01.2015 ");
        jLabel4.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jLabel4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel4MouseClicked(evt);
            }
        });

        jLabel5.setText(" 00:00 ");
        jLabel5.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jLabel5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel5MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 355, Short.MAX_VALUE)
                            .addComponent(jTextField1)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel5)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5))
                .addGap(18, 18, 18)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jLabel5MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel5MouseClicked
        TimeDialog dlg = new TimeDialog(null, "ru", jLabel5.getText().trim());
        if (dlg.getStringTime() != null && !dlg.getStringTime().isEmpty()) {
            jLabel5.setText(" " + dlg.getStringTime() + " ");
        }
    }//GEN-LAST:event_jLabel5MouseClicked

    private void jLabel4MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel4MouseClicked
        String setcastr = jLabel4.getText().trim();
        String[] setcaf = setcastr.split("\\.");
        Calendar setca = null;
        if (setcaf.length == 3) {
            setca = new GregorianCalendar(Integer.valueOf(setcaf[2]), Integer.valueOf(setcaf[1]) - 1, Integer.valueOf(setcaf[0]));
        }
        CalendarDialog dlg = new CalendarDialog(null, "ru", setca);
        Calendar ca = dlg.getCalendar();
        if (ca != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
            jLabel4.setText(" " + sdf.format(ca.getTime()) + " ");
        }
    }//GEN-LAST:event_jLabel4MouseClicked

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        String name;
        if (!jTextField1.getText().isEmpty()) {
            name = jTextField1.getText();
        } else {
            name = "Отсечка " + jLabel4.getText().trim() + " " + jLabel5.getText().trim();
        }

        String caDstr = jLabel4.getText().trim();
        String[] caDstrs = caDstr.split("\\.");

        String caTstr = jLabel5.getText().trim();
        String[] caTstrs = caTstr.split(":");

        if (caDstrs.length != 3 || caTstrs.length != 2) {
            javax.swing.JOptionPane.showMessageDialog(null, "Ошибка даты или времени.");
            return;
        }

        Calendar ca = new GregorianCalendar(Integer.valueOf(caDstrs[2]), Integer.valueOf(caDstrs[1]) - 1, Integer.valueOf(caDstrs[0]), Integer.valueOf(caTstrs[0]), Integer.valueOf(caTstrs[1]));
        Calendar caForCheck = Calendar.getInstance();
        caForCheck.add(Calendar.MINUTE, -5);
        if (caForCheck.after(ca)) {
            javax.swing.JOptionPane.showMessageDialog(null, "Указанное время уже в прошлом. Будет использовано текущее время.");
        }

        NetClient.sendAddMarker(idInDB, name, ca);

        dispose();
    }//GEN-LAST:event_jButton2ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
