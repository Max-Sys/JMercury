package org.maxsys.jmercury.client;

import java.awt.Image;
import java.awt.Rectangle;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.util.TreeMap;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.maxsys.calendarlib.CalendarDialog;

public class MainFrame extends javax.swing.JFrame {

    public MainFrame() {
        initComponents();

        Properties srvProps = NetClient.sendGetServerProps();
        setTitle(Vars.Version + " - подключено к " + srvProps.getProperty("Servername"));
        Image icon = new javax.swing.ImageIcon(getClass().getResource("/org/maxsys/jmercury/client/resources/icon_1_1.png")).getImage();
        setIconImage(icon);

        DefaultTableModel tm1 = new DefaultTableModel(
                new Object[][]{},
                new String[]{"Дата (дни)", "A+ на 00:00, кВт⋅ч", "R+ на 00:00, квар⋅ч", "A+, кВт⋅ч", "R+, квар⋅ч"}) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };
        jTable1.setModel(tm1);
        jTable1.getColumnModel().getColumn(0).setPreferredWidth(300);
        jTable1.getColumnModel().getColumn(1).setPreferredWidth(175);
        jTable1.getColumnModel().getColumn(2).setPreferredWidth(175);
        jTable1.getColumnModel().getColumn(3).setPreferredWidth(175);
        jTable1.getColumnModel().getColumn(4).setPreferredWidth(175);

