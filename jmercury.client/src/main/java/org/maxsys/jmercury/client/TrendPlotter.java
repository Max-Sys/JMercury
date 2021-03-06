package org.maxsys.jmercury.client;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.JPanel;

public class TrendPlotter extends JPanel {

    private class Trend {

        private String Name;
        private TreeMap<Calendar, Double> TrendValues;
        private TreeMap<Calendar, Double> TrendAvgValues;
        private long beginTime;
        private long endTime;
        private int colorRGB;

        public Trend(String Name, Color color, TreeMap<Calendar, Double> TrendValues) {
            this.Name = Name;
            this.TrendValues = TrendValues;
            this.colorRGB = color.getRGB();
            Calendar bt = null;
            Calendar et = null;
            for (Calendar tt : TrendValues.keySet()) {
                if (bt == null || bt.after(tt)) {
                    bt = tt;
                }
                if (et == null || et.before(tt)) {
                    et = tt;
                }
            }
            if (bt == null) {
                this.beginTime = 0;
            } else {
                this.beginTime = bt.getTimeInMillis();
            }
            if (et == null) {
                this.endTime = 0;
            } else {
                this.endTime = et.getTimeInMillis();
            }
        }

        public String getName() {
            return Name;
        }

        public TreeMap<Calendar, Double> getTrendValues() {
            return TrendValues;
        }

        public void setTrendAvgValues(TreeMap<Calendar, Double> TrendAvgValues) {
            this.TrendAvgValues = TrendAvgValues;
        }

        public TreeMap<Calendar, Double> getTrendAvgValues() {
            return TrendAvgValues;
        }

        public int getColorRGB() {
            return colorRGB;
        }

        public long getBeginTime() {
            return beginTime;
        }

        public long getEndTime() {
            return endTime;
        }
    }

    private class Markers {

        private Calendar Marker1Ca;
        private Calendar Marker2Ca;
        private double Aplus;
        private double baseAPlus;
        private double nullPoint = 0.0;

        public void addMarker1(Calendar ca) {
            if (Marker2Ca != null) {
                if (Marker2Ca.after(ca)) {
                    Marker1Ca = ca;
                } else {
                    Marker1Ca = Marker2Ca;
                    Marker2Ca = ca;
                }
                long m2m1 = Marker2Ca.getTimeInMillis() - Marker1Ca.getTimeInMillis();
                m2m1 /= 1000;
                m2m1 /= 60;
                m2m1 /= 30;
                baseAPlus = nullPoint * m2m1 / 2;
                Aplus = NetClient.sendGetAplusSumFromAvgArs(IdInDB, Marker1Ca, Marker2Ca);
            } else {
                Marker1Ca = ca;
            }
        }

        public void addMarker2(Calendar ca) {
            if (Marker1Ca != null) {
                if (Marker1Ca.before(ca)) {
                    Marker2Ca = ca;
                } else {
                    Marker2Ca = Marker1Ca;
                    Marker1Ca = ca;
                }
                long m2m1 = Marker2Ca.getTimeInMillis() - Marker1Ca.getTimeInMillis();
                m2m1 /= 1000;
                m2m1 /= 60;
                m2m1 /= 30;
                baseAPlus = nullPoint * m2m1 / 2;
                Aplus = NetClient.sendGetAplusSumFromAvgArs(IdInDB, Marker1Ca, Marker2Ca);
            } else {
                Marker2Ca = ca;
            }
        }

        public void Clear() {
            Marker1Ca = null;
            Marker2Ca = null;
            nullPoint = 0.0;
        }

        public boolean isMarker1Ok() {
            return Marker1Ca != null;
        }

        public boolean isMarker2Ok() {
            return Marker2Ca != null;
        }

        public Calendar getMarker1Ca() {
            return Marker1Ca;
        }

        public Calendar getMarker2Ca() {
            return Marker2Ca;
        }

        public double getAplus() {
            return Aplus;
        }

