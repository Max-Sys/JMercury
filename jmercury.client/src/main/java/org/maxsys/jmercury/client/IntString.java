package org.maxsys.jmercury.client;

public class IntString {

    private int i = 0;
    private String s = "";

    public IntString(int i, String str) {
        this.i = i;
        this.s = str;
    }

    public int getInt() {
        return i;
    }

    public void setInt(int i) {
        this.i = i;
    }

    public String getString() {
        return s;
    }

    public void setString(String str) {
        this.s = str;
    }

    @Override
    public String toString() {
        return s;
    }
}
