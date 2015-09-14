package org.maxsys.jmercury.client;

import java.util.Calendar;

public class AplusRplusD {

    private Calendar aprpDate;
    private double Aplus;
    private double Rplus;
    private double AplusOnBeg;
    private double RplusOnBeg;

    public AplusRplusD(Calendar date, double Aplus, double Rplus, double AplusOnBeg, double RplusOnBeg) {
        this.Aplus = Aplus;
        this.Rplus = Rplus;
        this.AplusOnBeg = AplusOnBeg;
        this.RplusOnBeg = RplusOnBeg;
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

    public double getAplusOnBeg() {
        return AplusOnBeg;
    }

    public double getRplusOnBeg() {
        return RplusOnBeg;
    }

    @Override
    public String toString() {
        return "AplusRplusD{" + "aprpDate=" + aprpDate + ", Aplus=" + Aplus + ", Rplus=" + Rplus + ", AplusOnBeg=" + AplusOnBeg + ", RplusOnBeg=" + RplusOnBeg + '}';
    }

}
