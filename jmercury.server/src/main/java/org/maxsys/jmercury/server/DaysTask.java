package org.maxsys.jmercury.server;

import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.maxsys.dblib.PDM;

public class DaysTask implements Runnable {

    EMeter em;

    public DaysTask(EMeter em) {
        this.em = em;
    }

    @Override
    public void run() {
        PDM pdm = new PDM();

        // Заполнить за вчера.
        Calendar canow = em.getMeterTime();
        if (canow == null) {
            STL.Log("MeterServer: " + em.getMeterName() + " ошибка чтения (DaysTask, em.getMeterTime() - 1)");
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                Logger.getLogger(DaysTask.class.getName()).log(Level.SEVERE, null, ex);
            }
            canow = em.getMeterTime();
            if (canow == null) {
                STL.Log("MeterServer: " + em.getMeterName() + " ошибка чтения (DaysTask, em.getMeterTime() - 2, return)");
            }
        }
        canow.add(Calendar.DAY_OF_YEAR, -1);
        Object strs = pdm.getScalar("em", "SELECT COUNT(*) FROM daydata WHERE hide = 0 AND dayDT = '" + PDM.getDTStringDateOnly(canow) + "' AND meter_id = " + em.getIdInDB());
        if (strs != null) {
            em.setMeterFlag("statusstr", "d ApRpPrDay");

            AplusRplus aprp = em.getAplusRplusPrevDay();
            if (aprp == null) {
                aprp = em.getAplusRplusPrevDay();
                if (aprp == null) {
                    return;
                }
            }

            AplusRplus aprpb = em.getAplusRplusPrevDayBegining();
            if (aprpb == null) {
                aprpb = em.getAplusRplusPrevDayBegining();
                if (aprpb == null) {
                    return;
                }
            }

            if ((Long) strs == 0) {
                pdm.executeNonQuery("em", "INSERT INTO daydata "
                        + "(meter_id, Aplus, Rplus, AplusOnBeg, RplusOnBeg, dayDT, hide) "
                        + "VALUES (" + em.getIdInDB() + ", "
                        + aprp.getAplus() + ", "
                        + aprp.getRplus() + ", "
                        + aprpb.getAplus() + ", "
                        + aprpb.getRplus() + ", "
                        + "'" + PDM.getDTStringDateOnly(canow) + "', 0)");
                STL.Log("MeterServer: " + em.getMeterName() + " - DaysTask - запись данных PrevDay.");
            } else {
                Object ko = pdm.getScalar("em", "SELECT k FROM daydata WHERE hide = 0 AND dayDT = '" + PDM.getDTStringDateOnly(canow) + "' AND meter_id = " + em.getIdInDB());
                if (ko != null) {
                    int k = (int) ko;
                    pdm.executeNonQuery("em", "UPDATE daydata "
                            + "SET `Aplus` = " + aprp.getAplus()
                            + ", `Rplus` = " + aprp.getRplus()
                            + ", `AplusOnBeg` = " + aprpb.getAplus()
                            + ", `RplusOnBeg` = " + aprpb.getRplus()
                            + " WHERE k = " + k);
                    STL.Log("MeterServer: " + em.getMeterName() + " - DaysTask - обновление данных PrevDay.");
                }
            }
        }

        // Заполнить за сегодня.
        em.setMeterFlag("statusstr", "d ApRpNowDay");

        AplusRplus aprp = em.getAplusRplusNowDay();
        if (aprp == null) {
            aprp = em.getAplusRplusNowDay();
            if (aprp == null) {
                return;
            }
        }

        AplusRplus aprpb = em.getAplusRplusNowDayBegining();
        if (aprpb == null) {
            aprpb = em.getAplusRplusNowDayBegining();
            if (aprpb == null) {
                return;
            }
        }

        canow.add(Calendar.DAY_OF_YEAR, 1);
        Object ko = pdm.getScalar("em", "SELECT k FROM daydata WHERE hide = 0 AND dayDT = '" + PDM.getDTStringDateOnly(canow) + "' AND meter_id = " + em.getIdInDB());
        if (ko == null) {
            pdm.executeNonQuery("em", "INSERT INTO daydata "
                    + "(meter_id, Aplus, Rplus, AplusOnBeg, RplusOnBeg, dayDT, hide) "
                    + "VALUES (" + em.getIdInDB() + ", "
                    + aprp.getAplus() + ", "
                    + aprp.getRplus() + ", "
                    + aprpb.getAplus() + ", "
                    + aprpb.getRplus() + ", "
                    + "'" + PDM.getDTStringDateOnly(canow) + "', 0)");
            STL.Log("MeterServer: " + em.getMeterName() + " - DaysTask - запись данных NowDay.");
        } else {
            int k = (int) ko;
            pdm.executeNonQuery("em", "UPDATE daydata "
                    + "SET `Aplus` = " + aprp.getAplus()
                    + ", `Rplus` = " + aprp.getRplus()
                    + ", `AplusOnBeg` = " + aprpb.getAplus()
                    + ", `RplusOnBeg` = " + aprpb.getRplus()
                    + " WHERE k = " + k);
            STL.Log("MeterServer: " + em.getMeterName() + " - DaysTask - обновление данных NowDay.");
        }

