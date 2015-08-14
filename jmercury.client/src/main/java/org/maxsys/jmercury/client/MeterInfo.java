package org.maxsys.jmercury.client;

public class MeterInfo {

    private String Name;
    private String Group;
    private int Ki;
    private int idInDB;
    private String SN;
    private String osv;

    public MeterInfo(int idInDB, String Name, String Group, int Ki, String SN, String osv) {
        this.idInDB = idInDB;
        this.Name = Name;
        this.Group = Group;
        this.Ki = Ki;
        this.SN = SN;
        this.osv = osv;
    }

    public int getIdInDB() {
        return idInDB;
    }

    public String getName() {
        return Name;
    }

    public String getGroup() {
        return Group;
    }

    public int getKi() {
        return Ki;
    }

    public String getSN() {
        return SN;
    }

    public String getOsv() {
        return osv;
    }

    @Override
    public String toString() {
        return Name + " (S/N: " + SN + ", Ki: " + Ki + ")";
    }
}
