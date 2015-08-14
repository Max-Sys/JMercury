package org.maxsys.jmercury.server;

import java.sql.Timestamp;
import java.util.Calendar;
import org.maxsys.dblib.PDM;
import static org.maxsys.jmercury.server.MeterServer.SaveMeterState;

public class AvgARsTask implements Runnable {

    EMeter em;

    public AvgARsTask(EMeter em) {
        this.em = em;
    }

    @Override
    public void run() {
        PDM pdm = new PDM();

        Calendar lastDTinDB = PDM.getCalendarFromTime((Timestamp) pdm.getScalar("em", "SELECT arDT FROM avgars WHERE meter_id = " + em.getIdInDB() + " AND hide = 0 ORDER BY arDT DESC LIMIT 1"));
        long lTimeDiff = 0;
        if (lastDTinDB != null) {
            // Надо проверку на nullpointer!
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
                        STL.Log("MeterServer: " + em.getMeterName() + " ошибка чтения (getAvgARPrev(), errs++)");
                        continue;
                    }

                    if (errs != 0) {
                        STL.Log("MeterServer: " + em.getMeterName() + " прочитано, errs = 0");
                    }
                    errs = 0;
                    break;
                } catch (Exception ex) {
                    errs++;
                }
            }
            if (errs != 0) {
                STL.Log("MeterServer: " + em.getMeterName() + ": ошибка соединения со счетчиком!");
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
            STL.Log("MeterServer: " + em.getMeterName() + " - AvgARsTask завершен с записью данных");
        } else {
            STL.Log("MeterServer: " + em.getMeterName() + " - AvgARsTask завершен.");
        }

        em.setMeterFlag("busy", "no");
        em.setMeterFlag("preosv", "no");
        em.setMeterFlag("osv", "no");
        em.setMeterFlag("AvgARsTask_t", String.valueOf(Calendar.getInstance().getTimeInMillis()));
        em.setMeterFlag("statusstr", "");
        SaveMeterState(em);
    }
}
