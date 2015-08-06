package org.maxsys.jmercury.client;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.JPanel;

public class TrendPlotter extends JPanel {

    private static HashMap<TreeMap<Calendar, Double>, Properties> trends = new HashMap<>();
    private static HashMap<TreeMap<Calendar, Double>, Properties> atrends = new HashMap<>();
    private static ArrayList<Properties> nlines = new ArrayList<>();
    private int tmpW = 0;
    private int tmpH = 0;
    private long tmpTB = 0;
    private long tmpTW = 0;
    private static double tmpTH = 0;
    private static double tmpTMin = 0;
    private int linex = 10;
    private static int avg = 1;

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        if (atrends.isEmpty()) {
            return;
        }

        linex = 25 + g.getFontMetrics().stringWidth("0.00");
        int liney = getHeight() - 10;

        int tmpWt = getWidth() - linex - 15;
        int tmpHt = liney - (atrends.size() * g.getFontMetrics().getHeight());
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

        for (TreeMap<Calendar, Double> trend : atrends.keySet()) {
            tmpTB = Long.valueOf(atrends.get(trend).getProperty("beginTime"));
            tmpTW = Long.valueOf(atrends.get(trend).getProperty("endTime")) - tmpTB;

            g.setColor(new Color(Integer.valueOf(atrends.get(trend).getProperty("color"))));

            Calendar ca = new GregorianCalendar();
            ca.setTimeInMillis(Long.valueOf(atrends.get(trend).getProperty("endTime")));
            String etstr = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(ca.getTime());
            g.drawString(etstr, tmpW + linex - g.getFontMetrics().stringWidth(etstr), liney);
            ca.setTimeInMillis(tmpTB);
            etstr = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(ca.getTime());
            g.drawString(etstr + " - " + atrends.get(trend).getProperty("name"), 10, liney);
            liney -= g.getFontMetrics().getHeight();

            int lastx = 0;
            int lasty = 0;
            for (Calendar trpc : trend.keySet()) {
                int x = getXfromCalendar(trpc);
                int y = getYfromDouble(trend.get(trpc));
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
    }

    private int getXfromCalendar(Calendar xca) {
        long lx = xca.getTimeInMillis() - tmpTB;
        return (int) (lx * tmpW / tmpTW) + linex;
    }

    private Calendar getCalendarFromX(int x) {
        long lx = (x - linex) * tmpTW / tmpW;
        lx += tmpTB;
        Calendar ca = new GregorianCalendar();
        ca.setTimeInMillis(lx);
        return ca;
    }

    private int getYfromDouble(Double value) {
        return (int) (tmpH - ((value - tmpTMin) * tmpH / (tmpTH - tmpTMin)));
    }

    private double getDoubleFromY(int y) {
        return ((tmpH - y) * (tmpTH - tmpTMin)) / tmpH + tmpTMin;
    }

    public static void addTrend(String trendName, Color trendColor, TreeMap<Calendar, Double> trend) {
        Properties prop = new Properties();
        prop.setProperty("name", trendName);
        prop.setProperty("color", String.valueOf(trendColor.getRGB()));

        Calendar bt = null;
        Calendar et = null;
        for (Calendar tt : trend.keySet()) {
            if (bt == null || bt.after(tt)) {
                bt = tt;
            }
            if (et == null || et.before(tt)) {
                et = tt;
            }
        }
        if (bt == null) {
            prop.setProperty("beginTime", "0");
        } else {
            prop.setProperty("beginTime", String.valueOf(bt.getTimeInMillis()));
        }
        if (bt == null) {
            prop.setProperty("endTime", "0");
        } else {
            prop.setProperty("endTime", String.valueOf(et.getTimeInMillis()));
        }

        trends.put(trend, prop);
        setAvg(avg);
    }

    public static void removeTrends() {
        trends.clear();
        atrends.clear();
        nlines.clear();
        tmpTH = 0;
        tmpTMin = 0;
    }

    public static void setAvg(int avgFactor) {
        avg = avgFactor;

        atrends.clear();
        for (TreeMap<Calendar, Double> trend : trends.keySet()) {
            TreeMap<Calendar, Double> tmpATrend = new TreeMap<>();
            Object[] cas = trend.keySet().toArray();
            for (int ic = 0; ic < cas.length; ic++) {
                double avgd = 0;
                int avgi = 0;
                while (avgi < avg && (ic + avgi) < cas.length) {
                    Calendar ca = (Calendar) cas[avgi + ic];
                    avgd += trend.get(ca);
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
            if (tmpTH < valH || atrends.isEmpty()) {
                tmpTH = valH;
            }
            if (tmpTMin > valHmin || atrends.isEmpty()) {
                tmpTMin = valHmin;
            }

            atrends.put(tmpATrend, trends.get(trend));
        }

        double p33 = ((tmpTH - tmpTMin) / 3);
        tmpTH += p33;
        tmpTMin -= p33;

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

    public static void removeNLines() {
        nlines.clear();
    }
}
