package org.maxsys.jmercury.server;

import java.util.Calendar;

public class TasksDialog extends javax.swing.JDialog {

    EMeter meter;

    public TasksDialog(java.awt.Frame parent, boolean modal, Integer ki) {
        super(parent, modal);
        initComponents();

        meter = Vars.meters.get(ki);

        setTitle("Tasks for " + meter.getMeterName());

        String osv = meter.getMeterFlag("osv") == null ? "no" : meter.getMeterFlag("osv");
        if (osv.equals("yes")) {
            jCheckBox1.setSelected(true);
        } else {
            jCheckBox1.setSelected(false);
        }

        String AvgARsTask = meter.getMeterFlag("AvgARsTask") == null ? "off" : meter.getMeterFlag("AvgARsTask");
        if (AvgARsTask.equals("on")) {
            jComboBox1.setSelectedIndex(0);
        } else {
            jComboBox1.setSelectedIndex(1);
        }
        String s_AvgARsTask_i = meter.getMeterFlag("AvgARsTask_i") == null ? "0" : meter.getMeterFlag("AvgARsTask_i");
        long AvgARsTask_i = Long.valueOf(s_AvgARsTask_i);
        AvgARsTask_i /= 1000;
        if (AvgARsTask_i < 60) {
            AvgARsTask_i = 60;
        }
        jSpinner1.setValue(AvgARsTask_i);

        String DaysTask = meter.getMeterFlag("DaysTask") == null ? "off" : meter.getMeterFlag("DaysTask");
        if (DaysTask.equals("on")) {
            jComboBox3.setSelectedIndex(0);
        } else {
            jComboBox3.setSelectedIndex(1);
        }
        String s_DaysTask_i = meter.getMeterFlag("DaysTask_i") == null ? "0" : meter.getMeterFlag("DaysTask_i");
        long DaysTaskTask_i = Long.valueOf(s_DaysTask_i);
        DaysTaskTask_i /= 1000;
        if (DaysTaskTask_i < 60) {
            DaysTaskTask_i = 60;
        }
        jSpinner3.setValue(DaysTaskTask_i);

        String MonthTask = meter.getMeterFlag("MonthTask") == null ? "off" : meter.getMeterFlag("MonthTask");
        if (MonthTask.equals("on")) {
            jComboBox2.setSelectedIndex(0);
        } else {
            jComboBox2.setSelectedIndex(1);
        }
        String s_MonthTask_i = meter.getMeterFlag("MonthTask_i") == null ? "0" : meter.getMeterFlag("MonthTask_i");
        long MonthTask_i = Long.valueOf(s_MonthTask_i);
        MonthTask_i /= 1000;
        if (MonthTask_i < 60) {
            MonthTask_i = 60;
        }
        jSpinner2.setValue(MonthTask_i);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        jSpinner1 = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        jComboBox2 = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        jSpinner2 = new javax.swing.JSpinner();
        jLabel5 = new javax.swing.JLabel();
        jComboBox3 = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        jSpinner3 = new javax.swing.JSpinner();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        jButton1.setText("Cancel");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Ok");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Meter control"));

        jCheckBox1.setText("Out of service");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckBox1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckBox1)
                .addContainerGap(19, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Meter tasks"));

        jLabel1.setText("AvgARs task:");

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "On", "Off" }));

        jLabel2.setText("AvgARs task interval (sec):");

        jSpinner1.setModel(new javax.swing.SpinnerNumberModel(Long.valueOf(60L), Long.valueOf(60L), Long.valueOf(31536000L), Long.valueOf(1L)));

        jLabel3.setText("MonthData task:");

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "On", "Off" }));

        jLabel4.setText("MonthData task interval (sec):");

        jSpinner2.setModel(new javax.swing.SpinnerNumberModel(Long.valueOf(60L), Long.valueOf(60L), Long.valueOf(31536000L), Long.valueOf(1L)));

        jLabel5.setText("DaysData task:");

        jComboBox3.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "On", "Off" }));

        jLabel6.setText("DaysData task interval (sec):");

        jSpinner3.setModel(new javax.swing.SpinnerNumberModel(Long.valueOf(60L), Long.valueOf(60L), Long.valueOf(31536000L), Long.valueOf(1L)));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSpinner1))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSpinner2))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSpinner3)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(jSpinner3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(jSpinner2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(13, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1))
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
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

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        if (jCheckBox1.isSelected()) {
            meter.setMeterFlag("osv", "yes");
        } else {
            meter.setMeterFlag("osv", "no");
        }

        if (jComboBox1.getSelectedIndex() == 0) {
            meter.setMeterFlag("AvgARsTask", "on");
        } else {
            meter.setMeterFlag("AvgARsTask", "off");
        }
        long AvgARsTask_i = (long) jSpinner1.getValue();
        AvgARsTask_i *= 1000;
        meter.setMeterFlag("AvgARsTask_i", String.valueOf(AvgARsTask_i));
        long AvgARsTask_t = Calendar.getInstance().getTimeInMillis() - AvgARsTask_i + 5000;
        meter.setMeterFlag("AvgARsTask_t", String.valueOf(AvgARsTask_t));

        if (jComboBox3.getSelectedIndex() == 0) {
            meter.setMeterFlag("DaysTask", "on");
        } else {
            meter.setMeterFlag("DaysTask", "off");
        }
        long DaysTask_i = (long) jSpinner3.getValue();
        DaysTask_i *= 1000;
        meter.setMeterFlag("DaysTask_i", String.valueOf(DaysTask_i));
        long DaysTask_t = Calendar.getInstance().getTimeInMillis() - DaysTask_i + 5000;
        meter.setMeterFlag("DaysTask_t", String.valueOf(DaysTask_t));

        if (jComboBox2.getSelectedIndex() == 0) {
            meter.setMeterFlag("MonthTask", "on");
        } else {
            meter.setMeterFlag("MonthTask", "off");
        }
        long MonthTask_i = (long) jSpinner2.getValue();
        MonthTask_i *= 1000;
        meter.setMeterFlag("MonthTask_i", String.valueOf(MonthTask_i));
        long MonthTask_t = Calendar.getInstance().getTimeInMillis() - MonthTask_i + 5000;
        meter.setMeterFlag("MonthTask_t", String.valueOf(MonthTask_t));

        Vars.SaveMeterState(meter);

        dispose();
    }//GEN-LAST:event_jButton2ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JComboBox jComboBox2;
    private javax.swing.JComboBox jComboBox3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JSpinner jSpinner2;
    private javax.swing.JSpinner jSpinner3;
    // End of variables declaration//GEN-END:variables
}
