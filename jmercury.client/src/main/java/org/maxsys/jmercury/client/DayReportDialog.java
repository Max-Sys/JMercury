package org.maxsys.jmercury.client;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRMapArrayDataSource;
import net.sf.jasperreports.view.JRViewer;
import org.maxsys.calendarlib.CalendarDialog;

public class DayReportDialog extends javax.swing.JDialog {

    CalendarString cs = new CalendarString("dd.MM.yyyy");

    public DayReportDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        jLabel2.setText(" " + cs.toString() + " ");
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSeparator1 = new javax.swing.JSeparator();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Отчет за сутки");
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

        jLabel1.setText("Дата отчета:");

        jLabel2.setText(" 01.01.2015 ");
        jLabel2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jLabel2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel2MouseClicked(evt);
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
                        .addComponent(jLabel2)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 52, Short.MAX_VALUE)
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
                    .addComponent(jLabel2))
                .addGap(18, 18, 18)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2)
                    .addComponent(jButton1))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        dispose();

        boolean isFullDay = true;

        if (cs.isToday()) {
            if (JOptionPane.showConfirmDialog(this, "Обновить данные перед выводом отчета?", "Обновление", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                RefreshBeforeReportDialog dlg = new RefreshBeforeReportDialog(null, "Days");
                dlg.setLocationRelativeTo(null);
                dlg.setVisible(true);
            }
            isFullDay = false;
        }

        ForReport[] frs = NetClient.sendGetForReportDays(cs.getYear(), cs.getMonth(), cs.getDay());

        if (frs == null) {
            JOptionPane.showMessageDialog(null, "Ошибка получения данных для отчета!");
            return;
        }

        ArrayList<Map> lrr = new ArrayList<>();

        double AplusSumAll = 0;

        Arrays.sort(frs, new Comparator<Object>() {

            @Override
            public int compare(Object o1, Object o2) {
                ForReport fro1 = (ForReport) o1;
                ForReport fro2 = (ForReport) o2;
                String so1 = Vars.AddNullsInString(fro1.getGroupName());
                String so2 = Vars.AddNullsInString(fro2.getGroupName());
                return so1.compareToIgnoreCase(so2);
            }
        });

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
        if (isFullDay) {
            pm.put("Title_1_date", "об электропотреблении за сутки " + cs.toString() + " г.");
            pm.put("EndText", "24 час.");
            pm.put("StarText", "");
        } else {
            pm.put("Title_1_date", "об электропотреблении за сутки" + cs.toString() + " г. *");
            pm.put("EndText", "24 час. *");
            pm.put("StarText", "* показаны данные за неполные сутки");
        }
        pm.put("AplusSumAll", df.format(AplusSumAll));
        pm.put("BeginText", "0 час.");

        Map[] reportRows = new Map[lrr.size()];
        reportRows = lrr.toArray(reportRows);

        JRMapArrayDataSource dataSource = new JRMapArrayDataSource(reportRows);

        JasperPrint jp = null;
        try {
            jp = JasperFillManager.fillReport(System.class.getResourceAsStream("/org/maxsys/jmercury/client/resources/potreb.jasper"), pm, dataSource);
        } catch (JRException ex) {
            Logger.getLogger(DayReportDialog.class.getName()).log(Level.SEVERE, null, ex);
        }

        JRViewer jrv = new JRViewer(jp);

        JFrame frame = new JFrame("Отчет за сутки");
        frame.getContentPane().add(jrv);
        int w = java.awt.Toolkit.getDefaultToolkit().getScreenSize().width - 100;
        int h = java.awt.Toolkit.getDefaultToolkit().getScreenSize().height - 100;
        frame.setSize(w, h);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
        jrv.setFitPageZoomRatio();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jLabel2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel2MouseClicked
        CalendarDialog dlg = new CalendarDialog(null, "ru", cs);
        if (dlg.getCalendar() != null) {
            cs.setCalendar(dlg.getCalendar());
            jLabel2.setText(" " + cs.toString() + " ");
        }
    }//GEN-LAST:event_jLabel2MouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JSeparator jSeparator1;
    // End of variables declaration//GEN-END:variables
}