        // Заполнить недостающие данные за последние 3 месяца.
        canow.add(Calendar.MONTH, -3);
        int ra = 0;
        int dberr = 0;
        while (dberr < 15) {
            em.setMeterFlag("statusstr", "d p3m");

            Object sc = pdm.getScalar("em", "SELECT"
                    + " avgars.meter_id,"
                    + " IF(COUNT(*) = 24 * (60 / avgars.arPeriod), 0, 1) AS hide "
                    + "FROM avgars "
                    + "WHERE"
                    + " avgars.meter_id = " + em.getIdInDB()
                    + " AND avgars.hide = 0"
                    + " AND DATE(avgars.arDT) >= '" + PDM.getDTStringDateOnly(canow) + "'"
                    + " AND DATE(avgars.arDT) NOT IN (SELECT dayDT FROM daydata WHERE meter_id = " + em.getIdInDB() + " AND hide = 0) "
                    + "GROUP BY DATE(avgars.arDT) HAVING hide = 0");

            if (sc != null) {
                ra = pdm.executeNonQueryUpdate("em",
                        "INSERT INTO daydata (meter_id, Aplus, Rplus, AplusOnBeg, RplusOnBeg, dayDT, hide) "
                        + "SELECT"
                        + " avgars.meter_id,"
                        + " IFNULL(SUM(avgars.Aplus) / (60 / avgars.arPeriod), 0) AS Aplus,"
                        + " IFNULL(SUM(avgars.Rplus) / (60 / avgars.arPeriod), 0) AS Rplus,"
                        + " 0 AS AplusOnBeg,"
                        + " 0 AS RplusOnBeg,"
                        + " DATE(avgars.arDT) AS dayDT,"
                        + " IF(COUNT(*) = 24 * (60 / avgars.arPeriod), 0, 1) AS hide "
                        + "FROM avgars "
                        + "WHERE"
                        + " avgars.meter_id = " + em.getIdInDB()
                        + " AND avgars.hide = 0"
                        + " AND DATE(avgars.arDT) >= '" + PDM.getDTStringDateOnly(canow) + "'"
                        + " AND DATE(avgars.arDT) NOT IN (SELECT dayDT FROM daydata WHERE meter_id = " + em.getIdInDB() + " AND hide = 0) "
                        + "GROUP BY DATE(avgars.arDT) HAVING hide = 0");

                if (ra == -1) {
                    dberr++;
                    STL.Log("MeterServer: " + em.getMeterName() + " - DaysTask - ошибка записи данных Prev 3 month. Пробуем еще раз... (" + dberr + ")");
                    em.setMeterFlag("busy_timer", "30");
                    em.setMeterFlag("statusstr", "d p3m wait " + dberr);
                    try {
                        Thread.sleep((long) (Math.random() * 10000));
                    } catch (InterruptedException ex) {
                        Logger.getLogger(MeterServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    dberr = 0;
                    break;
                }
            } else {
                dberr = 0;
                break;
            }
        }

        if (dberr == 0) {
            if (ra > 0) {
                STL.Log("MeterServer: " + em.getMeterName() + " - DaysTask - запись данных Prev 3 month (" + ra + ") rows.");
            }
        } else {
            STL.Log("MeterServer: " + em.getMeterName() + " - DaysTask - ошибка записи данных Prev 3 month!");
        }

        em.setMeterFlag("busy", "no");
        em.setMeterFlag("preosv", "no");
        em.setMeterFlag("osv", "no");
        em.setMeterFlag("DaysTask_t", String.valueOf(Calendar.getInstance().getTimeInMillis()));
        em.setMeterFlag("statusstr", "");
        MeterServer.SaveMeterState(em);
        STL.Log("MeterServer: " + em.getMeterName() + " - DaysTask завершен.");
    }
}
