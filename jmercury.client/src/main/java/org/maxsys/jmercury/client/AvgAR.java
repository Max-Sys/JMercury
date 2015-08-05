package org.maxsys.jmercury.client;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AvgAR {

    private final double Aplus;
    private final double Aminus;
    private final double Rplus;
    private final double Rminus;
    private final Calendar arDT;
    private final int arPeriod;

    public AvgAR(double Aplus, double Aminus, double Rplus, double Rminus, Calendar arDT, int arPeriod) {
        this.Aplus = Aplus;
        this.Aminus = Aminus;
        this.Rplus = Rplus;
        this.Rminus = Rminus;
        this.arDT = arDT;
        this.arPeriod = arPeriod;
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        return "AvgAR{pqDT=" + sdf.format(arDT.getTime()) + ", Aplus=" + Aplus + ", Aminus=" + Aminus + ", Rplus=" + Rplus + ", Rminus=" + Rminus + ", arPeriod=" + arPeriod + '}';
    }

    public double getAminus() {
        return Aminus;
    }

    public double getAplus() {
        return Aplus;
    }

    public double getRminus() {
        return Rminus;
    }

    public double getRplus() {
        return Rplus;
    }

    public Calendar getArDT() {
        return arDT;
    }

    public int getArPeriod() {
        return arPeriod;
    }
}
