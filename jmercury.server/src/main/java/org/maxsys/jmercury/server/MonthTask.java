package org.maxsys.jmercury.server;

import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.maxsys.dblib.PDM;

public class MonthTask implements Runnable {

    EMeter em;

    public MonthTask(EMeter meter) {
        this.em = meter;
    }

    @Override
    public void run() {
        em.setMeterFlag("statusstr", "m ApRpMonth");

        Calendar mca = em.getMeterTime();
        if (mca == null) {
            STL.Log("MeterServer: " + em.getMeterName() + " ошибка чтения (MonthTask, em.getMeterTime() - 1)");
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                Logger.getLogger(MonthTask.class.getName()).log(Level.SEVERE, null, ex);
            }
            mca = em.getMeterTime();
            if (mca == null) {
                STL.Log("MeterServer: " + em.getMeterName() + " ошибка чтения (MonthTask, em.getMeterTime() - 2, return)");
                return;
            }
        }
        int mYear = mca.get(Calendar.YEAR);
        int mMonth = mca.get(Calendar.MONTH) + 1;

        PDM pdm = new PDM();
        Object ko = pdm.getScalar("em", "SELECT k FROM monthdata"
                + " WHERE hide = 0"
                + " AND meter_id = " + em.getIdInDB()
                + " AND YEAR(monthDT) = " + mYear
                + " AND MONTH(monthDT) = " + mMonth);

        if (ko == null) {
            AplusRplus aprp = em.getAplusRplusMonthBegining(mMonth);
            double AplusOnBeg = aprp.getAplus();
            double RplusOnBeg = aprp.getRplus();

            aprp = em.getAplusRplusMonth(mMonth);
            double Aplus = aprp.getAplus();
            double Rplus = aprp.getRplus();

            double AplusOnEnd = AplusOnBeg + Aplus;
            double RplusOnEnd = RplusOnBeg + Rplus;

            String monthDT = PDM.getDTStringDateOnly(mca);

            int meterID = em.getIdInDB();

            pdm.executeNonQuery("em", "INSERT INTO monthdata "
                    + "(meter_id, Aplus, Rplus, AplusOnBeg, RplusOnBeg, AplusOnEnd, RplusOnEnd, monthDT, hide) VALUES "
                    + "(" + meterID
                    + ", " + Aplus
                    + ", " + Rplus
                    + ", " + AplusOnBeg
                    + ", " + RplusOnBeg
                    + ", " + AplusOnEnd
                    + ", " + RplusOnEnd
                    + ", '" + monthDT + "'"
                    + ", false)");

            mMonth--;
            if (mMonth == 0) {
                mMonth = 12;
            }
            ko = pdm.getScalar("em", "SELECT k FROM monthdata"
                    + " WHERE hide = 0"
                    + " AND meter_id = " + em.getIdInDB()
                    + " AND YEAR(monthDT) = " + mYear
                    + " AND MONTH(monthDT) = " + mMonth);
            if (ko != null) {
                int k = (int) ko;

                aprp = em.getAplusRplusMonth(mMonth);
                Aplus = aprp.getAplus();
                Rplus = aprp.getRplus();

                pdm.executeNonQuery("em", "UPDATE monthdata SET"
                        + " `Aplus` = " + Aplus
                        + ", `Rplus` = " + Rplus
                        + ", `AplusOnEnd` = " + AplusOnBeg
                        + ", `RplusOnEnd` = " + RplusOnBeg
                        + " WHERE k = " + k);
            }
        } else {
            int k = (int) ko;

            AplusRplus aprp = em.getAplusRplusMonthBegining(mMonth);
            if (aprp == null) {
                STL.Log("MeterServer: " + em.getMeterName() + " ошибка чтения (MonthTask, em.getAplusRplusMonthBegining(mMonth) - 1)");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Logger.getLogger(MonthTask.class.getName()).log(Level.SEVERE, null, ex);
                }
                aprp = em.getAplusRplusMonthBegining(mMonth);
                if (aprp == null) {
                    STL.Log("MeterServer: " + em.getMeterName() + " ошибка чтения (MonthTask, em.getAplusRplusMonthBegining(mMonth) - 2, return)");
                    return;
                }
            }

            double AplusOnBeg = aprp.getAplus();
            double RplusOnBeg = aprp.getRplus();

            aprp = em.getAplusRplusMonth(mMonth);
            if (aprp == null) {
                STL.Log("MeterServer: " + em.getMeterName() + " ошибка чтения (MonthTask, em.getAplusRplusMonth(mMonth) - 1)");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Logger.getLogger(MonthTask.class.getName()).log(Level.SEVERE, null, ex);
                }
                aprp = em.getAplusRplusMonth(mMonth);
                if (aprp == null) {
                    STL.Log("MeterServer: " + em.getMeterName() + " ошибка чтения (MonthTask, em.getAplusRplusMonth(mMonth) - 2, return)");
                    return;
                }
            }

            double Aplus = aprp.getAplus();
            double Rplus = aprp.getRplus();

            double AplusOnEnd = AplusOnBeg + Aplus;
            double RplusOnEnd = RplusOnBeg + Rplus;

            String monthDT = PDM.getDTStringDateOnly(mca);

            pdm.executeNonQuery("em", "UPDATE monthdata SET"
                    + " `Aplus` = " + Aplus
                    + ", `Rplus` = " + Rplus
                    + ", `AplusOnEnd` = " + AplusOnEnd
                    + ", `RplusOnEnd` = " + RplusOnEnd
                    + ", `monthDT` = '" + monthDT + "'"
                    + " WHERE k = " + k);
        }

        em.setMeterFlag("busy", "no");
        em.setMeterFlag("preosv", "no");
        em.setMeterFlag("osv", "no");
        em.setMeterFlag("MonthTask_t", String.valueOf(Calendar.getInstance().getTimeInMillis()));
        em.setMeterFlag("statusstr", "");
        MeterServer.SaveMeterState(em);
        STL.Log("MeterServer: " + em.getMeterName() + " - MonthTask завершен.");
    }
}
