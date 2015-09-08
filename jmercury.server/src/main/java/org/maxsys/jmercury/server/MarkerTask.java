package org.maxsys.jmercury.server;

import java.util.Calendar;
import org.maxsys.dblib.PDM;

public class MarkerTask implements Runnable {

    EMeter em;
    String markerlong;

    public MarkerTask(EMeter em, String markerlong) {
        this.em = em;
        this.markerlong = markerlong;
    }

    @Override
    public void run() {
        em.setMeterFlag("statusstr", "MarkerTask");

        PDM pdm = new PDM();
        Object ko = pdm.getScalar("em", "SELECT k FROM markers WHERE "
                + "meter_id = " + em.getIdInDB()
                + " AND timelong = " + this.markerlong
                + " AND hide = 0");
        if (ko != null) {
            Integer k = (Integer) ko;
            AplusRplus aprp = em.getAplusRplusFromReset();
            Calendar mtc = em.getMeterTime();
            pdm.executeNonQueryUpdate("em", "UPDATE allmetersdb.markers SET "
                    + "`timelong` = " + mtc.getTimeInMillis()
                    + ", `Aplus` = " + aprp.getAplus()
                    + ", `Rplus` = " + aprp.getRplus()
                    + " WHERE k = " + k);
        }

        String emmt = em.getMeterFlag("MarkerTask") == null ? "" : em.getMeterFlag("MarkerTask");
        if (!emmt.isEmpty()) {
            String[] emmts = emmt.split(",");
            if (emmts.length > 0) {
                StringBuilder sb = new StringBuilder();
                for (String mt : emmts) {
                    if (!mt.equals(markerlong)) {
                        sb.append(mt).append(",");
                    }
                }
                em.setMeterFlag("MarkerTask", sb.toString());
            }
        }

        em.setMeterFlag("busy", "no");
        em.setMeterFlag("preosv", "no");
        em.setMeterFlag("osv", "no");
        em.setMeterFlag("statusstr", "");
        MeterServer.SaveMeterState(em);
        STL.Log("MeterServer: " + em.getMeterName() + " - MarkerTask завершен.");
    }
}
