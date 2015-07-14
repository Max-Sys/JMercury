package org.maxsys.jmercury.server;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.maxsys.dblib.PDM;

public class MeterServer implements Runnable {

    private boolean isMsvrRunning = true;
    private boolean isMsvrPaused = true;

    @Override
    public void run() {
        System.out.println("MeterServer is running!");
        while (isMsvrRunning) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (isMsvrPaused) {
                for (EMeter em : Vars.meters.values()) {
                    em.setStatus("---");
                }
                continue;
            }

            for (final EMeter em : Vars.meters.values()) {

                // Out of service?
                String osv = em.getMeterFlag("osv") == null ? "no" : em.getMeterFlag("osv");
                if (osv.equals("yes")) {
                    //System.out.println(em.getMeterName() + " is out of service!");
                    em.setStatus("Out of service!");
                    continue;
                }

                // Busy?
                String busy = em.getMeterFlag("busy") == null ? "no" : em.getMeterFlag("busy");
                if (busy.equals("yes")) {
                    String busy_timer = em.getMeterFlag("busy_timer") == null ? "0" : em.getMeterFlag("busy_timer");
                    Integer busytimer = Integer.valueOf(busy_timer); // busy_timer поддерживается на уровне > 0 во время выполнения задачи.

                    String statusstr = em.getMeterFlag("statusstr") == null ? "" : em.getMeterFlag("statusstr");
                    if (statusstr.isEmpty()) {
                        em.setStatus("Busy (tte = " + busytimer + ")");
                    } else {
                        em.setStatus("Busy (" + statusstr + ")");
                    }

                    if (busytimer > 0) {
                        em.setMeterFlag("busy_timer", String.valueOf(busytimer - 1));
                        continue;
                    } else {
                        // Надо вставить проверки - не повисла ли задача.
                        System.out.println(em.getMeterName() + " - osv!");
                        em.setMeterFlag("busy", "no");
                        em.setMeterFlag("osv", "yes"); // Out of service
                        continue;
                    }
                }

                long DaysTask_secs = 1000000;
                long AvgARsTask_secs = 1000000;
                long MonthTask_secs = 1000000;

                // Tasks: task1 - on/off, task1_t - время завершения предыдущей работы (mills), task1_i - интервал (mills)
                // AvgARsTask
                String AvgARsTask = em.getMeterFlag("AvgARsTask") == null ? "off" : em.getMeterFlag("AvgARsTask");
                String s_AvgARsTask_t = em.getMeterFlag("AvgARsTask_t") == null ? "0" : em.getMeterFlag("AvgARsTask_t");
                long AvgARsTask_t = Long.valueOf(s_AvgARsTask_t);
                String s_AvgARsTask_i = em.getMeterFlag("AvgARsTask_i") == null ? "0" : em.getMeterFlag("AvgARsTask_i");
                long AvgARsTask_i = Long.valueOf(s_AvgARsTask_i);
                if (AvgARsTask.equals("on") && AvgARsTask_t != 0 && AvgARsTask_i != 0) {
                    //System.out.println(em.getMeterName() + " AvgARsTask is ON - " + (AvgARsTask_t + AvgARsTask_i) + " ? " + Calendar.getInstance().getTimeInMillis() + " ? " + ((AvgARsTask_t + AvgARsTask_i) - Calendar.getInstance().getTimeInMillis()));
                    AvgARsTask_secs = ((AvgARsTask_t + AvgARsTask_i) - Calendar.getInstance().getTimeInMillis()) / 1000;
                    if ((AvgARsTask_t + AvgARsTask_i) < Calendar.getInstance().getTimeInMillis()) {
                        System.out.println(em.getMeterName() + " - запуск AvgARsTask...");
                        System.out.println("Время последнего старта - " + AvgARsTask_t);
                        System.out.println("Интервал - " + AvgARsTask_i);

                        em.setMeterFlag("busy", "yes");
                        em.setMeterFlag("busy_timer", "60");

                        Thread thr = new Thread(new Runnable() {

                            @Override
                            public void run() {
                                PDM pdm = new PDM();

                                Calendar lastDTinDB = PDM.getCalendarFromTime((Timestamp) pdm.getScalar("em", "SELECT arDT FROM avgars WHERE meter_id = " + em.getIdInDB() + " AND hide = 0 ORDER BY arDT DESC LIMIT 1"));
                                long lTimeDiff = 0;
                                if (lastDTinDB != null) {
                                    lTimeDiff = (em.getAvgARLast().getArDT().getTimeInMillis() - lastDTinDB.getTimeInMillis());
                                    lTimeDiff = Math.abs(lTimeDiff);
                                    if (lTimeDiff > 2592000000l) {
                                        lastDTinDB = null;
                                    }
                                } else {
                                    em.getAvgARLast();
                                }
                                em.getAvgARNext();
                                int AvgARsCounter = 2000;
                                int FirstK = 0;

                                while (AvgARsCounter > 0) {
                                    AvgAR avgar = null;
                                    int errs = 0;
                                    while (errs < 5) {
                                        try {
                                            em.setMeterFlag("busy_timer", "60");

                                            avgar = em.getAvgARPrev();

                                            if (avgar == null) {
                                                errs++;
                                                System.out.println(em.getMeterName() + " errs++");
                                                continue;
                                            }

                                            errs = 0;
                                            break;
                                        } catch (Exception ex) {
                                            errs++;
                                        }
                                    }
                                    if (errs != 0) {
                                        System.out.println(em.getMeterName() + ": ERR!!!ERR!!!ERR!!!");
                                        return;
                                    }

                                    if (lastDTinDB == null) {
                                        if (avgar == null || avgar.getArPeriod() == -1) {
                                            AvgARsCounter = 0;
                                        } else {
                                            AvgARsCounter--;
                                        }
                                    } else {
                                        if (avgar != null && lastDTinDB.compareTo(avgar.getArDT()) < 0) {
                                            if (lTimeDiff > 0) {
                                                long lpr = lTimeDiff - (avgar.getArDT().getTimeInMillis() - lastDTinDB.getTimeInMillis());
                                                int perc = (int) (lpr * 100 / lTimeDiff);
                                                if (perc > 100) {
                                                    perc = 100;
                                                }
                                                em.setMeterFlag("statusstr", "a " + perc + "%");
                                            } else {
                                                em.setMeterFlag("statusstr", "");
                                            }
                                            AvgARsCounter = 1;
                                        } else {
                                            AvgARsCounter = 0;
                                        }
                                    }

                                    if (avgar != null && avgar.getArPeriod() != -1 && AvgARsCounter > 0) {
                                        int newK = pdm.executeNonQueryAI("em", "INSERT INTO avgars "
                                                + "(meter_id, Aplus, Aminus, Rplus, Rminus, arPeriod, arDT, hide) VALUES "
                                                + "(" + em.getIdInDB() + ", "
                                                + avgar.getAplus() + ", "
                                                + avgar.getAminus() + ", "
                                                + avgar.getRplus() + ", "
                                                + avgar.getRminus() + ", "
                                                + avgar.getArPeriod() + ", "
                                                + "'" + PDM.getDTString(avgar.getArDT()) + "', "
                                                + "true)");
                                        if (FirstK == 0) {
                                            FirstK = newK;
                                        }
                                    }
                                }

                                if (FirstK != 0) {
                                    pdm.executeNonQuery("em", "UPDATE avgars SET hide = 0 WHERE meter_id = " + em.getIdInDB() + " AND k >= " + FirstK);
                                    System.out.println(em.getMeterName() + " - AvgARsTask завершен с записью данных");
                                } else {
                                    System.out.println(em.getMeterName() + " - AvgARsTask завершен.");
                                }

                                em.setMeterFlag("busy", "no");
                                em.setMeterFlag("AvgARsTask_t", String.valueOf(Calendar.getInstance().getTimeInMillis()));
                                em.setMeterFlag("statusstr", "");
                                SaveMeterState(em);
                            }
                        });
                        thr.start();

                        //System.out.println(em.getMeterName() + " - AvgARsTask запущен.");
                        continue;
                    }
                }

                // DaysTask
                String DaysTask = em.getMeterFlag("DaysTask") == null ? "off" : em.getMeterFlag("DaysTask");
                String s_DaysTask_t = em.getMeterFlag("DaysTask_t") == null ? "0" : em.getMeterFlag("DaysTask_t");
                long DaysTask_t = Long.valueOf(s_DaysTask_t);
                String s_DaysTask_i = em.getMeterFlag("DaysTask_i") == null ? "0" : em.getMeterFlag("DaysTask_i");
                long DaysTask_i = Long.valueOf(s_DaysTask_i);
                if (DaysTask.equals("on") && DaysTask_t != 0 && DaysTask_i != 0) {
                    //System.out.println(em.getMeterName() + " DaysTask is ON - " + (DaysTask_t + DaysTask_i) + " ? " + Calendar.getInstance().getTimeInMillis() + " ? " + ((DaysTask_t + DaysTask_i) - Calendar.getInstance().getTimeInMillis()) / 1000);
                    DaysTask_secs = ((DaysTask_t + DaysTask_i) - Calendar.getInstance().getTimeInMillis()) / 1000;
                    if ((DaysTask_t + DaysTask_i) < Calendar.getInstance().getTimeInMillis()) {
                        System.out.println(em.getMeterName() + " - запуск DaysTask...");
                        System.out.println("Время последнего старта - " + DaysTask_t);
                        System.out.println("Интервал - " + DaysTask_i);

                        em.setMeterFlag("busy", "yes");
                        em.setMeterFlag("busy_timer", "10");
                        SaveMeterState(em);

                        Thread thr = new Thread(new Runnable() {

                            @Override
                            public void run() {
                                for (int i = 0; i < 100; i++) {
                                    System.out.println(em.getMeterName() + " - DaysTask работает... " + i);
                                    em.setMeterFlag("statusstr", "d " + i + "%");
                                    try {
                                        Thread.sleep(250);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                    em.setMeterFlag("busy_timer", "10");
                                }
                                em.setMeterFlag("busy", "no");
                                em.setMeterFlag("DaysTask_t", String.valueOf(Calendar.getInstance().getTimeInMillis()));
                                em.setMeterFlag("statusstr", "");
                                SaveMeterState(em);
                                System.out.println(em.getMeterName() + " - DaysTask завершен.");
                            }
                        });
                        thr.start();

                        System.out.println(em.getMeterName() + " - DaysTask запущен.");

                        continue;
                    }
                }

                // MonthTask
                String MonthTask = em.getMeterFlag("MonthTask") == null ? "off" : em.getMeterFlag("MonthTask");
                String s_MonthTask_t = em.getMeterFlag("MonthTask_t") == null ? "0" : em.getMeterFlag("MonthTask_t");
                long MonthTask_t = Long.valueOf(s_MonthTask_t);
                String s_MonthTask_i = em.getMeterFlag("MonthTask_i") == null ? "0" : em.getMeterFlag("MonthTask_i");
                long MonthTask_i = Long.valueOf(s_MonthTask_i);
                if (MonthTask.equals("on") && MonthTask_t != 0 && MonthTask_i != 0) {
                    //System.out.println(em.getMeterName() + " MonthTask is ON - " + (MonthTask_t + MonthTask_i) + " ? " + Calendar.getInstance().getTimeInMillis() + " ? " + ((MonthTask_t + MonthTask_i) - Calendar.getInstance().getTimeInMillis()));
                    MonthTask_secs = ((MonthTask_t + MonthTask_i) - Calendar.getInstance().getTimeInMillis()) / 1000;
                    if ((MonthTask_t + MonthTask_i) < Calendar.getInstance().getTimeInMillis()) {
                        System.out.println(em.getMeterName() + " - запуск MonthTask...");
                        System.out.println("Время последнего старта - " + MonthTask_t);
                        System.out.println("Интервал - " + MonthTask_i);

                        em.setMeterFlag("busy", "yes");
                        em.setMeterFlag("busy_timer", "10");
                        SaveMeterState(em);

                        Thread thr = new Thread(new Runnable() {

                            @Override
                            public void run() {
                                for (int i = 0; i < 15; i++) {
                                    System.out.println(em.getMeterName() + " - MonthTask работает... " + i);
                                    try {
                                        Thread.sleep(500);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                    em.setMeterFlag("busy_timer", "10");
                                }
                                em.setMeterFlag("busy", "no");
                                em.setMeterFlag("MonthTask_t", String.valueOf(Calendar.getInstance().getTimeInMillis()));
                                SaveMeterState(em);
                                System.out.println(em.getMeterName() + " - MonthTask завершен.");
                            }
                        });
                        thr.start();

                        System.out.println(em.getMeterName() + " - MonthTask запущен.");

                        continue;
                    }
                }

                // Set status in table
                long secs = Math.min(Math.min(AvgARsTask_secs, DaysTask_secs), MonthTask_secs);
                if (secs == 1000000) {
                    em.setStatus("Standby (no tasks)");
                } else {
                    em.setStatus("Standby (" + secs + ")");
                }
            }
        }
        System.out.println("MeterServer is closed!");
    }

    public void setMsvrRunning(boolean isMsvrRunning) {
        this.isMsvrRunning = isMsvrRunning;
    }

    public void setMsvrPaused(boolean isMsvrPaused) {
        this.isMsvrPaused = isMsvrPaused;
        if (this.isMsvrPaused) {
            System.out.println("MeterServer is paused.");
        } else {
            System.out.println("MeterServer is unpaused.");
        }
    }

    public boolean isIsMsvrRunning() {
        return isMsvrRunning;
    }

    public boolean isIsMsvrPaused() {
        return isMsvrPaused;
    }

    private void SaveMeterState(EMeter meter) {
        PDM pdm = new PDM();
        pdm.executeNonQueryUpdate("em", "UPDATE meters SET flags = '" + meter.getMeterFlags() + "' WHERE k = " + meter.getIdInDB());
    }
}
