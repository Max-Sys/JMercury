package org.maxsys.jmercury.server;

public class AplusRplus {

    private double Aplus;
    private double Rplus;

    public AplusRplus(double Aplus, double Rplus) {
        this.Aplus = Aplus;
        this.Rplus = Rplus;
    }

    public double getAplus() {
        return Aplus;
    }

    public double getRplus() {
        return Rplus;
    }

    @Override
    public String toString() {
        return "AplusRplus{" + "Aplus=" + Aplus + ", Rplus=" + Rplus + '}';
    }
}
