package org.maxsys.jmercury.client;

public class ForReport {

    private String GroupName;
    private String MeterName;
    private String MeterSN;
    private String Aplus1;
    private String Aplus2;
    private String Aplus21;
    private String MeterKi;
    private String Aplus21Ki;
    private String AplusGroupSum;

    public ForReport(String GroupName, String MeterName, String MeterSN, String Aplus1, String Aplus2, String Aplus21, String MeterKi, String Aplus21Ki, String AplusGroupSum) {
        this.GroupName = GroupName;
        this.MeterName = MeterName;
        this.MeterSN = MeterSN;
        this.Aplus1 = Aplus1;
        this.Aplus2 = Aplus2;
        this.Aplus21 = Aplus21;
        this.MeterKi = MeterKi;
        this.Aplus21Ki = Aplus21Ki;
        this.AplusGroupSum = AplusGroupSum;
    }

    public void setAplusGroupSum(String AplusGroupSum) {
        this.AplusGroupSum = AplusGroupSum;
    }

    public String getGroupName() {
        return GroupName;
    }

    public String getMeterName() {
        return MeterName;
    }

    public String getMeterSN() {
        return MeterSN;
    }

    public String getAplus1() {
        return Aplus1;
    }

    public String getAplus2() {
        return Aplus2;
    }

    public String getAplus21() {
        return Aplus21;
    }

    public String getMeterKi() {
        return MeterKi;
    }

    public String getAplus21Ki() {
        return Aplus21Ki;
    }

    public String getAplusGroupSum() {
        return AplusGroupSum;
    }

}
