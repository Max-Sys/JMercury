package org.maxsys.jmercury.client;

import java.util.Calendar;

public class AplusRplusD {

    private Calendar aprpDate;
    private double Aplus;
    private double Rplus;

    public AplusRplusD(Calendar date, double Aplus, double Rplus) {
        this.Aplus = Aplus;
        this.Rplus = Rplus;
        this.aprpDate = date;
    }

    public Calendar getAprpDate() {
        return aprpDate;
    }

    public double getAplus() {
        return Aplus;
    }

    public double getRplus() {
        return Rplus;
    }

    @Override
    public String toString() {
        return "AplusRplusD{" + "aprpDate=" + aprpDate.getTime() + ", Aplus=" + Aplus + ", Rplus=" + Rplus + '}';
    }

}