        public void setNullPoint(double nullPoint) {
            this.nullPoint = nullPoint;
            if (isMarker1Ok() && isMarker2Ok()) {
                long m2m1 = Marker2Ca.getTimeInMillis() - Marker1Ca.getTimeInMillis();
                m2m1 /= 1000;
                m2m1 /= 60;
                m2m1 /= 30;
                baseAPlus = nullPoint * m2m1 / 2;
            }
        }

        public double getNullPoint() {
            return nullPoint;
        }

        public double getBaseAPlus() {
            return baseAPlus;
        }
    }

    private ArrayList<Trend> trends = new ArrayList<>();
    private ArrayList<Properties> nlines = new ArrayList<>();
    private Markers markers = new Markers();
    private int tmpW = 0;
    private int tmpH = 0;
    private long tmpTB = 0;
    private long tmpTW = 0;
    private double tmpTH = 0;
    private double tmpTMin = 0;
    private int linex = 10;
    private int avg = 1;
    private int IdInDB;

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        if (trends.isEmpty()) {
            return;
        }

        linex = 25 + g.getFontMetrics().stringWidth("0000.00");
        int liney = getHeight() - 10;

        int tmpWt = getWidth() - linex - 15;
        int tmpHt;
        if (markers.isMarker1Ok() && markers.isMarker2Ok()) {
            tmpHt = liney - ((trends.size() + 1) * g.getFontMetrics().getHeight());
        } else {
            tmpHt = liney - (trends.size() * g.getFontMetrics().getHeight());
        }
        if (tmpW != tmpWt || tmpH != tmpHt) {
            tmpW = tmpWt;
            tmpH = tmpHt;
        }

        DecimalFormat df = new DecimalFormat("#.##");

        int dy0 = getYfromDouble(tmpTMin);
        int dy1 = getYfromDouble(tmpTH);
        int ddy = g.getFontMetrics().getHeight() * 2;
        for (int cdy = 1; cdy < ((dy0 - dy1) / ddy); cdy++) {
            double dy = tmpTMin + (tmpTH - tmpTMin) / ((dy0 - dy1) / ddy) * cdy;
            g.setColor(Color.LIGHT_GRAY);
            g.drawLine(10, getYfromDouble(dy), tmpW + linex, getYfromDouble(dy));
            g.setColor(Color.DARK_GRAY);
            g.drawString(df.format(dy), 10, getYfromDouble(dy) - 3);
        }

        g.setColor(Color.DARK_GRAY);
        g.drawLine(10, getYfromDouble(tmpTMin), tmpW + linex, getYfromDouble(tmpTMin));
        g.drawString(df.format(tmpTMin), 10, getYfromDouble(tmpTMin) - 3);
        g.drawLine(linex, 10, linex, tmpH);

        for (Trend trend : trends) {
            tmpTB = trend.getBeginTime();
            tmpTW = trend.getEndTime() - tmpTB;

            g.setColor(new Color(trend.getColorRGB()));

            Calendar ca = new GregorianCalendar();
            ca.setTimeInMillis(trend.getEndTime());
            String etstr = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(ca.getTime());
            g.drawString(etstr, tmpW + linex - g.getFontMetrics().stringWidth(etstr), liney);
            ca.setTimeInMillis(tmpTB);
            etstr = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(ca.getTime());
            g.drawString(etstr + " - " + trend.getName(), 10, liney);
            liney -= g.getFontMetrics().getHeight();

            int lastx = 0;
            int lasty = 0;
            for (Calendar trpc : trend.getTrendAvgValues().keySet()) {
                int x = getXfromCalendar(trpc);
                int y = getYfromDouble(trend.getTrendAvgValues().get(trpc));
                if (lastx == 0) {
                    lastx = x;
                }
                if (lasty == 0) {
                    lasty = y;
                }
                g.drawLine(lastx, lasty, x, y);
                lastx = x;
                lasty = y;
            }
        }

