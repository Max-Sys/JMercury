package org.maxsys.jmercury.server;

import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.maxsys.dblib.PDM;

public class MeterServer implements Runnable {

    private boolean isMsvrRunning = true;
    private boolean isMsvrPaused = true;

    @Override
    public void run() {
        STL.Log("MeterServer: is running!");
        while (isMsvrRunning) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (isMsvrPaused) {
                for (EMeter em : Vars.meters.values()) {
                    String busy = em.getMeterFlag("busy") == null ? "no" : em.getMeterFlag("busy");
                    if (busy.equals("yes")) {
                        String statusstr = em.getMeterFlag("statusstr") == null ? "" : em.getMeterFlag("statusstr");
                        if (statusstr.isEmpty()) {
                            em.setStatus("Busy (tte = ?)");
                        } else {
                            em.setStatus("Busy (" + statusstr + ")");
                        }
                    } else {
                        em.setStatus("---");
                    }
                }
                continue;
            }

            for (final EMeter em : Vars.meters.values()) {

                // Out of service?
                String osv = em.getMeterFlag("osv") == null ? "no" : em.getMeterFlag("osv");
                if (osv.equals("yes")) {
                    //STL.Log(em.getMeterName() + " is out of service!");
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
                        String preosv = em.getMeterFlag("preosv") == null ? "no" : em.getMeterFlag("preosv");
                        if (preosv.equals("no")) {
                            STL.Log("MeterServer: " + em.getMeterName() + " - preosv!");
                            em.setMeterFlag("preosv", "yes");
                            em.setMeterFlag("busy", "no");
                        } else {
                            STL.Log("MeterServer: " + em.getMeterName() + " - osv!");
                            em.setMeterFlag("busy", "no");
                            em.setMeterFlag("preosv", "no");
                            em.setMeterFlag("osv", "yes");
                        }
                        SaveMeterState(em);
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
                    //STL.Log(em.getMeterName() + " AvgARsTask is ON - " + (AvgARsTask_t + AvgARsTask_i) + " ? " + Calendar.getInstance().getTimeInMillis() + " ? " + ((AvgARsTask_t + AvgARsTask_i) - Calendar.getInstance().getTimeInMillis()));
                    AvgARsTask_secs = ((AvgARsTask_t + AvgARsTask_i) - Calendar.getInstance().getTimeInMillis()) / 1000;
                    if ((AvgARsTask_t + AvgARsTask_i) < Calendar.getInstance().getTimeInMillis()) {
                        STL.Log("MeterServer: " + em.getMeterName() + " - запуск AvgARsTask...");
                        //STL.Log("Время последнего старта - " + AvgARsTask_t);
                        //STL.Log("Интервал - " + AvgARsTask_i);

                        em.setMeterFlag("busy", "yes");
                        em.setMeterFlag("busy_timer", "60");

                        Thread thr = new Thread(new AvgARsTask(em));
                        thr.start();

                        //STL.Log(em.getMeterName() + " - AvgARsTask запущен.");
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
                    //System.out.println((em.getMeterName() + " DaysTask is ON - " + (DaysTask_t + DaysTask_i) + " ? " + Calendar.getInstance().getTimeInMillis() + " ? " + ((DaysTask_t + DaysTask_i) - Calendar.getInstance().getTimeInMillis()) / 1000));
                    DaysTask_secs = ((DaysTask_t + DaysTask_i) - Calendar.getInstance().getTimeInMillis()) / 1000;
                    if ((DaysTask_t + DaysTask_i) < Calendar.getInstance().getTimeInMillis()) {
                        STL.Log("MeterServer: " + em.getMeterName() + " - запуск DaysTask...");
                        //STL.Log("Время последнего старта - " + DaysTask_t);
                        //STL.Log("Интервал - " + DaysTask_i);

                        em.setMeterFlag("busy", "yes");
                        em.setMeterFlag("busy_timer", "30");
                        SaveMeterState(em);

                        Thread thr = new Thread(new DaysTask(em));
                        thr.start();

                        //STL.Log("MeterServer: " + em.getMeterName() + " - DaysTask запущен.");
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
                    //STL.Log(em.getMeterName() + " MonthTask is ON - " + (MonthTask_t + MonthTask_i) + " ? " + Calendar.getInstance().getTimeInMillis() + " ? " + ((MonthTask_t + MonthTask_i) - Calendar.getInstance().getTimeInMillis()));
                    MonthTask_secs = ((MonthTask_t + MonthTask_i) - Calendar.getInstance().getTimeInMillis()) / 1000;
                    if ((MonthTask_t + MonthTask_i) < Calendar.getInstance().getTimeInMillis()) {
                        STL.Log("MeterServer: " + em.getMeterName() + " - запуск MonthTask...");
                        //STL.Log("Время последнего старта - " + MonthTask_t);
                        //STL.Log("Интервал - " + MonthTask_i);

                        em.setMeterFlag("busy", "yes");
                        em.setMeterFlag("busy_timer", "30");
                        SaveMeterState(em);

                        Thread thr = new Thread(new MonthTask(em));
                        thr.start();

                        STL.Log("MeterServer: " + em.getMeterName() + " - MonthTask запущен.");

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
        STL.Log("MeterServer: is closed!");
    }

    public void setMsvrRunning(boolean isMsvrRunning) {
        this.isMsvrRunning = isMsvrRunning;
    }

    public void setMsvrPaused(boolean isMsvrPaused) {
        this.isMsvrPaused = isMsvrPaused;
        if (this.isMsvrPaused) {
            STL.Log("MeterServer: is paused.");
        } else {
            STL.Log("MeterServer: is unpaused.");
        }
    }

    public boolean isIsMsvrRunning() {
        return isMsvrRunning;
    }

    public boolean isIsMsvrPaused() {
        return isMsvrPaused;
    }

    public static void SaveMeterState(EMeter meter) {
        PDM pdm = new PDM();
        pdm.executeNonQueryUpdate("em", "UPDATE meters SET flags = '" + meter.getMeterFlags() + "' WHERE k = " + meter.getIdInDB());
    }
}
