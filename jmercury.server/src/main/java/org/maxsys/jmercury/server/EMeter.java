package org.maxsys.jmercury.server;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jssc.SerialPort;
import jssc.SerialPortException;

public class EMeter {

    private final int idInDB;
    private final HashMap<String, String> meterFlags = new HashMap<>();
    private final String meterName;
    private final String groupName;
    private String serialNumber = "";
    private final String portName;
    private SerialPort serialPort;
    private Calendar lastCmdTime;
    private int delay = 500;
    private byte addressOnBus = 0;
    private int addressInMem3 = 0;
    private Boolean overloadBit = null;
    private int Ki = 1;
    private final int meterConst = 1000;
    private byte[] lastResp = new byte[0];
    private final int[] CRC16_TABLE = {0x0000, 0xc0c1, 0xc181, 0x0140, 0xc301, 0x03c0, 0x0280, 0xc241, 0xc601, 0x06c0, 0x0780, 0xc741, 0x0500, 0xc5c1, 0xc481, 0x0440, 0xcc01, 0x0cc0, 0x0d80, 0xcd41, 0x0f00, 0xcfc1, 0xce81, 0x0e40, 0x0a00, 0xcac1, 0xcb81, 0x0b40, 0xc901, 0x09c0, 0x0880, 0xc841, 0xd801, 0x18c0, 0x1980, 0xd941, 0x1b00, 0xdbc1, 0xda81, 0x1a40, 0x1e00, 0xdec1, 0xdf81, 0x1f40, 0xdd01, 0x1dc0, 0x1c80, 0xdc41, 0x1400, 0xd4c1, 0xd581, 0x1540, 0xd701, 0x17c0, 0x1680, 0xd641, 0xd201, 0x12c0, 0x1380, 0xd341, 0x1100, 0xd1c1, 0xd081, 0x1040, 0xf001, 0x30c0, 0x3180, 0xf141, 0x3300, 0xf3c1, 0xf281, 0x3240, 0x3600, 0xf6c1, 0xf781, 0x3740, 0xf501, 0x35c0, 0x3480, 0xf441, 0x3c00, 0xfcc1, 0xfd81, 0x3d40, 0xff01, 0x3fc0, 0x3e80, 0xfe41, 0xfa01, 0x3ac0, 0x3b80, 0xfb41, 0x3900, 0xf9c1, 0xf881, 0x3840, 0x2800, 0xe8c1, 0xe981, 0x2940, 0xeb01, 0x2bc0, 0x2a80, 0xea41, 0xee01, 0x2ec0, 0x2f80, 0xef41, 0x2d00, 0xedc1, 0xec81, 0x2c40, 0xe401, 0x24c0, 0x2580, 0xe541, 0x2700, 0xe7c1, 0xe681, 0x2640, 0x2200, 0xe2c1, 0xe381, 0x2340, 0xe101, 0x21c0, 0x2080, 0xe041, 0xa001, 0x60c0, 0x6180, 0xa141, 0x6300, 0xa3c1, 0xa281, 0x6240, 0x6600, 0xa6c1, 0xa781, 0x6740, 0xa501, 0x65c0, 0x6480, 0xa441, 0x6c00, 0xacc1, 0xad81, 0x6d40, 0xaf01, 0x6fc0, 0x6e80, 0xae41, 0xaa01, 0x6ac0, 0x6b80, 0xab41, 0x6900, 0xa9c1, 0xa881, 0x6840, 0x7800, 0xb8c1, 0xb981, 0x7940, 0xbb01, 0x7bc0, 0x7a80, 0xba41, 0xbe01, 0x7ec0, 0x7f80, 0xbf41, 0x7d00, 0xbdc1, 0xbc81, 0x7c40, 0xb401, 0x74c0, 0x7580, 0xb541, 0x7700, 0xb7c1, 0xb681, 0x7640, 0x7200, 0xb2c1, 0xb381, 0x7340, 0xb101, 0x71c0, 0x7080, 0xb041, 0x5000, 0x90c1, 0x9181, 0x5140, 0x9301, 0x53c0, 0x5280, 0x9241, 0x9601, 0x56c0, 0x5780, 0x9741, 0x5500, 0x95c1, 0x9481, 0x5440, 0x9c01, 0x5cc0, 0x5d80, 0x9d41, 0x5f00, 0x9fc1, 0x9e81, 0x5e40, 0x5a00, 0x9ac1, 0x9b81, 0x5b40, 0x9901, 0x59c0, 0x5880, 0x9841, 0x8801, 0x48c0, 0x4980, 0x8941, 0x4b00, 0x8bc1, 0x8a81, 0x4a40, 0x4e00, 0x8ec1, 0x8f81, 0x4f40, 0x8d01, 0x4dc0, 0x4c80, 0x8c41, 0x4400, 0x84c1, 0x8581, 0x4540, 0x8701, 0x47c0, 0x4680, 0x8641, 0x8201, 0x42c0, 0x4380, 0x8341, 0x4100, 0x81c1, 0x8081, 0x4040};
    private String status = "---";

