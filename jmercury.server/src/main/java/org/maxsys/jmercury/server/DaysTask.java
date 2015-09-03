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
        Calendar canow = em.getMeterTime();
        canow.add(Calendar.DAY_OF_YEAR, -1);
        Object strs = pdm.getScalar("em", "SELECT * FROM daydata WHERE dayDT = '" + PDM.getDTStringDateOnly(canow) + "' AND meter_id = " + em.getIdInDB());
        if (strs == null) {
            em.setMeterFlag("statusstr", "d ApRpPrDay");
            AplusRplus aprp = em.getAplusRplusPrevDay();
            pdm.executeNonQuery("em", "INSERT INTO daydata "
                    + "(meter_id, Aplus, Rplus, dayDT, hide) "
                    + "VALUES (" + em.getIdInDB() + ", "
                    + aprp.getAplus() + ", "
                    + aprp.getRplus() + ", "
                    + "'" + PDM.getDTStringDateOnly(canow) + "', 0)");
            STL.Log("MeterServer: " + em.getMeterName() + " - DaysTask - запись данных PrevDay.");
        }

        // Заполнить недостающие данные за последние 3 месяца.
        canow.add(Calendar.MONTH, -3);
        int ra = 0;
        int dberr = 0;
        while (dberr < 15) {
            em.setMeterFlag("statusstr", "d p3m");
            ra = pdm.executeNonQueryUpdate("em",
                    "INSERT INTO daydata (meter_id, Aplus, Rplus, dayDT, hide) "
                    + "SELECT"
                    + " avgars.meter_id,"
                    + " IFNULL(SUM(avgars.Aplus) / (60 / avgars.arPeriod), 0) AS Aplus,"
                    + " IFNULL(SUM(avgars.Rplus) / (60 / avgars.arPeriod), 0) AS Rplus,"
                    + " DATE(avgars.arDT) AS dayDT,"
                    + " IF(COUNT(*) = 24 * (60 / avgars.arPeriod), 0, 1) AS hide "
                    + "FROM avgars "
                    + "WHERE"
                    + " avgars.meter_id = " + em.getIdInDB()
                    + " AND avgars.hide = 0"
                    + " AND DATE(avgars.arDT) >= '" + PDM.getDTStringDateOnly(canow) + "'"
                    + " AND DATE(avgars.arDT) NOT IN (SELECT dayDT FROM daydata WHERE meter_id = " + em.getIdInDB() + ") "
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
