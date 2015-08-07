package org.maxsys.jmercury.client;

import java.util.Calendar;

public class AplusRplusM {

    private Calendar aprpDate;
    private double Aplus;
    private double Rplus;
    private double AplusOnBeg;
    private double RplusOnBeg;
    private double AplusOnEnd;
    private double RplusOnEnd;

    public AplusRplusM(Calendar aprpDate, double Aplus, double Rplus, double AplusOnBeg, double RplusOnBeg, double AplusOnEnd, double RplusOnEnd) {
        this.aprpDate = aprpDate;
        this.Aplus = Aplus;
        this.Rplus = Rplus;
        this.AplusOnBeg = AplusOnBeg;
        this.RplusOnBeg = RplusOnBeg;
        this.AplusOnEnd = AplusOnEnd;
        this.RplusOnEnd = RplusOnEnd;
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

    public double getAplusOnEnd() {
        return AplusOnEnd;
    }

    public double getRplusOnEnd() {
        return RplusOnEnd;
    }
    
}