        g.setColor(Color.GRAY);
        for (Properties p : nlines) {
            String c1s = p.getProperty("c1");
            String c2s = p.getProperty("c2");
            String v1s = p.getProperty("v1");
            String v2s = p.getProperty("v2");

            Calendar c1 = new GregorianCalendar();
            c1.setTimeInMillis(Long.valueOf(c1s));
            int x1 = getXfromCalendar(c1);
            Calendar c2 = new GregorianCalendar();
            c2.setTimeInMillis(Long.valueOf(c2s));
            int x2 = getXfromCalendar(c2);

            int y1 = getYfromDouble(Double.valueOf(v1s));
            int y2 = getYfromDouble(Double.valueOf(v2s));

            Polygon polygon = new Polygon();
            polygon.addPoint(x1, y1);
            polygon.addPoint(x2, y2);
            g.drawPolygon(polygon);
            g.drawString(new SimpleDateFormat("dd.MM.yyyy HH:mm").format(c1.getTime()) + " - " + df.format(Double.valueOf(v1s)), x2, y2);
        }

        if (markers.isMarker1Ok()) {
            g.setColor(Color.GRAY);
            g.drawLine(getXfromCalendar(markers.getMarker1Ca()), 10, getXfromCalendar(markers.getMarker1Ca()), tmpH);
        }

        if (markers.isMarker2Ok()) {
            g.setColor(Color.GRAY);
            g.drawLine(getXfromCalendar(markers.getMarker2Ca()), 10, getXfromCalendar(markers.getMarker2Ca()), tmpH);
        }

        if (markers.getNullPoint() > 0) {
            g.setColor(Color.BLUE);
            g.drawLine(linex, getYfromDouble(markers.getNullPoint()), tmpW + linex, getYfromDouble(markers.getNullPoint()));
        }