    public EMeter(String counterName, String groupName, int Ki, String comPort, int addressOnBus, int idInDB) {
        this.meterName = counterName;
        this.groupName = groupName;
        this.addressOnBus = (byte) addressOnBus;
        this.Ki = Ki;
        this.portName = comPort;
        if (!portName.equals("null")) {
            serialPort = new SerialPort(portName);
            lastCmdTime = Calendar.getInstance();
            lastCmdTime.add(Calendar.HOUR_OF_DAY, -1);
        }
        this.idInDB = idInDB;
    }

    private void setCRC16(byte[] cmd) {
        int sum = 0xFFFF;
        for (int i = 0; i < cmd.length - 2; i++) {
            sum = (sum >> 8) ^ CRC16_TABLE[((sum) ^ ((int) cmd[i] & 0xff)) & 0xff];
        }
        cmd[cmd.length - 1] = (byte) (sum >>> 8);
        cmd[cmd.length - 2] = (byte) sum;
    }

    public boolean sendCMD(byte[] cmd) {
        if (portName.equals("null")) {
            lastResp = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            try {
                Thread.sleep(delay);
            } catch (InterruptedException ex) {
                Logger.getLogger(EMeter.class.getName()).log(Level.SEVERE, null, ex);
            }
            return false;
        }
        Calendar ca = Calendar.getInstance();
        ca.add(Calendar.SECOND, -200);
        if (lastCmdTime.before(ca)) {
            byte[] tcmd = new byte[]{
                cmd[0], // Адрес
                (byte) 0x01, // Код запроса
                (byte) 0x01, // Access level
                (byte) 0x01, // Password
                (byte) 0x01, // Password
                (byte) 0x01, // Password
                (byte) 0x01, // Password
                (byte) 0x01, // Password
                (byte) 0x01, // Password
                (byte) 0x00, // CRC
                (byte) 0x00};// CRC
            setCRC16(tcmd);
            try {
                if (!serialPort.isOpened()) {
                    if (!serialPort.openPort()) {
                        return false;
                    }
                    serialPort.setParams(jssc.SerialPort.BAUDRATE_9600, jssc.SerialPort.DATABITS_8, jssc.SerialPort.STOPBITS_1, jssc.SerialPort.PARITY_NONE);
                    if (!serialPort.isOpened()) {
                        return false;
                    }
                }
                serialPort.writeBytes(tcmd);
                Thread.sleep(delay);
                serialPort.readBytes();
            } catch (SerialPortException | InterruptedException ex) {
                Logger.getLogger(EMeter.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }
        setCRC16(cmd);
        try {
            if (!serialPort.isOpened()) {
                serialPort.openPort();
                if (!serialPort.isOpened()) {
                    return false;
                }
            }
            serialPort.writeBytes(cmd);
            Thread.sleep(delay);
            lastResp = serialPort.readBytes();
            serialPort.closePort();
        } catch (SerialPortException | InterruptedException ex) {
            Logger.getLogger(EMeter.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        lastCmdTime = Calendar.getInstance();
        return true;
    }

    public byte[] getResponse() {
        if (lastResp == null) {
            return null;
        }
        byte[] resp = lastResp.clone();
        lastResp = new byte[0];
        return resp;
    }

    public String getMeterName() {
        return meterName;
    }

    public String getGroupName() {
        return groupName;
    }

    public int getMeterKi() {
        return Ki;
    }

    public String getMeterComPort() {
        return portName;
    }

    public int getIdInDB() {
        return idInDB;
    }

    public void setMeterFlags(String flagsString) {
        meterFlags.clear();
        String[] vks = flagsString.split(";");
        for (String vk : vks) {
            String[] valkey = vk.split(":");
            if (valkey.length == 2) {
                meterFlags.put(valkey[0], valkey[1]);
            }
        }
    }

    public String getMeterFlags() {
        StringBuilder flagsString = new StringBuilder();
        for (Map.Entry<String, String> kvp : meterFlags.entrySet()) {
            flagsString.append(kvp.getKey()).append(":").append(kvp.getValue()).append(";");
        }
        return flagsString.toString();
    }

    public String getMeterFlag(String flagName) {
        return meterFlags.get(flagName);
    }

    public void setMeterFlag(String flagName, String flagValue) {
        meterFlags.put(flagName, flagValue);
    }

    @Override
    public String toString() {
        return meterName;
    }

    int getDelay() {
        return this.delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public Double getFreq() {
        double freq = 0d;
        byte[] cmd = new byte[]{
            addressOnBus, // Адрес
            (byte) 0x08, // Код запроса
            (byte) 0x11,
            (byte) 0x40,
            (byte) 0x00, // CRC ставится автоматом
            (byte) 0x00};// CRC ставится автоматом
        if (!sendCMD(cmd)) {
            return null;
        }
        byte[] resp = getResponse();
        if (resp != null && resp.length == 6) {
            freq += (resp[3] & 0xFF) << 8;
            freq += (resp[2] & 0xFF);
            freq = freq / 100;
        } else {
            return null;
        }
        return freq;
    }

    public Double getA12() {
        double a12 = 0d;
        byte[] cmd = new byte[]{
            addressOnBus, // Адрес
            (byte) 0x08, // Код запроса
            (byte) 0x11,
            (byte) 0x51,
            (byte) 0x00, // CRC ставится автоматом
            (byte) 0x00};// CRC ставится автоматом
        if (!sendCMD(cmd)) {
            return null;
        }
        byte[] resp = getResponse();
        if (resp != null && resp.length == 6) {
            a12 += (resp[3] & 0xFF) << 8;
            a12 += (resp[2] & 0xFF);
            a12 = a12 / 100;
        } else {
            return null;
        }
        return a12;
    }

    public Double getA13() {
        double a13 = 0d;
        byte[] cmd = new byte[]{
            addressOnBus, // Адрес
            (byte) 0x08, // Код запроса
            (byte) 0x11,
            (byte) 0x52,
            (byte) 0x00, // CRC ставится автоматом
            (byte) 0x00};// CRC ставится автоматом
        if (!sendCMD(cmd)) {
            return null;
        }
        byte[] resp = getResponse();
        if (resp != null && resp.length == 6) {
            a13 += (resp[3] & 0xFF) << 8;
            a13 += (resp[2] & 0xFF);
            a13 = a13 / 100;
        } else {
            return null;
        }
        return a13;
    }

    public Double getA23() {
        double a23 = 0d;
        byte[] cmd = new byte[]{
            addressOnBus, // Адрес
            (byte) 0x08, // Код запроса
            (byte) 0x11,
            (byte) 0x53,
            (byte) 0x00, // CRC ставится автоматом
            (byte) 0x00};// CRC ставится автоматом
        if (!sendCMD(cmd)) {
            return null;
        }
        byte[] resp = getResponse();
        if (resp != null && resp.length == 6) {
            a23 += (resp[3] & 0xFF) << 8;
            a23 += (resp[2] & 0xFF);
            a23 = a23 / 100;
        } else {
            return null;
        }
        return a23;
    }

    public Double getU1() {
        double u1 = 0d;
        byte[] cmd = new byte[]{
            addressOnBus,
            (byte) 0x08,
            (byte) 0x11,
            (byte) 0x11,
            (byte) 0x00,
            (byte) 0x00};
        if (!sendCMD(cmd)) {
            return null;
        }
        byte[] resp = getResponse();
        if (resp != null && resp.length == 6) {
            u1 += (resp[3] & 0xFF) << 8;
            u1 += (resp[2] & 0xFF);
            u1 /= 100;
        } else {
            return null;
        }
        return u1;
    }

    public Double getU2() {
        double u2 = 0d;
        byte[] cmd = new byte[]{
            addressOnBus,
            (byte) 0x08,
            (byte) 0x11,
            (byte) 0x12,
            (byte) 0x00,
            (byte) 0x00};
        if (!sendCMD(cmd)) {
            return null;
        }
        byte[] resp = getResponse();
        if (resp != null && resp.length == 6) {
            u2 += (resp[3] & 0xFF) << 8;
            u2 += (resp[2] & 0xFF);
            u2 /= 100;
        } else {
            return null;
        }
        return u2;
    }

    public Double getU3() {
        double u3 = 0d;
        byte[] cmd = new byte[]{
            addressOnBus,
            (byte) 0x08,
            (byte) 0x11,
            (byte) 0x13,
            (byte) 0x00,
            (byte) 0x00};
        if (!sendCMD(cmd)) {
            return null;
        }
        byte[] resp = getResponse();
        if (resp != null && resp.length == 6) {
            u3 += (resp[3] & 0xFF) << 8;
            u3 += (resp[2] & 0xFF);
            u3 /= 100;
        } else {
            return null;
        }
        return u3;
    }

    public Double getI1() {
        double i1 = 0d;
        byte[] cmd = new byte[]{
            addressOnBus,
            (byte) 0x08,
            (byte) 0x11,
            (byte) 0x21,
            (byte) 0x00,
            (byte) 0x00};
        if (!sendCMD(cmd)) {
            return null;
        }
        byte[] resp = getResponse();
        if (resp != null && resp.length == 6) {
            i1 += (resp[3] & 0xFF) << 8;
            i1 += (resp[2] & 0xFF);
            i1 /= 1000;
            i1 *= Ki;
        } else {
            return null;
        }
        return i1;
    }

    public Double getI2() {
        double i2 = 0d;
        byte[] cmd = new byte[]{
            addressOnBus,
            (byte) 0x08,
            (byte) 0x11,
            (byte) 0x22,
            (byte) 0x00,
            (byte) 0x00};
        if (!sendCMD(cmd)) {
            return null;
        }
        byte[] resp = getResponse();
        if (resp != null && resp.length == 6) {
            i2 += (resp[3] & 0xFF) << 8;
            i2 += (resp[2] & 0xFF);
            i2 /= 1000;
            i2 *= Ki;
        } else {
            return null;
        }
        return i2;
    }

    public Double getI3() {
        double i3 = 0d;
        byte[] cmd = new byte[]{
            addressOnBus,
            (byte) 0x08,
            (byte) 0x11,
            (byte) 0x23,
            (byte) 0x00,
            (byte) 0x00};
        if (!sendCMD(cmd)) {
            return null;
        }
        byte[] resp = getResponse();
        if (resp != null && resp.length == 6) {
            i3 += (resp[3] & 0xFF) << 8;
            i3 += (resp[2] & 0xFF);
            i3 /= 1000;
            i3 *= Ki;
        } else {
            return null;
        }
        return i3;
    }

    public Double getP1() {
        double p1 = 0d;
        byte[] cmd = new byte[]{
            addressOnBus,
            (byte) 0x08,
            (byte) 0x11,
            (byte) 0x01,
            (byte) 0x00,
            (byte) 0x00};
        if (!sendCMD(cmd)) {
            return null;
        }
        byte[] resp = getResponse();
        if (resp != null && resp.length == 6) {
            p1 += (resp[3] & 0xFF) << 8;
            p1 += (resp[2] & 0xFF);
            if ((resp[1] & 0x80) == 0x80) {
                p1 /= -100;
            } else {
                p1 /= 100;
            }
            p1 *= Ki;
        } else {
            return null;
        }
        return p1;
    }

    public Double getP2() {
        double p2 = 0d;
        byte[] cmd = new byte[]{
            addressOnBus,
            (byte) 0x08,
            (byte) 0x11,
            (byte) 0x02,
            (byte) 0x00,
            (byte) 0x00};
        if (!sendCMD(cmd)) {
            return null;
        }
        byte[] resp = getResponse();
        if (resp != null && resp.length == 6) {
            p2 += (resp[3] & 0xFF) << 8;
            p2 += (resp[2] & 0xFF);
            if ((resp[1] & 0x80) == 0x80) {
                p2 /= -100;
            } else {
                p2 /= 100;
            }
            p2 *= Ki;
        } else {
            return null;
        }
        return p2;
    }

    public Double getP3() {
        double p3 = 0d;
        byte[] cmd = new byte[]{
            addressOnBus,
            (byte) 0x08,
            (byte) 0x11,
            (byte) 0x03,
            (byte) 0x00,
            (byte) 0x00};
        if (!sendCMD(cmd)) {
            return null;
        }
        byte[] resp = getResponse();
        if (resp != null && resp.length == 6) {
            p3 += (resp[3] & 0xFF) << 8;
            p3 += (resp[2] & 0xFF);
            if ((resp[1] & 0x80) == 0x80) {
                p3 /= -100;
            } else {
                p3 /= 100;
            }
            p3 *= Ki;
        } else {
            return null;
        }
        return p3;
    }

    public Double getPsum() {
        double psum = 0d;
        byte[] cmd = new byte[]{
            addressOnBus,
            (byte) 0x08,
            (byte) 0x11,
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0x00};
        if (!sendCMD(cmd)) {
            return null;
        }
        byte[] resp = getResponse();
        if (resp != null && resp.length == 6) {
            psum += (resp[3] & 0xFF) << 8;
            psum += (resp[2] & 0xFF);
            if ((resp[1] & 0x80) == 0x80) {
                psum /= -100;
            } else {
                psum /= 100;
            }
            psum *= Ki;
        } else {
            return null;
        }
        return psum;
    }

    public Double getQ1() {
        double q1 = 0d;
        byte[] cmd = new byte[]{
            addressOnBus,
            (byte) 0x08,
            (byte) 0x11,
            (byte) 0x05,
            (byte) 0x00,
            (byte) 0x00};
        if (!sendCMD(cmd)) {
            return null;
        }
        byte[] resp = getResponse();
        if (resp != null && resp.length == 6) {
            q1 += (resp[3] & 0xFF) << 8;
            q1 += (resp[2] & 0xFF);
            if ((resp[1] & 0x40) == 0x40) {
                q1 /= -100;
            } else {
                q1 /= 100;
            }
            q1 *= Ki;
        } else {
            return null;
        }
        return q1;
    }

    public Double getQ2() {
        double q2 = 0d;
        byte[] cmd = new byte[]{
            addressOnBus,
            (byte) 0x08,
            (byte) 0x11,
            (byte) 0x06,
            (byte) 0x00,
            (byte) 0x00};
        if (!sendCMD(cmd)) {
            return null;
        }
        byte[] resp = getResponse();
        if (resp != null && resp.length == 6) {
            q2 += (resp[3] & 0xFF) << 8;
            q2 += (resp[2] & 0xFF);
            if ((resp[1] & 0x40) == 0x40) {
                q2 /= -100;
            } else {
                q2 /= 100;
            }
            q2 *= Ki;
        } else {
            return null;
        }
        return q2;
    }

    public Double getQ3() {
        double q3 = 0d;
        byte[] cmd = new byte[]{
            addressOnBus,
            (byte) 0x08,
            (byte) 0x11,
            (byte) 0x07,
            (byte) 0x00,
            (byte) 0x00};
        if (!sendCMD(cmd)) {
            return null;
        }
        byte[] resp = getResponse();
        if (resp != null && resp.length == 6) {
            q3 += (resp[3] & 0xFF) << 8;
            q3 += (resp[2] & 0xFF);
            if ((resp[1] & 0x40) == 0x40) {
                q3 /= -100;
            } else {
                q3 /= 100;
            }
            q3 *= Ki;
        } else {
            return null;
        }
        return q3;
    }

    public Double getQsum() {
        double qsum = 0d;
        byte[] cmd = new byte[]{
            addressOnBus,
            (byte) 0x08,
            (byte) 0x11,
            (byte) 0x04,
            (byte) 0x00,
            (byte) 0x00};
        if (!sendCMD(cmd)) {
            return null;
        }
        byte[] resp = getResponse();
        if (resp != null && resp.length == 6) {
            qsum += (resp[3] & 0xFF) << 8;
            qsum += (resp[2] & 0xFF);
            if ((resp[1] & 0x40) == 0x40) {
                qsum /= -100;
            } else {
                qsum /= 100;
            }
            qsum *= Ki;
        } else {
            return null;
        }
        return qsum;
    }

    public Double getS1() {
        double s1 = 0d;
        byte[] cmd = new byte[]{
            addressOnBus,
            (byte) 0x08,
            (byte) 0x11,
            (byte) 0x09,
            (byte) 0x00,
            (byte) 0x00};
        if (!sendCMD(cmd)) {
            return null;
        }
        byte[] resp = getResponse();
        if (resp != null && resp.length == 6) {
            s1 += (resp[3] & 0xFF) << 8;
            s1 += (resp[2] & 0xFF);
            s1 /= 100;
            s1 *= Ki;
        } else {
            return null;
        }
        return s1;
    }

    public Double getS2() {
        double s2 = 0d;
        byte[] cmd = new byte[]{
            addressOnBus,
            (byte) 0x08,
            (byte) 0x11,
            (byte) 0x0A,
            (byte) 0x00,
            (byte) 0x00};
        if (!sendCMD(cmd)) {
            return null;
        }
        byte[] resp = getResponse();
        if (resp != null && resp.length == 6) {
            s2 += (resp[3] & 0xFF) << 8;
            s2 += (resp[2] & 0xFF);
            s2 /= 100;
            s2 *= Ki;
        } else {
            return null;
        }
        return s2;
    }

    public Double getS3() {
        double s3 = 0d;
        byte[] cmd = new byte[]{
            addressOnBus,
            (byte) 0x08,
            (byte) 0x11,
            (byte) 0x0B,
            (byte) 0x00,
            (byte) 0x00};
        if (!sendCMD(cmd)) {
            return null;
        }
        byte[] resp = getResponse();
        if (resp != null && resp.length == 6) {
            s3 += (resp[3] & 0xFF) << 8;
            s3 += (resp[2] & 0xFF);
            s3 /= 100;
            s3 *= Ki;
        } else {
            return null;
        }
        return s3;
    }

    public Double getSsum() {
        double ssum = 0d;
        byte[] cmd = new byte[]{
            addressOnBus,
            (byte) 0x08,
            (byte) 0x11,
            (byte) 0x08,
            (byte) 0x00,
            (byte) 0x00};
        if (!sendCMD(cmd)) {
            return null;
        }
        byte[] resp = getResponse();
        if (resp != null && resp.length == 6) {
            ssum += (resp[3] & 0xFF) << 8;
            ssum += (resp[2] & 0xFF);
            ssum /= 100;
            ssum *= Ki;
        } else {
            return null;
        }
        return ssum;
    }

    public AplusRplus getAplusRplusFromReset() {
        double aplus = 0d;
        double rplus = 0d;
        byte[] cmd = new byte[]{
            addressOnBus,
            (byte) 0x05,
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0x00};
        if (!sendCMD(cmd)) {
            return null;
        }
        byte[] resp = getResponse();
        if (resp != null) {
            aplus += (resp[2] & 0xFF) << 24;
            aplus += (resp[1] & 0xFF) << 16;
            aplus += (resp[4] & 0xFF) << 8;
            aplus += (resp[3] & 0xFF);
            aplus /= 1000;
            aplus *= Ki;
            rplus += (resp[10] & 0xFF) << 24;
            rplus += (resp[9] & 0xFF) << 16;
            rplus += (resp[12] & 0xFF) << 8;
            rplus += (resp[11] & 0xFF);
            rplus /= 1000;
            rplus *= Ki;
        } else {
            return null;
        }
        return new AplusRplus(aplus, rplus);
    }

    public AplusRplus getAplusRplusYear() {
        double aplus = 0d;
        double rplus = 0d;
        byte[] cmd = new byte[]{
            addressOnBus,
            (byte) 0x05,
            (byte) 0x10,
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0x00};
        if (!sendCMD(cmd)) {
            return null;
        }
        byte[] resp = getResponse();
        if (resp != null) {
            aplus += (resp[2] & 0xFF) << 24;
            aplus += (resp[1] & 0xFF) << 16;
            aplus += (resp[4] & 0xFF) << 8;
            aplus += (resp[3] & 0xFF);
            aplus /= 1000;
            aplus *= Ki;
            rplus += (resp[10] & 0xFF) << 24;
            rplus += (resp[9] & 0xFF) << 16;
            rplus += (resp[12] & 0xFF) << 8;
            rplus += (resp[11] & 0xFF);
            rplus /= 1000;
            rplus *= Ki;
        } else {
            return null;
        }
        return new AplusRplus(aplus, rplus);
    }

    public AplusRplus getAplusRplusMonth(int month) {
        double aplus = 0d;
        double rplus = 0d;
        byte[] cmd = new byte[]{
            addressOnBus,
            (byte) 0x05,
            (byte) 0x30,
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0x00};
        cmd[2] |= month;
        if (!sendCMD(cmd)) {
            return null;
        }
        byte[] resp = getResponse();
        if (resp != null && resp.length == 19) {
            aplus += (resp[2] & 0xFF) << 24;
            aplus += (resp[1] & 0xFF) << 16;
            aplus += (resp[4] & 0xFF) << 8;
            aplus += (resp[3] & 0xFF);
            aplus *= Ki;
            aplus /= 1000;
            rplus += (resp[10] & 0xFF) << 24;
            rplus += (resp[9] & 0xFF) << 16;
            rplus += (resp[12] & 0xFF) << 8;
            rplus += (resp[11] & 0xFF);
            rplus /= 1000;
            rplus *= Ki;
        } else {
            return null;
        }
        return new AplusRplus(aplus, rplus);
    }

    /**
     * Moves the address forward.
     * @param month 1 = January / 12 = December
     * @return AplusRplus
     */
    public AplusRplus getAplusRplusMonthBegining(int month) {
        double aplus = 0d;
        double rplus = 0d;
        int addr = 0x0255 + month * 0x55;
        byte addrHi = (byte) (addr >> 8);
        byte addrLo = (byte) addr;
        byte[] cmd = new byte[]{
            addressOnBus,
            (byte) 0x06,
            (byte) 0x02,
            addrHi,
            addrLo,
            (byte) 0x10,
            (byte) 0x00,
            (byte) 0x00};
        if (!sendCMD(cmd)) {
            return null;
        }
        byte[] resp = getResponse();
        if (resp != null) {
            aplus += (resp[2] & 0xFF) << 24;
            aplus += (resp[1] & 0xFF) << 16;
            aplus += (resp[4] & 0xFF) << 8;
            aplus += (resp[3] & 0xFF);
            aplus *= Ki;
            aplus /= 2000;
            rplus += (resp[10] & 0xFF) << 24;
            rplus += (resp[9] & 0xFF) << 16;
            rplus += (resp[12] & 0xFF) << 8;
            rplus += (resp[11] & 0xFF);
            rplus /= 2000;
            rplus *= Ki;
        } else {
            return null;
        }
        return new AplusRplus(aplus, rplus);
    }

    public AplusRplus getAplusRplusNowDay() {
        double aplus = 0d;
        double rplus = 0d;
        byte[] cmd = new byte[]{
            addressOnBus,
            (byte) 0x05,
            (byte) 0x40,
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0x00};
        if (!sendCMD(cmd)) {
            return null;
        }
        byte[] resp = getResponse();
        if (resp != null && resp.length == 19) {
            aplus += (resp[2] & 0xFF) << 24;
            aplus += (resp[1] & 0xFF) << 16;
            aplus += (resp[4] & 0xFF) << 8;
            aplus += (resp[3] & 0xFF);
            aplus *= Ki;
            aplus /= 1000;
            rplus += (resp[10] & 0xFF) << 24;
            rplus += (resp[9] & 0xFF) << 16;
            rplus += (resp[12] & 0xFF) << 8;
            rplus += (resp[11] & 0xFF);
            rplus /= 1000;
            rplus *= Ki;
        } else {
            return null;
        }
        return new AplusRplus(aplus, rplus);
    }

    public AplusRplus getAplusRplusNowDayBegining() {
        double aplus = 0d;
        double rplus = 0d;
        int addr = 0x06A6;
        byte addrHi = (byte) (addr >> 8);
        byte addrLo = (byte) addr;
        byte[] cmd = new byte[]{
            addressOnBus,
            (byte) 0x06,
            (byte) 0x02,
            addrHi,
            addrLo,
            (byte) 0x10,
            (byte) 0x00,
            (byte) 0x00};
        if (!sendCMD(cmd)) {
            return null;
        }
        byte[] resp = getResponse();
        if (resp != null) {
            aplus += (resp[2] & 0xFF) << 24;
            aplus += (resp[1] & 0xFF) << 16;
            aplus += (resp[4] & 0xFF) << 8;
            aplus += (resp[3] & 0xFF);
            aplus *= Ki;
            aplus /= 2000;
            rplus += (resp[10] & 0xFF) << 24;
            rplus += (resp[9] & 0xFF) << 16;
            rplus += (resp[12] & 0xFF) << 8;
            rplus += (resp[11] & 0xFF);
            rplus /= 2000;
            rplus *= Ki;
        } else {
            return null;
        }
        return new AplusRplus(aplus, rplus);
    }

    public AplusRplus getAplusRplusPrevDay() {
        double aplus = 0d;
        double rplus = 0d;
        byte[] cmd = new byte[]{
            addressOnBus,
            (byte) 0x05,
            (byte) 0x50,
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0x00};
        if (!sendCMD(cmd)) {
            return null;
        }
        byte[] resp = getResponse();
        if (resp != null && resp.length == 19) {
            aplus += (resp[2] & 0xFF) << 24;
            aplus += (resp[1] & 0xFF) << 16;
            aplus += (resp[4] & 0xFF) << 8;
            aplus += (resp[3] & 0xFF);
            aplus *= Ki;
            aplus /= 1000;
            rplus += (resp[10] & 0xFF) << 24;
            rplus += (resp[9] & 0xFF) << 16;
            rplus += (resp[12] & 0xFF) << 8;
            rplus += (resp[11] & 0xFF);
            rplus /= 1000;
            rplus *= Ki;
        } else {
            return null;
        }
        return new AplusRplus(aplus, rplus);
    }

    public Byte getMeterAddress() {
        if (addressOnBus == 0) {
            byte[] cmd = new byte[]{
                addressOnBus,
                (byte) 0x08,
                (byte) 0x05,
                (byte) 0x00,
                (byte) 0x00};
            if (!sendCMD(cmd)) {
                return null;
            }
            byte[] resp = getResponse();
            if (resp != null) {
                return resp[2];
            } else {
                return null;
            }
        } else {
            return addressOnBus;
        }
    }

    public void setMeterSN(String meterSN) {
        this.serialNumber = meterSN;
    }

    public String getMeterSN() {
        if (this.serialNumber.length() == 0) {
            byte[] cmd = new byte[]{
                addressOnBus,
                (byte) 0x08,
                (byte) 0x00,
                (byte) 0x00,
                (byte) 0x00};
            if (!sendCMD(cmd)) {
                return null;
            }
            byte[] resp = getResponse();
            if (resp != null) {
                String str, s1, s2, s3, s4;
                s1 = String.valueOf(resp[1]);
                if (s1.length() == 1) {
                    s1 = "0" + s1;
                }
                s2 = String.valueOf(resp[2]);
                if (s2.length() == 1) {
                    s2 = "0" + s2;
                }
                s3 = String.valueOf(resp[3]);
                if (s3.length() == 1) {
                    s3 = "0" + s3;
                }
                s4 = String.valueOf(resp[4]);
                if (s4.length() == 1) {
                    s4 = "0" + s4;
                }
                str = s1 + s2 + s3 + s4;
                this.serialNumber = str;
                return str;
            } else {
                return null;
            }
        } else {
            return this.serialNumber;
        }
    }

    public Calendar getMeterTime() {
        byte[] cmd = new byte[]{
            addressOnBus,
            (byte) 0x04,
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0x00};
        if (!sendCMD(cmd)) {
            return null;
        }
        byte[] resp = getResponse();
        if (resp == null || resp.length != 11) {
            return null;
        }
        int year, month, day, hour, minute, second;
        year = Integer.parseInt("20" + Integer.toHexString(resp[7] & 0xFF));
        month = Integer.parseInt(Integer.toHexString(resp[6] & 0xFF));
        month--;
        day = Integer.parseInt(Integer.toHexString(resp[5] & 0xFF));
        hour = Integer.parseInt(Integer.toHexString(resp[3] & 0xFF));
        minute = Integer.parseInt(Integer.toHexString(resp[2] & 0xFF));
        second = Integer.parseInt(Integer.toHexString(resp[1] & 0xFF));
        return new GregorianCalendar(year, month, day, hour, minute, second);
    }

    private AvgAR getAvgARAtAddr(int addr) {
        addressInMem3 = addr;
        byte addrHflags = 0;
        if ((addr & 0x10000) == 0x10000) {
            addrHflags |= 128;
        }
        addrHflags |= 3;
        byte addrLo = (byte) addr;
        byte addrHi = (byte) (addr >> 8);
        byte[] cmd = new byte[]{
            addressOnBus,
            (byte) 0x06,
            (byte) addrHflags,
            (byte) addrHi,
            (byte) addrLo,
            (byte) 0x0F,
            (byte) 0x00,
            (byte) 0x00};
        if (!sendCMD(cmd)) {
            return null;
        }
        byte[] resp = getResponse();
        if (resp == null || resp.length < 16) {
            return null;
        }

        int arPeriod = resp[7] == 0 ? -1 : resp[7];

        double Aplus = 0;
        short AplusS = (short) ((resp[9] & 0xFF) << 8);
        AplusS |= resp[8] & 0xFF;
        if ((AplusS & 0xFFFF) != 0xFFFF) {
            Aplus = AplusS & 0xFFFF;
        }
        Aplus *= 60 / arPeriod;
        Aplus /= 2;
        Aplus /= meterConst;
        Aplus *= Ki;

        double Aminus = 0;
        short AminusS = (short) ((resp[11] & 0xFF) << 8);
        AminusS |= resp[10] & 0xFF;
        if ((AminusS & 0xFFFF) != 0xFFFF) {
            Aminus = AminusS & 0xFFFF;
        }
        Aminus *= 60 / arPeriod;
        Aminus /= 2;
        Aminus /= meterConst;
        Aminus *= Ki;

        double Rplus = 0;
        short RplusS = (short) ((resp[13] & 0xFF) << 8);
        RplusS |= resp[12] & 0xFF;
        if ((RplusS & 0xFFFF) != 0xFFFF) {
            Rplus = RplusS & 0xFFFF;
        }
        Rplus *= 60 / arPeriod;
        Rplus /= 2;
        Rplus /= meterConst;
        Rplus *= Ki;

        double Rminus = 0;
        short RminusS = (short) ((resp[15] & 0xFF) << 8);
        RminusS |= resp[14] & 0xFF;
        if ((RminusS & 0xFFFF) != 0xFFFF) {
            Rminus = RminusS & 0xFFFF;
        }
        Rminus *= 60 / arPeriod;
        Rminus /= 2;
        Rminus /= meterConst;
        Rminus *= Ki;

        int year, month, day, hour, minute;
        if (resp[6] != 0) {
            year = Integer.parseInt("20" + Integer.toHexString(resp[6] & 0xFF));
            month = Integer.parseInt(Integer.toHexString(resp[5] & 0xFF));
            month--;
            day = Integer.parseInt(Integer.toHexString(resp[4] & 0xFF));
            hour = Integer.parseInt(Integer.toHexString(resp[2] & 0xFF));
            minute = Integer.parseInt(Integer.toHexString(resp[3] & 0xFF));
        } else {
            year = 0;
            month = 0;
            day = 1;
            hour = 0;
            minute = 0;
        }

        return new AvgAR(Aplus, Aminus, Rplus, Rminus, new GregorianCalendar(year, month, day, hour, minute), arPeriod);
    }

    public AvgAR getAvgARLast() {
        byte[] cmd = {
            addressOnBus,
            (byte) 0x08,
            (byte) 0x13,
            (byte) 0x00,
            (byte) 0x00};
        if (!sendCMD(cmd)) {
            return null;
        }
        byte[] resp = getResponse();

        overloadBit = (resp[3] & 1) == 1;

        int address = 0;

        if ((resp[1] & 16) == 16) {
            address |= 0x10000;
        }

        byte adrHi = resp[1];
        byte adrLo = resp[2];
        short adrHL = (short) ((adrHi & 0xFF) << 8);
        adrHL |= adrLo & 0xFF;
        adrHL = (short) (adrHL << 4);
        address |= adrHL & 0xFFFF;
        return getAvgARAtAddr(address);
    }

    public AvgAR getAvgARPrev() {
        if (addressInMem3 < 0x10) {
            // Сделать проверку на количество памяти счетчика
            return getAvgARAtAddr(0x1FFF0);
        } else {
            return getAvgARAtAddr(addressInMem3 - 0x10);
        }
    }

    public AvgAR getAvgARNext() {
        if (addressInMem3 >= 0x1FFF0) {
            // Сделать проверку на количество памяти счетчика
            return getAvgARAtAddr(0x00000);
        } else {
            return getAvgARAtAddr(addressInMem3 + 0x10);
        }
    }

    public AvgAR getAvgARCurr() {
        return getAvgARAtAddr(addressInMem3);
    }

    public int getDeepInMins() {
        AvgAR aar = getAvgARLast();
        if (overloadBit) {
            return 8192 * aar.getArPeriod();
        }
        return addressInMem3 * aar.getArPeriod();
    }

    /**
     *
     * @return
     */
    public int getAddressInMem3() {
        return addressInMem3;
    }

    /**
     *
     * @param addr
     */
    public void setAddressInMem3(int addr) {
        addressInMem3 = addr;
    }

    /**
     *
     */
    public void setAddressInMem3ToLast() {
        byte[] cmd = {
            addressOnBus,
            (byte) 0x08,
            (byte) 0x13,
            (byte) 0x00,
            (byte) 0x00};
        if (!sendCMD(cmd)) {
            return;
        }
        byte[] resp = getResponse();

        overloadBit = (resp[3] & 1) == 1;

        int address = 0;

        if ((resp[1] & 16) == 16) {
            address |= 0x10000;
        }

        byte adrHi = resp[1];
        byte adrLo = resp[2];
        short adrHL = (short) ((adrHi & 0xFF) << 8);
        adrHL |= adrLo & 0xFF;
        adrHL = (short) (adrHL << 4);
        address |= adrHL & 0xFFFF;

        addressInMem3 = address;
    }

    /**
     * Moves the address backward.
     *
     * @param count how many times to move.
     */
    public void setAddressInMem3ToPrev(int count) {
        for (int i = 0; i < count; i++) {
            if (addressInMem3 < 0x10) {
                // Сделать проверку на количество памяти счетчика
                addressInMem3 = 0x1FFF0;
                continue;
            }
            addressInMem3 -= 0x10;
        }
    }

    /**
     * Moves the address forward.
     *
     * @param count how many times to move.
     */
    public void setAddressInMem3ToNext(int count) {
        for (int i = 0; i < count; i++) {
            if (addressInMem3 >= 0x1FFF0) {
                // Сделать проверку на количество памяти счетчика
                addressInMem3 = 0x00000;
                continue;
            }
            addressInMem3 += 0x10;
        }
    }

    /**
     * Moves the address to the date.
     *
     * @param ca Calendar
     */
    public void setAddressInMem3ToDate(Calendar ca) {
        Calendar caCur = getAvgARCurr().getArDT();
        Long mills = ca.getTimeInMillis() - caCur.getTimeInMillis();
        boolean Forward = mills > 0;
        mills = Math.abs(mills);
        mills /= 1800000;
        if (Forward) {
            setAddressInMem3ToNext(mills.intValue());
        } else {
            setAddressInMem3ToPrev(mills.intValue());
        }
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
