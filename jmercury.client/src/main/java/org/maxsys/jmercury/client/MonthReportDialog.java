package org.maxsys.jmercury.client;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRMapArrayDataSource;
import net.sf.jasperreports.view.JRViewer;

public class MonthReportDialog extends javax.swing.JDialog {

    class myCalendar extends GregorianCalendar {

        @Override
        public String toString() {
            SimpleDateFormat sdf = new SimpleDateFormat("LLLL yyyy");
            return sdf.format(this.getTime());
        }
    }

    public MonthReportDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        DefaultComboBoxModel cm = new DefaultComboBoxModel();
        Calendar[] cas = NetClient.sendGetMinMaxMonth();
        if (cas != null) {
            cas[0].set(Calendar.DAY_OF_MONTH, 1);
            cas[1].set(Calendar.DAY_OF_MONTH, 2);
            Calendar ca = new GregorianCalendar();
            ca.setTimeInMillis(cas[0].getTimeInMillis());
            while (cas[1].after(ca)) {
                myCalendar mca = new myCalendar();
                mca.setTimeInMillis(ca.getTimeInMillis());
                cm.addElement(mca);
                ca.add(Calendar.MONTH, 1);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Нет данных для отчета.");
        }
        jComboBox1.setModel(cm);
        jComboBox1.setSelectedIndex(jComboBox1.getItemCount() - 1);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();
        jSeparator1 = new javax.swing.JSeparator();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Отчет за месяц");
        setResizable(false);

        jButton1.setText("Отмена");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Просмотр");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jLabel1.setText("Месяц:");

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" }));
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
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
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBox1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 32, Short.MAX_VALUE)
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        if (jComboBox1.getSelectedItem() == null) {
            return;
        }

        dispose();

        int year = ((myCalendar) jComboBox1.getSelectedItem()).get(Calendar.YEAR);
        int month = ((myCalendar) jComboBox1.getSelectedItem()).get(Calendar.MONTH) + 1;

        ForReport[] frs = NetClient.sendGetForReport(year, month);

        if (frs == null) {
            JOptionPane.showMessageDialog(null, "Ошибка получения данных для отчета!");
            return;
        }

        ArrayList<Map> lrr = new ArrayList<>();

        double AplusSumAll = 0;

        for (ForReport fr : frs) {
            Map hm = new HashMap();
            hm.put("GroupName", fr.getGroupName());
            hm.put("MeterName", fr.getMeterName());
            hm.put("MeterSN", fr.getMeterSN());
            hm.put("Aplus1", fr.getAplus1());
            hm.put("Aplus2", fr.getAplus2());
            hm.put("Aplus21", fr.getAplus21());
            hm.put("MeterKi", fr.getMeterKi());
            hm.put("Aplus21Ki", fr.getAplus21Ki());
            hm.put("AplusGroupSum", fr.getAplusGroupSum());
            lrr.add(hm);
            double Aplus21Ki = Double.valueOf(fr.getAplus21Ki().replace(',', '.'));
            AplusSumAll += Aplus21Ki;

        }

        DecimalFormat df = new DecimalFormat("#.##");

        Map pm = new HashMap<>();
        pm.put("Title_1_date", jComboBox1.getSelectedItem().toString());
        pm.put("AplusSumAll", df.format(AplusSumAll));

        Map[] reportRows = new Map[lrr.size()];
        reportRows = lrr.toArray(reportRows);

        JRMapArrayDataSource dataSource = new JRMapArrayDataSource(reportRows);

        JasperPrint jp = null;
        try {
            jp = JasperFillManager.fillReport(App.class.getResourceAsStream("/org/maxsys/jmercury/client/resources/potreb.jasper"), pm, dataSource);
        } catch (JRException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }

        JRViewer jrv = new JRViewer(jp);

        JFrame frame = new JFrame("Report test");
        frame.getContentPane().add(jrv);
        int w = java.awt.Toolkit.getDefaultToolkit().getScreenSize().width - 100;
        int h = java.awt.Toolkit.getDefaultToolkit().getScreenSize().height - 100;
        frame.setSize(w, h);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
        jrv.setFitPageZoomRatio();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JSeparator jSeparator1;
    // End of variables declaration//GEN-END:variables
}