        if (markers.isMarker1Ok() && markers.isMarker2Ok()) {
            g.setColor(Color.BLACK);
            CalendarString csm1 = new CalendarString(markers.getMarker1Ca());
            CalendarString csm2 = new CalendarString(markers.getMarker2Ca());
            csm1.setFormat("dd.MM.yyyy HH:mm");
            csm2.setFormat("dd.MM.yyyy HH:mm");
            if (markers.getBaseAPlus() > 0) {
                g.drawString("Потребление (A+) с " + csm1.toString() + " по " + csm2.toString() + " составило " + df.format(markers.getAplus()) + " кВт⋅ч, с учетом установленной базы (" + df.format(markers.getNullPoint()) + ") составило " + df.format(markers.getAplus() - markers.getBaseAPlus()) + " кВт⋅ч", 10, liney);
            } else {
                g.drawString("Потребление (A+) с " + csm1.toString() + " по " + csm2.toString() + " составило " + df.format(markers.getAplus()) + " кВт⋅ч", 10, liney);
            }
        }
    }

    private int getXfromCalendar(Calendar xca) {
        long lx = xca.getTimeInMillis() - tmpTB;
        return (int) (lx * tmpW / tmpTW) + linex;
    }

    public Calendar getCalendarFromX(int x) {
        long lx = (x - linex) * tmpTW / tmpW;
        lx += tmpTB;
        Calendar ca = new GregorianCalendar();
        ca.setTimeInMillis(lx);
        return ca;
    }

    private int getYfromDouble(Double value) {
        return (int) (tmpH - ((value - tmpTMin) * tmpH / (tmpTH - tmpTMin)));
    }

    public double getDoubleFromY(int y) {
        return ((tmpH - y) * (tmpTH - tmpTMin)) / tmpH + tmpTMin;
    }

    public void addTrend(String trendName, Color trendColor, TreeMap<Calendar, Double> trend) {
        trends.add(new Trend(trendName, trendColor, trend));
        setAvg(avg);
    }

    public void removeTrends() {
        trends.clear();
        nlines.clear();
        tmpTH = 0;
        tmpTMin = 0;
    }

    public void setAvg(int avgFactor) {
        tmpTH = 0;
        tmpTMin = 0;

        avg = avgFactor;

        for (Trend trend : trends) {
            TreeMap<Calendar, Double> tmpATrend = new TreeMap<>();
            Object[] cas = trend.getTrendValues().keySet().toArray();
            for (int ic = 0; ic < cas.length; ic++) {
                double avgd = 0;
                int avgi = 0;
                while (avgi < avg && (ic + avgi) < cas.length) {
                    Calendar ca = (Calendar) cas[avgi + ic];
                    avgd += trend.getTrendValues().get(ca);
                    avgi++;
                }
                avgd /= avgi;

                Calendar ca = (Calendar) cas[ic];
                tmpATrend.put(ca, avgd);
            }

            double valH = tmpATrend.firstEntry().getValue();
            double valHmin = tmpATrend.firstEntry().getValue();
            for (double value : tmpATrend.values()) {
                if (valH < value) {
                    valH = value;
                }
                if (valHmin > value) {
                    valHmin = value;
                }
            }
            if (tmpTH < valH) {
                tmpTH = valH;
            }
            if (tmpTMin > valHmin) {
                tmpTMin = valHmin;
            }

            trend.setTrendAvgValues(tmpATrend);
        }

        double p20 = ((tmpTH - tmpTMin) / 5);
        tmpTH += p20;
        tmpTMin -= p20;
        if (tmpTMin < 0) {
            tmpTMin = 0;
        }

        nlines.clear();
    }

    public void addNLine(int x1, int y1, int x2, int y2) {
        Properties p = new Properties();
        p.setProperty("c1", String.valueOf(getCalendarFromX(x1).getTimeInMillis()));
        p.setProperty("c2", String.valueOf(getCalendarFromX(x2).getTimeInMillis()));
        p.setProperty("v1", String.valueOf(getDoubleFromY(y1)));
        p.setProperty("v2", String.valueOf(getDoubleFromY(y2)));
        nlines.add(p);
    }

    public void removeNLines() {
        nlines.clear();
    }

    public void addMarker1(int x) {
        Calendar ca = getCalendarFromX(x);
        if (ca.get(Calendar.MINUTE) > 15 && ca.get(Calendar.MINUTE) < 45) {
            ca.set(Calendar.MINUTE, 30);
        } else {
            if (ca.get(Calendar.MINUTE) < 30) {
                ca.set(Calendar.MINUTE, 0);
            } else {
                ca.set(Calendar.MINUTE, 0);
                ca.add(Calendar.HOUR, 1);
            }
        }
        ca.set(Calendar.SECOND, 0);
        ca.set(Calendar.MILLISECOND, 0);
        markers.addMarker1(ca);
    }

    public void addMarker2(int x) {
        Calendar ca = getCalendarFromX(x);
        if (ca.get(Calendar.MINUTE) > 15 && ca.get(Calendar.MINUTE) < 45) {
            ca.set(Calendar.MINUTE, 30);
        } else {
            if (ca.get(Calendar.MINUTE) < 30) {
                ca.set(Calendar.MINUTE, 0);
            } else {
                ca.set(Calendar.MINUTE, 0);
                ca.add(Calendar.HOUR, 1);
            }
        }
        ca.set(Calendar.SECOND, 0);
        ca.set(Calendar.MILLISECOND, 0);
        markers.addMarker2(ca);
    }

    public void addMarkerNext(int x) {
        if (!markers.isMarker1Ok()) {
            addMarker1(x);
        } else {
            addMarker2(x);
        }
    }

    public void removeMarkers() {
        markers.Clear();
    }

    public void setIdInDB(int IdInDB) {
        this.IdInDB = IdInDB;
    }

    public boolean isMarkersSet() {
        return markers.isMarker1Ok() && markers.isMarker2Ok();
    }

    void addNull(int tmpY) {
        markers.setNullPoint(getDoubleFromY(tmpY));
    }
}
