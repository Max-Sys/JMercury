package org.maxsys.jmercury.client;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.io.FileUtils;

public class ProgramUpdateDialog extends javax.swing.JDialog {

    final int updateNumber;

    public ProgramUpdateDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        jLabel1.setText("Текущая версия: " + Vars.Version);

        String myunstr = Vars.prop.getProperty("MyUN") == null ? "0" : Vars.prop.getProperty("MyUN");
        int myun = Integer.valueOf(myunstr);
        updateNumber = NetClient.sendGetUpdateNumber();
        if (updateNumber > myun) {
            jLabel3.setText("Есть новая версия программы. Обновление возможно.");
        } else {
            jLabel3.setText("Установлена самая новая версия. Обновление не нужно.");
            jButton1.setText("Принудительно обновить программу");
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jProgressBar1 = new javax.swing.JProgressBar();
        jButton2 = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Обновление программы");
        setResizable(false);

        jButton1.setText("Обновить программу");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel1.setText("Текущая версия: ");

        jButton2.setText("Закрыть");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jLabel3.setText("...");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 158, Short.MAX_VALUE)
                        .addComponent(jButton2))
                    .addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2)
                    .addComponent(jButton1))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        jButton1.setEnabled(false);
        jButton2.setEnabled(false);
        jLabel1.setEnabled(false);

        Thread thr = new Thread(new Runnable() {

            @Override
            public void run() {
                jLabel3.setText("Получение файла...");

                File upfile = new File("update.zip");

                if (upfile.exists()) {
                    try {
                        Files.delete(upfile.toPath());
                    } catch (IOException ex) {
                        Logger.getLogger(ProgramUpdateDialog.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                Socket socket = NetClient.GetNewSocket();
                NetClient.SendToSrv(socket, "GetUpdate");

                int size = Integer.valueOf(NetClient.GetRespFromSrv(socket));
                if (size == 0) {
                    javax.swing.JOptionPane.showMessageDialog(null, "Ошибка обновления!");
                    dispose();
                    return;
                }

                jProgressBar1.setMaximum(size);

                try {
                    byte[] bytes = new byte[4096];
                    InputStream is = socket.getInputStream();
                    FileOutputStream fos = new FileOutputStream(upfile);
                    try (BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                        int bytesread;
                        while ((bytesread = is.read(bytes)) > 0) {
                            bos.write(bytes, 0, bytesread);
                            jProgressBar1.setValue(jProgressBar1.getValue() + bytesread);
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(ProgramUpdateDialog.class.getName()).log(Level.SEVERE, null, ex);
                }

                NetClient.CloseSocket(socket);

                File upfolder = new File("update");
                if (upfolder.exists()) {
                    try {
                        FileUtils.deleteDirectory(upfolder);
                    } catch (IOException ex) {
                        Logger.getLogger(ProgramUpdateDialog.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                try {
                    try (ZipFile zipfile = new ZipFile(upfile)) {
                        jLabel3.setText("Распаковка файла...");
                        jProgressBar1.setMaximum(zipfile.size());
                        jProgressBar1.setValue(0);
                        Enumeration<? extends ZipEntry> ents = zipfile.entries();
                        while (ents.hasMoreElements()) {
                            ZipEntry ent = (ZipEntry) ents.nextElement();
                            File entf = new File(ent.getName());

                            if (ent.isDirectory()) {
                                entf.mkdirs();
                                continue;
                            }

                            FileOutputStream os;
                            try (InputStream is = zipfile.getInputStream(ent)) {
                                os = new FileOutputStream(entf);
                                int length;
                                byte[] bytes = new byte[4096];
                                while ((length = is.read(bytes)) > 0) {
                                    os.write(bytes, 0, length);
                                }
                            }
                            os.close();

                            jProgressBar1.setValue(jProgressBar1.getValue() + 1);
                        }
                        zipfile.close();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(ProgramUpdateDialog.class.getName()).log(Level.SEVERE, null, ex);
                }

                jLabel3.setText("Перезапуск...");
                Vars.prop.setProperty("MyUN", String.valueOf(updateNumber));
                Vars.SaveProperties();

                try {
                    Files.delete(upfile.toPath());
                } catch (IOException ex) {
                    Logger.getLogger(ProgramUpdateDialog.class.getName()).log(Level.SEVERE, null, ex);
                }

                try {
                    Runtime.getRuntime().exec("java -jar jmercury.client.jar", null, new File(System.getProperty("user.dir") + "/update"));
                } catch (IOException ex) {
                    Logger.getLogger(ProgramUpdateDialog.class.getName()).log(Level.SEVERE, null, ex);
                }

                System.exit(0);
            }
        });
        thr.start();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        dispose();
    }//GEN-LAST:event_jButton2ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JProgressBar jProgressBar1;
    // End of variables declaration//GEN-END:variables
}
