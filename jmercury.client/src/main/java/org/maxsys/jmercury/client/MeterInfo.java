package org.maxsys.jmercury.client;

public class MeterInfo {

    private String Name;
    private String Group;
    private int Ki;
    private int idInDB;
    private String SN;

    public MeterInfo(int idInDB, String Name, String Group, int Ki, String SN) {
        this.idInDB = idInDB;
        this.Name = Name;
        this.Group = Group;
        this.Ki = Ki;
        this.SN = SN;
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

    @Override
    public String toString() {
        return Name + " (S/N: " + SN + ", Ki: " + Ki + ")";
    }
}
