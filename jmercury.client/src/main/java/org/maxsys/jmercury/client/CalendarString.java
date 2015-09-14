package org.maxsys.jmercury.client;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class CalendarString extends GregorianCalendar {

    private String format = "dd.MM.yyyy HH:mm";

    public CalendarString() {
    }

    public CalendarString(int year, int month, int dayOfMonth) {
        super(year, month, dayOfMonth);
    }

    public CalendarString(int year, int month, int dayOfMonth, int hourOfDay, int minute) {
        super(year, month, dayOfMonth, hourOfDay, minute);
    }

    public CalendarString(int year, int month, int dayOfMonth, int hourOfDay, int minute, int second) {
        super(year, month, dayOfMonth, hourOfDay, minute, second);
    }

    public CalendarString(Calendar ca) {
        setTimeInMillis(ca.getTimeInMillis());
    }

    public void setFormat(String format) {
        this.format = format;
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(getTime());
    }

    public int getYear() {
        return get(CalendarString.YEAR);
    }

    public int getMonth() {
        return get(Calendar.MONTH) + 1;
    }

    public int getDay() {
        return get(Calendar.DAY_OF_MONTH);
    }

    public boolean isToday() {
        int tY = Calendar.getInstance().get(Calendar.YEAR);
        int tD = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        return getYear() == tY && get(Calendar.DAY_OF_YEAR) == tD;
    }

    public boolean isNowMonth() {
        int tY = Calendar.getInstance().get(Calendar.YEAR);
        int tM = Calendar.getInstance().get(Calendar.MONTH) + 1;
        return getYear() == tY && getMonth() == tM;
    }
}