        DefaultTableModel tm2 = new DefaultTableModel(
                new Object[][]{},
                new String[]{"Дата (мес)", "A+ начало", "A+ конец", "A+", "R+ начало", "R+ конец", "R+"}) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };
        jTable2.setModel(tm2);
        jTable2.getColumnModel().getColumn(0).setPreferredWidth(160);
        jTable2.getColumnModel().getColumn(1).setPreferredWidth(140);
        jTable2.getColumnModel().getColumn(2).setPreferredWidth(140);
        jTable2.getColumnModel().getColumn(3).setPreferredWidth(140);
        jTable2.getColumnModel().getColumn(4).setPreferredWidth(140);
        jTable2.getColumnModel().getColumn(5).setPreferredWidth(140);
        jTable2.getColumnModel().getColumn(6).setPreferredWidth(140);

        DefaultTableModel tm3 = new DefaultTableModel(
                new Object[][]{},
                new String[]{"Дата/время", "A+, кВт", "R+, квар", "Период"}) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };
        jTable3.setModel(tm3);
        jTable3.getColumnModel().getColumn(0).setPreferredWidth(300);
        jTable3.getColumnModel().getColumn(1).setPreferredWidth(250);
        jTable3.getColumnModel().getColumn(2).setPreferredWidth(250);
        jTable3.getColumnModel().getColumn(3).setPreferredWidth(200);

        DefaultTableModel tm4 = new DefaultTableModel(
                new Object[][]{},
                new String[]{"Название", "Дата/время", "A+, кВт⋅ч", "R+, квар⋅ч"}) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };
        jTable4.setModel(tm4);
        jTable4.getColumnModel().getColumn(0).setPreferredWidth(350);
        jTable4.getColumnModel().getColumn(0).setPreferredWidth(250);
        jTable4.getColumnModel().getColumn(0).setPreferredWidth(200);
        jTable4.getColumnModel().getColumn(0).setPreferredWidth(200);

        jTable4.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (jTable4.getSelectedRow() > -1) {
                    jButton5.setEnabled(true);
                } else {
                    jButton5.setEnabled(false);
                }
            }
        });

        Calendar ca = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        jLabel6.setText(" " + sdf.format(ca.getTime()) + " ");
        jLabel10.setText(" " + sdf.format(ca.getTime()) + " ");
        ca.add(Calendar.DAY_OF_YEAR, -1);
        jLabel4.setText(" " + sdf.format(ca.getTime()) + " ");
        ca.add(Calendar.DAY_OF_YEAR, 1);
        ca.add(Calendar.MONTH, -3);
        jLabel8.setText(" " + sdf.format(ca.getTime()) + " ");

        RefreshTree();

        if (Vars.prop.getProperty("AutoT3") != null && Vars.prop.getProperty("AutoT3").toLowerCase().equals("true")) {
            jCheckBoxMenuItem1.setSelected(true);
        }

        jSplitPane1.setDividerLocation(0.33);
        pack();
        setMinimumSize(getSize());
    }

    private void RefreshTree() {
        jLabel1.setText("---");

        DefaultTableModel tm1 = (DefaultTableModel) jTable1.getModel();
        while (tm1.getRowCount() > 0) {
            tm1.removeRow(0);
        }
        DefaultTableModel tm2 = (DefaultTableModel) jTable2.getModel();
        while (tm2.getRowCount() > 0) {
            tm2.removeRow(0);
        }
        DefaultTableModel tm3 = (DefaultTableModel) jTable3.getModel();
        while (tm3.getRowCount() > 0) {
            tm3.removeRow(0);
        }
        DefaultTableModel tm4 = (DefaultTableModel) jTable3.getModel();
        while (tm4.getRowCount() > 0) {
            tm4.removeRow(0);
        }

        //jButton1.setEnabled(false);
        jButton2.setEnabled(false);
        jButton3.setEnabled(false);
        jButton4.setEnabled(false);

        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Группы");

        String metersData = NetClient.sendGetMetersData();
        if (metersData.isEmpty()) {
            return;
        }

        String[] metersDatas = metersData.split("\n");
        Arrays.sort(metersDatas, new Comparator<Object>() {

            @Override
            public int compare(Object o1, Object o2) {
                String so1 = Vars.AddNullsInString(o1.toString().split("\001")[2]);
                String so2 = Vars.AddNullsInString(o2.toString().split("\001")[2]);
                return so1.compareToIgnoreCase(so2);
            }
        });

        metersDatas:
        for (String mData : metersDatas) {
            String[] mparams = mData.split("\001");
            MeterInfo meterInfo = new MeterInfo(Integer.valueOf(mparams[0]), mparams[1], mparams[2], Integer.valueOf(mparams[5]), mparams[6], mparams[7]);
            for (Enumeration e = rootNode.children(); e.hasMoreElements();) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
                if (((String) node.getUserObject()).equals(meterInfo.getGroup())) {
                    node.add(new DefaultMutableTreeNode(meterInfo));
                    continue metersDatas;
                }
            }
            DefaultMutableTreeNode grn = new DefaultMutableTreeNode(meterInfo.getGroup());
            grn.add(new DefaultMutableTreeNode(meterInfo));
            rootNode.add(grn);
        }

        jTree1.setModel(new DefaultTreeModel(rootNode));
    }

    private void RefreshTable1(int IdInDB) {
        DefaultTableModel tm = (DefaultTableModel) jTable1.getModel();
        while (tm.getRowCount() > 0) {
            tm.removeRow(0);
        }

        String caFromStr = jLabel8.getText().trim();
        String[] caFromF = caFromStr.split("\\.");
        Calendar caFrom;
        if (caFromF.length == 3) {
            caFrom = new GregorianCalendar(Integer.valueOf(caFromF[2]), Integer.valueOf(caFromF[1]) - 1, Integer.valueOf(caFromF[0]));
        } else {
            caFrom = Calendar.getInstance();
            caFrom.add(Calendar.DAY_OF_YEAR, -1);
        }

        String caToStr = jLabel10.getText().trim();
        String[] caToF = caToStr.split("\\.");
        Calendar caTo;
        if (caToF.length == 3) {
            caTo = new GregorianCalendar(Integer.valueOf(caToF[2]), Integer.valueOf(caToF[1]) - 1, Integer.valueOf(caToF[0]), 23, 59);
        } else {
            caTo = Calendar.getInstance();
        }

        DecimalFormat df = new DecimalFormat("#.##");
        ArrayList<AplusRplusD> aprpds = NetClient.sendGetApRpDays(IdInDB, caFrom, caTo);

        for (AplusRplusD aprpd : aprpds) {
            Object[] rowData = new Object[5];
            CalendarString cs = new CalendarString(aprpd.getAprpDate());
            if (cs.isToday()) {
                cs.setFormat("dd MMMM yyyy (*)");
            } else {
                cs.setFormat("dd MMMM yyyy");
            }
            rowData[0] = cs;
            rowData[1] = df.format(aprpd.getAplusOnBeg());
            rowData[2] = df.format(aprpd.getRplusOnBeg());
            rowData[3] = df.format(aprpd.getAplus());
            rowData[4] = df.format(aprpd.getRplus());
            tm.addRow(rowData);
        }

        jTable1.scrollRectToVisible(new Rectangle(jTable1.getCellRect(jTable1.getRowCount() - 1, 0, true)));

        if (aprpds.size() > 0 && jTabbedPane1.getSelectedIndex() == 1) {
            jButton2.setEnabled(true);
        }
    }

    private void RefreshTable2(int IdInDB) {
        DefaultTableModel tm = (DefaultTableModel) jTable2.getModel();
        while (tm.getRowCount() > 0) {
            tm.removeRow(0);
        }

        //SimpleDateFormat sdf = new SimpleDateFormat("LLLL yyyy");
        DecimalFormat df = new DecimalFormat("#.##");

        ArrayList<AplusRplusM> aprpms = NetClient.sendGetApRpMonths(IdInDB);

        for (AplusRplusM aprpm : aprpms) {
            Object[] rowData = new Object[7];
            //rowData[0] = sdf.format(aprpm.getAprpDate().getTime());
            CalendarString cs = new CalendarString(aprpm.getAprpDate());
            if (cs.isNowMonth()) {
                cs.setFormat("LLLL yyyy (*)");
            } else {
                cs.setFormat("LLLL yyyy");
            }
            rowData[0] = cs;
            rowData[1] = df.format(aprpm.getAplusOnBeg());
            rowData[2] = df.format(aprpm.getAplusOnEnd());
            rowData[3] = df.format(aprpm.getAplus());
            rowData[4] = df.format(aprpm.getRplusOnBeg());
            rowData[5] = df.format(aprpm.getRplusOnEnd());
            rowData[6] = df.format(aprpm.getRplus());
            tm.addRow(rowData);
        }

        jTable2.scrollRectToVisible(new Rectangle(jTable2.getCellRect(jTable2.getRowCount() - 1, 0, true)));
    }

    private void RefreshTable3(int IdInDB) {
        DefaultTableModel tm = (DefaultTableModel) jTable3.getModel();
        while (tm.getRowCount() > 0) {
            tm.removeRow(0);
        }

        String caFromStr = jLabel4.getText().trim();
        String[] caFromF = caFromStr.split("\\.");
        Calendar caFrom;
        if (caFromF.length == 3) {
            caFrom = new GregorianCalendar(Integer.valueOf(caFromF[2]), Integer.valueOf(caFromF[1]) - 1, Integer.valueOf(caFromF[0]));
        } else {
            caFrom = Calendar.getInstance();
            caFrom.add(Calendar.DAY_OF_YEAR, -1);
        }

        String caToStr = jLabel6.getText().trim();
        String[] caToF = caToStr.split("\\.");
        Calendar caTo;
        if (caToF.length == 3) {
            caTo = new GregorianCalendar(Integer.valueOf(caToF[2]), Integer.valueOf(caToF[1]) - 1, Integer.valueOf(caToF[0]), 23, 59);
        } else {
            caTo = Calendar.getInstance();
        }

        ArrayList<AvgAR> aars = NetClient.sendGetAvgArs(IdInDB, caFrom, caTo);

        DecimalFormat df = new DecimalFormat("#.##");

        for (AvgAR aar : aars) {
            Object[] rowData = new Object[4];
            CalendarString cs = new CalendarString(aar.getArDT());
            cs.setFormat("dd.MM.yyyy HH:mm");
            rowData[0] = cs;
            rowData[1] = df.format(aar.getAplus());
            rowData[2] = df.format(aar.getRplus());
            rowData[3] = aar.getArPeriod() + " мин";
            tm.addRow(rowData);
        }

        jTable3.scrollRectToVisible(new Rectangle(jTable3.getCellRect(jTable3.getRowCount() - 1, 0, true)));

        if (aars.size() > 0 && jTabbedPane1.getSelectedIndex() == 0) {
            jButton2.setEnabled(true);
        }
    }

    private void RefreshTable4(int IdInDB) {
        DefaultTableModel tm = (DefaultTableModel) jTable4.getModel();
        while (tm.getRowCount() > 0) {
            tm.removeRow(0);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");

        String[] resp = NetClient.sendGetMarkerTasks(IdInDB);
        if (!resp[1].isEmpty()) {
            for (String marker : resp[1].split("\n")) {
                String[] markerFields = marker.split("\001");
                Object[] rowData = new Object[4];

                rowData[0] = new IntString(Integer.valueOf(markerFields[0]), markerFields[1]);

                Calendar ca = new GregorianCalendar();
                ca.setTimeInMillis(Long.valueOf(markerFields[2]));
                rowData[1] = sdf.format(ca.getTime());

                String ap = markerFields[3].equals("-1") ? "---" : markerFields[3];
                rowData[2] = ap;

                String rp = markerFields[4].equals("-1") ? "---" : markerFields[4];
                rowData[3] = rp;

                tm.addRow(rowData);
            }
        }

        jTable4.scrollRectToVisible(new Rectangle(jTable4.getCellRect(jTable4.getRowCount() - 1, 0, true)));
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel3 = new javax.swing.JLabel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree();
        jPanel7 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTable3 = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jButton4 = new javax.swing.JButton();
        jScrollPane5 = new javax.swing.JScrollPane();
        jTable4 = new javax.swing.JTable();
        jButton5 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem6 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();
        jCheckBoxMenuItem1 = new javax.swing.JCheckBoxMenuItem();
        jMenu3 = new javax.swing.JMenu();
        jMenuItem3 = new javax.swing.JMenuItem();

        jLabel3.setText("jLabel3");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jSplitPane1.setBorder(null);
        jSplitPane1.setDividerLocation(250);

        jTree1.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                jTree1ValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(jTree1);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 627, Short.MAX_VALUE)
        );

        jSplitPane1.setLeftComponent(jPanel1);

        jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jTabbedPane1StateChanged(evt);
            }
        });

        jTable3.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jTable3.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane4.setViewportView(jTable3);

        jLabel2.setText("с");

        jLabel4.setText(" ... ");
        jLabel4.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jLabel4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel4MouseClicked(evt);
            }
        });

        jLabel5.setText("по");

        jLabel6.setText(" ... ");
        jLabel6.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jLabel6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel6MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel6)
                        .addGap(0, 634, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 483, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Данные по часам", jPanel2);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jTable1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane2.setViewportView(jTable1);

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jTable2.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane3.setViewportView(jTable2);

        jLabel7.setText("с");

        jLabel8.setText(" ... ");
        jLabel8.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jLabel8.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel8MouseClicked(evt);
            }
        });

        jLabel9.setText("по");

        jLabel10.setText(" ... ");
        jLabel10.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jLabel10.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel10MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 711, Short.MAX_VALUE)
                    .addComponent(jScrollPane3)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel10)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jLabel8)
                    .addComponent(jLabel9)
                    .addComponent(jLabel10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Накопленная энергия", jPanel3);

        jButton4.setText("Создать отсечку по расписанию");
        jButton4.setEnabled(false);
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jTable4.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jTable4.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane5.setViewportView(jTable4);

        jButton5.setText("Убрать отсечку");
        jButton5.setEnabled(false);
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jButton4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton5)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 711, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton4)
                    .addComponent(jButton5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 477, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Отсечки по расписанию", jPanel6);

        jLabel1.setText("---");

        jButton1.setText("Обновить");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("График");
        jButton2.setEnabled(false);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("Диаграмма");
        jButton3.setEnabled(false);
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton3)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2)
                    .addComponent(jButton3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1))
        );

        jSplitPane1.setRightComponent(jPanel7);

        jMenu1.setText("Файл");

        jMenuItem1.setText("Выход");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Сервис");

        jMenuItem4.setText("Перечитать список счетчиков");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem4);

        jMenuItem6.setText("Группы счетчиков...");
        jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem6ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem6);

        jMenuItem2.setText("Настройки...");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem2);

        jMenuItem5.setText("Обновление программы...");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem5);

        jMenuBar1.add(jMenu2);

        jMenu4.setText("Вид");

        jCheckBoxMenuItem1.setText("Автоматически обновлять таблицы");
        jCheckBoxMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxMenuItem1ActionPerformed(evt);
            }
        });
        jMenu4.add(jCheckBoxMenuItem1);

        jMenuBar1.add(jMenu4);

        jMenu3.setText("Отчеты");

        jMenuItem3.setText("Отчет об электропотреблении за месяц...");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem3);

        jMenuBar1.add(jMenu3);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        dispose();
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        OptionsDialog dlg = new OptionsDialog(this, true);
        dlg.setLocationRelativeTo(null);
        dlg.setVisible(true);
        RefreshTree();
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jTree1ValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_jTree1ValueChanged
        TreePath tp = jTree1.getSelectionPath();
        if (tp == null) {
            return;
        }

        jButton2.setEnabled(false);

        DefaultMutableTreeNode tn = (DefaultMutableTreeNode) tp.getLastPathComponent();

        if (tn != null && tn.getUserObject().getClass() == MeterInfo.class) {
            MeterInfo mi = (MeterInfo) tn.getUserObject();
            if (mi.getOsv().equals("yes")) {
                jLabel1.setText("<html><font color='red'>Счетчик не опрашивается сервером! (" + mi.getGroup() + " / " + mi.getName() + ", серийный номер: " + mi.getSN() + ", Ki = " + mi.getKi() + ")</font></html>");
            } else {
                jLabel1.setText("Счетчик: " + mi.getGroup() + " / " + mi.getName() + ", серийный номер: " + mi.getSN() + ", Ki = " + mi.getKi());
            }
            if (jCheckBoxMenuItem1.isSelected()) {
                RefreshTable1(mi.getIdInDB());
                RefreshTable2(mi.getIdInDB());
                RefreshTable3(mi.getIdInDB());
                RefreshTable4(mi.getIdInDB());
            } else {
                DefaultTableModel tm1 = (DefaultTableModel) jTable1.getModel();
                while (tm1.getRowCount() > 0) {
                    tm1.removeRow(0);
                }
                DefaultTableModel tm2 = (DefaultTableModel) jTable2.getModel();
                while (tm2.getRowCount() > 0) {
                    tm2.removeRow(0);
                }
                DefaultTableModel tm3 = (DefaultTableModel) jTable3.getModel();
                while (tm3.getRowCount() > 0) {
                    tm3.removeRow(0);
                }
                DefaultTableModel tm4 = (DefaultTableModel) jTable4.getModel();
                while (tm4.getRowCount() > 0) {
                    tm4.removeRow(0);
                }
                //jButton1.setEnabled(true);
            }
            jButton3.setEnabled(true);
            jButton4.setEnabled(true);
        }

        if (tn == null || tn.getUserObject().getClass() == String.class) {
            jLabel1.setText("---");
            //jButton1.setEnabled(false);
            jButton3.setEnabled(false);
            jButton4.setEnabled(false);
            DefaultTableModel tm1 = (DefaultTableModel) jTable1.getModel();
            while (tm1.getRowCount() > 0) {
                tm1.removeRow(0);
            }
            DefaultTableModel tm2 = (DefaultTableModel) jTable2.getModel();
            while (tm2.getRowCount() > 0) {
                tm2.removeRow(0);
            }
            DefaultTableModel tm3 = (DefaultTableModel) jTable3.getModel();
            while (tm3.getRowCount() > 0) {
                tm3.removeRow(0);
            }
            DefaultTableModel tm4 = (DefaultTableModel) jTable4.getModel();
            while (tm4.getRowCount() > 0) {
                tm4.removeRow(0);
            }
        }
    }//GEN-LAST:event_jTree1ValueChanged

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        MonthReportDialog dlg = new MonthReportDialog(this, true);
        dlg.setLocationRelativeTo(null);
        dlg.setVisible(true);
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jLabel4MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel4MouseClicked
        CalendarDialog cdlg = new CalendarDialog(this, "ru");
        Calendar ca = cdlg.getCalendar();
        if (ca != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
            jLabel4.setText(" " + sdf.format(ca.getTime()) + " ");
            if (!jLabel1.getText().equals("---") && jCheckBoxMenuItem1.isSelected()) {
                TreePath tp = jTree1.getSelectionPath();
                if (tp != null) {
                    DefaultMutableTreeNode tn = (DefaultMutableTreeNode) tp.getLastPathComponent();
                    if (tn != null && tn.getUserObject().getClass() == MeterInfo.class) {
                        MeterInfo mi = (MeterInfo) tn.getUserObject();
                        RefreshTable3(mi.getIdInDB());
                    }
                }
            }
        }
    }//GEN-LAST:event_jLabel4MouseClicked

    private void jLabel6MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel6MouseClicked
        CalendarDialog cdlg = new CalendarDialog(this, "ru");
        Calendar ca = cdlg.getCalendar();
        if (ca != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
            jLabel6.setText(" " + sdf.format(ca.getTime()) + " ");
            if (!jLabel1.getText().equals("---") && jCheckBoxMenuItem1.isSelected()) {
                TreePath tp = jTree1.getSelectionPath();
                if (tp != null) {
                    DefaultMutableTreeNode tn = (DefaultMutableTreeNode) tp.getLastPathComponent();
                    if (tn != null && tn.getUserObject().getClass() == MeterInfo.class) {
                        MeterInfo mi = (MeterInfo) tn.getUserObject();
                        RefreshTable3(mi.getIdInDB());
                    }
                }
            }
        }
    }//GEN-LAST:event_jLabel6MouseClicked

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        TreePath tp = jTree1.getSelectionPath();
        if (tp != null) {
            DefaultMutableTreeNode tn = (DefaultMutableTreeNode) tp.getLastPathComponent();
            if (tn != null && tn.getUserObject().getClass() == MeterInfo.class) {
                MeterInfo mi = (MeterInfo) tn.getUserObject();
                RefreshTable1(mi.getIdInDB());
                RefreshTable2(mi.getIdInDB());
                RefreshTable3(mi.getIdInDB());
                RefreshTable4(mi.getIdInDB());
            }
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        if (jTable3.getRowCount() == 0) {
            return;
        }

        if (jTabbedPane1.getSelectedIndex() == 0) {
            TreeMap<Calendar, Double> tAp = new TreeMap<>();
            TreeMap<Calendar, Double> tRp = new TreeMap<>();

            for (int rc = 0; rc < jTable3.getRowCount(); rc++) {
                CalendarString cs = (CalendarString) jTable3.getValueAt(rc, 0);

                Object vr1 = jTable3.getValueAt(rc, 1);
                double Ap = Double.valueOf(vr1.toString().replace(',', '.'));

                Object vr2 = jTable3.getValueAt(rc, 2);
                double Rp = Double.valueOf(vr2.toString().replace(',', '.'));

                tAp.put(cs, Ap);
                tRp.put(cs, Rp);
            }

            AvgArsChartDialog dlg = new AvgArsChartDialog(this, tAp, tRp, jLabel1.getText());
            dlg.setLocationRelativeTo(null);
            dlg.setVisible(true);
        }

        if (jTabbedPane1.getSelectedIndex() == 1) {
            TreeMap<Calendar, Double> tAp = new TreeMap<>();
            TreeMap<Calendar, Double> tRp = new TreeMap<>();

            for (int rc = 0; rc < jTable1.getRowCount(); rc++) {
                CalendarString cs = (CalendarString) jTable1.getValueAt(rc, 0);

                Object vr1 = jTable1.getValueAt(rc, 3);
                double Ap = Double.valueOf(vr1.toString().replace(',', '.'));

                Object vr2 = jTable1.getValueAt(rc, 4);
                double Rp = Double.valueOf(vr2.toString().replace(',', '.'));

                tAp.put(cs, Ap);
                tRp.put(cs, Rp);
            }

            AvgArsChartDialog dlg = new AvgArsChartDialog(this, tAp, tRp, jLabel1.getText());
            dlg.setLocationRelativeTo(null);
            dlg.setVisible(true);
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jCheckBoxMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxMenuItem1ActionPerformed
        if (jCheckBoxMenuItem1.isSelected()) {
            //jButton1.setEnabled(false);
            if (!jLabel1.getText().equals("---")) {
                TreePath tp = jTree1.getSelectionPath();
                if (tp != null) {
                    DefaultMutableTreeNode tn = (DefaultMutableTreeNode) tp.getLastPathComponent();
                    if (tn != null && tn.getUserObject().getClass() == MeterInfo.class) {
                        MeterInfo mi = (MeterInfo) tn.getUserObject();
                        RefreshTable1(mi.getIdInDB());
                        RefreshTable2(mi.getIdInDB());
                        RefreshTable3(mi.getIdInDB());
                        RefreshTable4(mi.getIdInDB());
                    }
                }
            }
        } else {
            if (!jLabel1.getText().equals("---")) {
                //jButton1.setEnabled(true);
            }
        }
        Vars.prop.setProperty("AutoT3", String.valueOf(jCheckBoxMenuItem1.isSelected()));
        Vars.SaveProperties();
    }//GEN-LAST:event_jCheckBoxMenuItem1ActionPerformed

    private void jTabbedPane1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jTabbedPane1StateChanged
        jButton2.setEnabled(false);
        if (jTabbedPane1.getSelectedIndex() == 0 && jTable3.getRowCount() > 0) {
            jButton2.setEnabled(true);
        }
        if (jTabbedPane1.getSelectedIndex() == 1 && jTable1.getRowCount() > 0) {
            jButton2.setEnabled(true);
        }

        TreePath tp = jTree1.getSelectionPath();
        if (tp != null) {
            DefaultMutableTreeNode tn = (DefaultMutableTreeNode) tp.getLastPathComponent();
            if (tn != null && tn.getUserObject().getClass() == MeterInfo.class) {
                if (!jCheckBoxMenuItem1.isSelected()) {
                    //jButton1.setEnabled(true);
                }
            }
            if (tn == null || tn.getUserObject().getClass() == String.class) {
                //jButton1.setEnabled(false);
            }
        } else {
            //jButton1.setEnabled(false);
        }
    }//GEN-LAST:event_jTabbedPane1StateChanged

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        RefreshTree();
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        TreePath tp = jTree1.getSelectionPath();
        if (tp != null) {
            DefaultMutableTreeNode tn = (DefaultMutableTreeNode) tp.getLastPathComponent();
            if (tn != null && tn.getUserObject().getClass() == MeterInfo.class) {
                MeterInfo mi = (MeterInfo) tn.getUserObject();
                DiagramDialog dlg = new DiagramDialog(this, true, mi);
                dlg.setLocationRelativeTo(null);
                dlg.setVisible(true);
            }
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
        ProgramUpdateDialog dlg = new ProgramUpdateDialog(this, true);
        dlg.setLocationRelativeTo(null);
        dlg.setVisible(true);
    }//GEN-LAST:event_jMenuItem5ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        TreePath tp = jTree1.getSelectionPath();
        if (tp != null) {
            DefaultMutableTreeNode tn = (DefaultMutableTreeNode) tp.getLastPathComponent();
            if (tn != null && tn.getUserObject().getClass() == MeterInfo.class) {
                final MeterInfo mi = (MeterInfo) tn.getUserObject();
                AddMarkerDialog dlg = new AddMarkerDialog(this, mi.getIdInDB());
                dlg.setLocationRelativeTo(null);
                dlg.setVisible(true);
                RefreshTable4(mi.getIdInDB());
            }
        }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem6ActionPerformed
        GroupsDialog dlg = new GroupsDialog(this, true);
        dlg.setLocationRelativeTo(null);
        dlg.setVisible(true);
        RefreshTree();
    }//GEN-LAST:event_jMenuItem6ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        TreePath tp = jTree1.getSelectionPath();
        if (tp != null) {
            DefaultMutableTreeNode tn = (DefaultMutableTreeNode) tp.getLastPathComponent();
            if (tn != null && tn.getUserObject().getClass() == MeterInfo.class) {
                if (jTable4.getSelectedRow() > -1) {
                    if (JOptionPane.showConfirmDialog(this, "Вы уверены, что хотите удалить эту отсечку?", "Подтверждение", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                        IntString is = (IntString) jTable4.getValueAt(jTable4.getSelectedRow(), 0);
                        NetClient.sendRemoveMarker(is.getInt());
                        MeterInfo mi = (MeterInfo) tn.getUserObject();
                        RefreshTable4(mi.getIdInDB());
                    }
                }
            }
        }
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jLabel8MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel8MouseClicked
        CalendarDialog cdlg = new CalendarDialog(this, "ru");
        Calendar ca = cdlg.getCalendar();
        if (ca != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
            jLabel8.setText(" " + sdf.format(ca.getTime()) + " ");
            if (!jLabel1.getText().equals("---") && jCheckBoxMenuItem1.isSelected()) {
                TreePath tp = jTree1.getSelectionPath();
                if (tp != null) {
                    DefaultMutableTreeNode tn = (DefaultMutableTreeNode) tp.getLastPathComponent();
                    if (tn != null && tn.getUserObject().getClass() == MeterInfo.class) {
                        MeterInfo mi = (MeterInfo) tn.getUserObject();
                        RefreshTable1(mi.getIdInDB());
                    }
                }
            }
        }
    }//GEN-LAST:event_jLabel8MouseClicked

    private void jLabel10MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel10MouseClicked
        CalendarDialog cdlg = new CalendarDialog(this, "ru");
        Calendar ca = cdlg.getCalendar();
        if (ca != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
            jLabel10.setText(" " + sdf.format(ca.getTime()) + " ");
            if (!jLabel1.getText().equals("---") && jCheckBoxMenuItem1.isSelected()) {
                TreePath tp = jTree1.getSelectionPath();
                if (tp != null) {
                    DefaultMutableTreeNode tn = (DefaultMutableTreeNode) tp.getLastPathComponent();
                    if (tn != null && tn.getUserObject().getClass() == MeterInfo.class) {
                        MeterInfo mi = (MeterInfo) tn.getUserObject();
                        RefreshTable1(mi.getIdInDB());
                    }
                }
            }
        }
    }//GEN-LAST:event_jLabel10MouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTable jTable3;
    private javax.swing.JTable jTable4;
    private javax.swing.JTree jTree1;
    // End of variables declaration//GEN-END:variables
}
