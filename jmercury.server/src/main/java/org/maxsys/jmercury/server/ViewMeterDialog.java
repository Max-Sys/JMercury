package org.maxsys.jmercury.server;

import java.awt.Color;
import java.awt.Graphics;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.swing.JPanel;

public class ViewMeterDialog extends javax.swing.JDialog {

    private class DiagramPlotter extends JPanel {

        private boolean stroff = false;

        private void StrOn() {
            stroff = false;
        }

        private void StrOff() {
            stroff = true;
        }

        private void drawArrow(Graphics g, double x0, double y0, double a0, double a, double l) {
            int x1, y1, x2, y2;

            x1 = (int) x0;
            y1 = (int) y0;

            x2 = (int) (x0 + l * Math.sin(Math.toRadians(a0 + a)));
            y2 = (int) (y0 - l * Math.cos(Math.toRadians(a0 + a)));

            g.drawLine(x1, y1, x2, y2);
            if (!stroff) {
                g.drawLine(x1 + 1, y1, x2 + 1, y2);
                g.drawLine(x1, y1 + 1, x2, y2 + 1);
            }

            x1 = (int) (x2 + 10 * Math.sin(Math.toRadians(a0 + a - 160)));
            y1 = (int) (y2 - 10 * Math.cos(Math.toRadians(a0 + a - 160)));

            g.drawLine(x2, y2, x1, y1);
            if (!stroff) {
                g.drawLine(x2 + 1, y2, x1 + 1, y1);
                g.drawLine(x2, y2 + 1, x1, y1 + 1);
            }

            x1 = (int) (x2 + 10 * Math.sin(Math.toRadians(a0 + a + 160)));
            y1 = (int) (y2 - 10 * Math.cos(Math.toRadians(a0 + a + 160)));

            g.drawLine(x2, y2, x1, y1);
            if (!stroff) {
                g.drawLine(x2 + 1, y2, x1 + 1, y1);
                g.drawLine(x2, y2 + 1, x1, y1 + 1);
            }
        }

        @Override
        public void paint(Graphics g) {
            if (g == null) {
                return;
            }

            super.paint(g);

            int X0 = jPanel3.getWidth() / 2;
            int Y0 = jPanel3.getHeight() / 2;

            int UIMAX = Math.min(X0, Y0) - 15;
            double UMAX = Math.max(Math.max(au1, au2), au3);
            double uK = UIMAX / UMAX;
            if (uK == Double.POSITIVE_INFINITY) {
                return;
            }
            double IMAX = Math.max(Math.max(ai1, ai2), ai3);
            double iK = UIMAX / IMAX * 0.6;
            if (iK == Double.POSITIVE_INFINITY) {
                return;
            }

            if ((ai1 * iK) < (UIMAX * 0.25)) {
                ai1 = UIMAX * 0.25 / iK;
            }
            if ((ai2 * iK) < (UIMAX * 0.3)) {
                ai2 = UIMAX * 0.25 / iK;
            }
            if ((ai3 * iK) < (UIMAX * 0.3)) {
                ai3 = UIMAX * 0.25 / iK;
            }

            Color cy = new Color(194, 176, 18);
            g.setColor(cy);
            StrOn();
            drawArrow(g, X0, Y0, a0, 0, au1 * uK);
            StrOff();
            drawArrow(g, X0, Y0, a0, phi1, ai1 * iK);

            Color cg = new Color(0, 190, 0);
            g.setColor(cg);
            StrOn();
            drawArrow(g, X0, Y0, a0, aph12, au2 * uK);
            StrOff();
            drawArrow(g, X0, Y0, a0, aph12 + phi2, ai2 * iK);

            Color cr = new Color(255, 0, 0);
            g.setColor(cr);
            StrOn();
            drawArrow(g, X0, Y0, a0, aph13, au3 * uK);
            StrOff();
            drawArrow(g, X0, Y0, a0, aph13 + phi3, ai3 * iK);
        }
    }

    private final EMeter emeter;
    private double a0 = 0;
    private double au1, ai1, phi1;
    private double au2, ai2, phi2;
    private double au3, ai3, phi3;
    private double aph12, aph13, aph23;

    public ViewMeterDialog(java.awt.Frame parent, boolean modal, Integer ki) {
        super(parent, modal);
        initComponents();

        emeter = Vars.meters.get(ki);

        setTitle("Детальный просмотр - " + emeter.getMeterName());
        jLabel2.setText("Название: " + emeter.getMeterName() + "(" + emeter.getGroupName() + ")");
        jLabel3.setText("Серийный номер: " + emeter.getMeterSN());
        jLabel4.setText("Коэффициент трансформации тока: " + emeter.getMeterKi());

        jLabel39.setText("Cos \u03C6 = *.**");

        jProgressBar1.setEnabled(false);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new DiagramPlotter();
        jLabel36 = new javax.swing.JLabel();
        jLabel37 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        jLabel39 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jProgressBar1 = new javax.swing.JProgressBar();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        jButton1.setText("Закрыть");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder("Текущие показания (с учетом коэффициента трансформации)"));

        jLabel14.setText("Частота: ***** Гц");

        jLabel16.setText("U1: ***** В");

        jLabel17.setText("U2: ***** В");

        jLabel18.setText("U3: ***** В");

        jLabel19.setText("I1: ***** A");

        jLabel20.setText("I2: ***** A");

        jLabel22.setText("I3: ***** A");

        jLabel24.setText("P1: ******* Вт");

        jLabel25.setText("P2: ******* Вт");

        jLabel26.setText("P3: ******* Вт");

        jLabel28.setText("P: ******* Вт");

        jLabel27.setText("Q1: ******* вар");

        jLabel29.setText("Q2: ******* вар");

        jLabel30.setText("Q3: ******* вар");

        jLabel31.setText("Q: ******* вар");

        jLabel32.setText("S1: ******* ВА");

        jLabel33.setText("S2: ******* ВА");

        jLabel34.setText("S3: ******* ВА");

        jLabel35.setText("S: ******* ВА");

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jPanel3.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                jPanel3MouseWheelMoved(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 362, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jLabel36.setText("Угол 1-2: *****");

        jLabel37.setText("Угол 1-3: *****");

        jLabel38.setText("Угол 2-3: *****");

        jLabel39.setText("Cos Ф 1 = 0.99");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel38, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel37, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel36, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel39, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGap(13, 13, 13)
                        .addComponent(jLabel36)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel37)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel38)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel39)
                        .addGap(0, 249, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel32, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel27, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
                            .addComponent(jLabel24, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel16, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel19, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel20, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel25, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel29, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
                            .addComponent(jLabel33, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel26, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel22, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel18, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel30, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
                            .addComponent(jLabel34, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel31, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel28, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel35, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
                            .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(jLabel17)
                    .addComponent(jLabel18)
                    .addComponent(jLabel14))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19)
                    .addComponent(jLabel20)
                    .addComponent(jLabel22))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel24)
                    .addComponent(jLabel25)
                    .addComponent(jLabel26)
                    .addComponent(jLabel28))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel27)
                    .addComponent(jLabel29)
                    .addComponent(jLabel30)
                    .addComponent(jLabel31))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel32)
                    .addComponent(jLabel33)
                    .addComponent(jLabel34)
                    .addComponent(jLabel35))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Параметры счетчика"));

        jLabel2.setText("Название:");

        jLabel3.setText("Серийный номер:");

        jLabel4.setText("Коэффициент трансформации тока:");

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel7.setText("...");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addContainerGap(14, Short.MAX_VALUE))
        );

        jButton2.setText("Обновить");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jButton2)
                        .addGap(18, 18, 18)
                        .addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(jButton1))
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton2)
                    .addComponent(jButton1)
                    .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed

        jLabel7.setText("...");

        jLabel16.setText("U1: ***** В");
        jLabel17.setText("U2: ***** В");
        jLabel18.setText("U3: ***** В");

        jLabel14.setText("Частота: ***** Гц");

        jLabel19.setText("I1: ***** A");
        jLabel20.setText("I2: ***** A");
        jLabel22.setText("I3: ***** A");

        jLabel24.setText("P1: ******* Вт");
        jLabel25.setText("P2: ******* Вт");
        jLabel26.setText("P3: ******* Вт");
        jLabel28.setText("P: ******* Вт");

        jLabel27.setText("Q1: ******* вар");
        jLabel29.setText("Q2: ******* вар");
        jLabel30.setText("Q3: ******* вар");
        jLabel31.setText("Q: ******* вар");

        jLabel32.setText("S1: ******* ВА");
        jLabel33.setText("S2: ******* ВА");
        jLabel34.setText("S3: ******* ВА");
        jLabel35.setText("S: ******* ВА");

        jLabel36.setText("Угол 1-2: *****");
        jLabel37.setText("Угол 1-3: *****");
        jLabel38.setText("Угол 2-3: *****");

        jProgressBar1.setEnabled(true);
        jProgressBar1.setMaximum(23);
        jProgressBar1.setValue(0);

        Thread thr;
        thr = new Thread(new Runnable() {

            @Override
            public void run() {
                double u1 = 0, u2 = 0, u3 = 0, f;
                double i1 = 0, i2 = 0, i3 = 0;
                double p1 = 0, p2 = 0, p3 = 0, psum = 0;
                double q1 = 0, q2 = 0, q3 = 0, qsum;
                double s1, s2, s3, ssum = 0;
                double a12 = 0, a13 = 0, a23 = 0;

                DecimalFormat df = new DecimalFormat("#.##");
                DecimalFormat dfa = new DecimalFormat("#.#");
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");

                jButton2.setEnabled(false);

                Calendar ca = emeter.getMeterTime();
                if (ca != null) {
                    jLabel7.setText(sdf.format(ca.getTime()));
                } else {
                    jLabel7.setText("XXXXXXX");
                }
                jProgressBar1.setValue(jProgressBar1.getValue() + 1);

                try {
                    u1 = emeter.getU1();
                    jLabel16.setText("U1: " + df.format(u1) + " В");
                    jProgressBar1.setValue(jProgressBar1.getValue() + 1);
                    u2 = emeter.getU2();
                    jLabel17.setText("U2: " + df.format(u2) + " В");
                    jProgressBar1.setValue(jProgressBar1.getValue() + 1);
                    u3 = emeter.getU3();
                    jLabel18.setText("U3: " + df.format(u3) + " В");
                    jProgressBar1.setValue(jProgressBar1.getValue() + 1);
                    f = emeter.getFreq();
                    jLabel14.setText("Частота: " + df.format(f) + " Гц");
                    jProgressBar1.setValue(jProgressBar1.getValue() + 1);
                } catch (Exception e) {
                    jLabel16.setText("U1: XXXXXXX В");
                    jLabel17.setText("U2: XXXXXXX В");
                    jLabel18.setText("U3: XXXXXXX В");
                    jLabel14.setText("Частота: XXXXX Гц");
                    jProgressBar1.setValue(jProgressBar1.getValue() + 1);
                    jProgressBar1.setValue(jProgressBar1.getValue() + 1);
                    jProgressBar1.setValue(jProgressBar1.getValue() + 1);
                    jProgressBar1.setValue(jProgressBar1.getValue() + 1);
                }

                try {
                    i1 = emeter.getI1();
                    jLabel19.setText("I1: " + df.format(i1) + " А");
                    jProgressBar1.setValue(jProgressBar1.getValue() + 1);
                    i2 = emeter.getI2();
                    jLabel20.setText("I2: " + df.format(i2) + " А");
                    jProgressBar1.setValue(jProgressBar1.getValue() + 1);
                    i3 = emeter.getI3();
                    jLabel22.setText("I3: " + df.format(i3) + " А");
                    jProgressBar1.setValue(jProgressBar1.getValue() + 1);
                } catch (Exception e) {
                    jLabel19.setText("I1: XXXXXXX А");
                    jLabel20.setText("I2: XXXXXXX А");
                    jLabel22.setText("I3: XXXXXXX А");
                    jProgressBar1.setValue(jProgressBar1.getValue() + 1);
                    jProgressBar1.setValue(jProgressBar1.getValue() + 1);
                    jProgressBar1.setValue(jProgressBar1.getValue() + 1);
                }

                try {
                    p1 = emeter.getP1();
                    jLabel24.setText("P1: " + df.format(p1) + " Вт");
                    jProgressBar1.setValue(jProgressBar1.getValue() + 1);
                    p2 = emeter.getP2();
                    jLabel25.setText("P2: " + df.format(p2) + " Вт");
                    jProgressBar1.setValue(jProgressBar1.getValue() + 1);
                    p3 = emeter.getP3();
                    jLabel26.setText("P3: " + df.format(p3) + " Вт");
                    jProgressBar1.setValue(jProgressBar1.getValue() + 1);
                    psum = emeter.getPsum();
                    jLabel28.setText("P: " + df.format(psum) + " Вт");
                    jProgressBar1.setValue(jProgressBar1.getValue() + 1);
                } catch (Exception e) {
                    jLabel24.setText("P1: XXXXXXX Вт");
                    jLabel25.setText("P2: XXXXXXX Вт");
                    jLabel26.setText("P3: XXXXXXX Вт");
                    jLabel28.setText("P: XXXXXXX Вт");
                    jProgressBar1.setValue(jProgressBar1.getValue() + 1);
                    jProgressBar1.setValue(jProgressBar1.getValue() + 1);
                    jProgressBar1.setValue(jProgressBar1.getValue() + 1);
                    jProgressBar1.setValue(jProgressBar1.getValue() + 1);
                }

                try {
                    q1 = emeter.getQ1();
                    jLabel27.setText("Q1: " + df.format(q1) + " вар");
                    jProgressBar1.setValue(jProgressBar1.getValue() + 1);
                    q2 = emeter.getQ2();
                    jLabel29.setText("Q2: " + df.format(q2) + " вар");
                    jProgressBar1.setValue(jProgressBar1.getValue() + 1);
                    q3 = emeter.getQ3();
                    jLabel30.setText("Q3: " + df.format(q3) + " вар");
                    jProgressBar1.setValue(jProgressBar1.getValue() + 1);
                    qsum = emeter.getQsum();
                    jLabel31.setText("Q: " + df.format(qsum) + " вар");
                    jProgressBar1.setValue(jProgressBar1.getValue() + 1);
                } catch (Exception e) {
                    jLabel27.setText("Q1: XXXXXXX вар");
                    jLabel29.setText("Q2: XXXXXXX вар");
                    jLabel30.setText("Q3: XXXXXXX вар");
                    jLabel31.setText("Q: XXXXXXX вар");
                    jProgressBar1.setValue(jProgressBar1.getValue() + 1);
                    jProgressBar1.setValue(jProgressBar1.getValue() + 1);
                    jProgressBar1.setValue(jProgressBar1.getValue() + 1);
                    jProgressBar1.setValue(jProgressBar1.getValue() + 1);
                }

                try {
                    s1 = emeter.getS1();
                    jLabel32.setText("S1: " + df.format(s1) + " ВА");
                    jProgressBar1.setValue(jProgressBar1.getValue() + 1);
                    s2 = emeter.getS2();
                    jLabel33.setText("S2: " + df.format(s2) + " ВА");
                    jProgressBar1.setValue(jProgressBar1.getValue() + 1);
                    s3 = emeter.getS3();
                    jLabel34.setText("S3: " + df.format(s3) + " ВА");
                    jProgressBar1.setValue(jProgressBar1.getValue() + 1);
                    ssum = emeter.getSsum();
                    jLabel35.setText("S: " + df.format(ssum) + " ВА");
                    jProgressBar1.setValue(jProgressBar1.getValue() + 1);
                } catch (Exception e) {
                    jLabel32.setText("S1: XXXXXXX ВА");
                    jLabel33.setText("S2: XXXXXXX ВА");
                    jLabel34.setText("S3: XXXXXXX ВА");
                    jLabel35.setText("S: XXXXXXX ВА");
                    jProgressBar1.setValue(jProgressBar1.getValue() + 1);
                    jProgressBar1.setValue(jProgressBar1.getValue() + 1);
                    jProgressBar1.setValue(jProgressBar1.getValue() + 1);
                    jProgressBar1.setValue(jProgressBar1.getValue() + 1);
                }

                try {
                    a12 = emeter.getA12();
                    jLabel36.setText("Угол 1-2: " + dfa.format(a12));
                    jProgressBar1.setValue(jProgressBar1.getValue() + 1);
                    a13 = emeter.getA13();
                    jLabel37.setText("Угол 1-3: " + dfa.format(a13));
                    jProgressBar1.setValue(jProgressBar1.getValue() + 1);
                    a23 = emeter.getA23();
                    jLabel38.setText("Угол 2-3: " + dfa.format(a23));
                    jProgressBar1.setValue(jProgressBar1.getValue() + 1);
                } catch (Exception e) {
                    jLabel36.setText("Угол 1-2: XXXXX");
                    jLabel37.setText("Угол 1-3: XXXXX");
                    jLabel38.setText("Угол 2-3: XXXXX");
                    jProgressBar1.setValue(jProgressBar1.getValue() + 1);
                    jProgressBar1.setValue(jProgressBar1.getValue() + 1);
                    jProgressBar1.setValue(jProgressBar1.getValue() + 1);
                }

                if (psum != 0 && ssum != 0) {
                    jLabel39.setText("Cos \u03C6 = " + df.format(psum / ssum));
                } else {
                    jLabel39.setText("Cos \u03C6 = XXXXX");
                }

                au1 = u1;
                au2 = u2;
                au3 = u3;

                ai1 = i1;
                ai2 = i2;
                ai3 = i3;

                aph12 = a12;
                aph13 = a13;
                aph23 = a23;

                if (q1 != 0 && p1 != 0) {
                    phi1 = Math.toDegrees(Math.atan(q1 / p1));
                }
                if (q2 != 0 && p2 != 0) {
                    phi2 = Math.toDegrees(Math.atan(q2 / p2));
                }
                if (q3 != 0 && p3 != 0) {
                    phi3 = Math.toDegrees(Math.atan(q3 / p3));
                }

                jPanel3.paint(jPanel3.getGraphics());

                jProgressBar1.setEnabled(false);
                jProgressBar1.setValue(0);
                jButton2.setEnabled(true);
            }
        });
        thr.start();

    }//GEN-LAST:event_jButton2ActionPerformed

    private void jPanel3MouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_jPanel3MouseWheelMoved
        if (evt.getWheelRotation() < 0) {
            a0 = a0 - 5;
            jPanel3.paint(jPanel3.getGraphics());
        } else {
            a0 = a0 + 5;
            jPanel3.paint(jPanel3.getGraphics());
        }
    }//GEN-LAST:event_jPanel3MouseWheelMoved

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JProgressBar jProgressBar1;
    // End of variables declaration//GEN-END:variables
}
