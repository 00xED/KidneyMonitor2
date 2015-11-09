package com.example.zhilo.kidneymonitor2;

import android.os.Environment;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class ProcedureSettings {
    private static ProcedureSettings mInstance = null;

    //Initialisation of LogWriter
    private static final String TAG = "ProcedureSettings";
    LogWriter lw;

    private int dialPump1Flow, dialPump2Flow, dialPump3Flow;

    private float dialPress1Min;
    private float dialPress1;
    private float dialPress1Max;
    private float dialPress2Min;
    private float dialPress2;
    private float dialPress2Max;
    private float dialPress3Min;
    private float dialPress3;
    private float dialPress3Max;

    private float dialTemp1Min;
    private float dialTemp1;
    private float dialTemp1Max;

    private float dialCond1Min;
    private float dialCond1;
    private float dialCond1Max;

    private float dialCurrent1, dialCurrent2, dialCurrent3, dialCurrent4;

    private int fillPump1Flow, fillPump2Flow, fillPump3Flow;

    private int flushPump1Flow;
    private int flushPump2Flow;
    private int flushPump3Flow;

    private int battery;
    private int procedure;
    private int procedure_previous;
    private int proc_parameters;
    private int dev_funct;
    private int sorbtime;
    private long last_connection;

    public static ProcedureSettings getInstance(){
        if(mInstance == null)
        {
            mInstance = new ProcedureSettings();
        }
        return mInstance;
    }

    public int getDialPump1Flow(){
        return this.dialPump1Flow;
    }
    public int getDialPump2Flow(){
        return this.dialPump2Flow;
    }
    public int getDialPump3Flow(){
        return this.dialPump3Flow;
    }
    public int getFillPump1Flow(){
        return this.fillPump1Flow;
    }
    public int getFillPump2Flow(){
        return this.fillPump2Flow;
    }
    public int getFillPump3Flow(){
        return this.fillPump3Flow;
    }
    public int getFlushPump1Flow(){
        return this.flushPump1Flow;
    }
    public int getFlushPump2Flow(){
        return this.flushPump2Flow;
    }
    public int getFlushPump3Flow(){
        return this.flushPump3Flow;
    }
    public float getDialPress1Min(){
        return this.dialPress1Min;
    }
    public float getDialPress1Max(){
        return this.dialPress1Max;
    }
    public float getDialPress2Min(){
        return this.dialPress2Min;
    }
    public float getDialPress2Max(){
        return this.dialPress2Max;
    }
    public float getDialPress3Min(){
        return this.dialPress3Min;
    }
    public float getDialPress3Max(){
        return this.dialPress3Max;
    }
    public float getDialTemp1Min(){
        return this.dialTemp1Min;
    }
    public float getDialTemp1Max(){
        return this.dialTemp1Max;
    }
    public float getDialCond1Min(){
        return this.dialCond1Min;
    }
    public float getDialCond1Max(){
        return this.dialCond1Max;
    }
    public float getDialPress1() {
        return this.dialPress1;
    }
    public float getDialPress2() {
        return this.dialPress2;
    }
    public float getDialPress3() {
        return this.dialPress3;
    }
    public float getDialTemp1() {
        return this.dialTemp1;
    }
    public float getDialCond1() {
        return this.dialCond1;
    }
    public float getDialCurrent1() {
        return this.dialCurrent1;
    }
    public float getDialCurrent2() {
        return this.dialCurrent2;
    }
    public float getDialCurrent3() {
        return this.dialCurrent3;
    }
    public float getDialCurrent4() {
        return this.dialCurrent4;
    }
    public int getBattery() {
        return this.battery;
    }
    public int getProcedure() {
        return this.procedure;
    }
    public int getProcedure_previous() {
        return this.procedure_previous;
    }
    public int getProc_parameters() {
        return this.proc_parameters;
    }
    public int getDev_funct() {
        return this.dev_funct;
    }
    public int getSorbtime() {
        return this.sorbtime;
    }
    public long getLast_connection() {
        return this.last_connection;
    }
    public void setDialPress1(float dialPress1) {
        this.dialPress1 = dialPress1;
    }
    public void setDialPress2(float dialPress2) {
        this.dialPress2 = dialPress2;
    }
    public void setDialPress3(float dialPress3) {
        this.dialPress3 = dialPress3;
    }
    public void setDialTemp1(float dialTemp1) {
        this.dialTemp1 = dialTemp1;
    }
    public void setDialCond1(float dialCond1) {
        this.dialCond1 = dialCond1;
    }
    public void setDialCurrent1(float dialCurrent1) {
        this.dialCurrent1 = dialCurrent1;
    }
    public void setDialCurrent2(float dialCurrent2) {
        this.dialCurrent2 = dialCurrent2;
    }
    public void setDialCurrent3(float dialCurrent3) {
        this.dialCurrent3 = dialCurrent3;
    }
    public void setDialCurrent4(float dialCurrent4) {
        this.dialCurrent1 = dialCurrent4;
    }
    public void setBattery(int battery) {
        this.battery = battery;
    }
    public void setProcedure(int procedure) {
        this.procedure = procedure;
    }
    public void setProcedure_previous(int procedure_previous) {
        this.procedure_previous = procedure_previous;
    }
    public void setProc_parameters(int proc_parameters) {
        this.proc_parameters = proc_parameters;
    }
    public void setDev_funct(int dev_funct) {
        this.dev_funct = dev_funct;
    }
    public void setSorbtime(int sorbtime) {
        this.sorbtime = sorbtime;
    }
    public void setLast_connection(long last_connection) {
        this.last_connection = last_connection;
    }

    private ProcedureSettings() {
        lw = new LogWriter();
        dialPump1Flow = 0;
        dialPump2Flow = 0;
        dialPump3Flow = 0;

        dialPress1Min = 0.0f;
        dialPress1 = 0.0f;
        dialPress1Max = 0.0f;
        dialPress2Min = 0.0f;
        dialPress2 = 0.0f;
        dialPress2Max = 0.0f;
        dialPress3Min = 0.0f;
        dialPress3 = 0.0f;
        dialPress3Max = 0.0f;

        dialTemp1Min = 0.0f;
        dialTemp1 = 0.0f;
        dialTemp1Max = 0.0f;

        dialCond1Min = 0.0f;
        dialCond1 = 0.0f;
        dialCond1Max = 0.0f;

        fillPump1Flow = 0;
        fillPump2Flow = 0;
        fillPump3Flow = 0;

        flushPump1Flow = 0;
        flushPump2Flow = 0;
        flushPump3Flow = 0;

        battery = Constants.PARAMETER_UNKNOWN;
        procedure = Constants.PARAMETER_UNKNOWN;
        procedure_previous = Constants.PARAMETER_UNKNOWN;
        proc_parameters = Constants.PARAMETER_UNKNOWN;
        dev_funct = Constants.PARAMETER_UNKNOWN;
        sorbtime = Constants.PARAMETER_UNKNOWN;
        last_connection = Constants.PARAMETER_UNKNOWN;

        try {
            FileInputStream fstream = new FileInputStream(Environment.getExternalStorageDirectory().getPath() + "/" + Constants.procedureSettingsFile);//Read from file
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            String strLine;
            //Read File Line By Lined
            while ((strLine = br.readLine()) != null) {//Reading file line by line
                if (isStringCorrect(strLine)) {//If string is not a comment

                    String snumber = strLine.substring(strLine.indexOf("=") + 1, strLine.indexOf(":"));//Get channel #
                    int number = Integer.valueOf(snumber);

                    String svalue = strLine.substring(strLine.indexOf(":") + 1, strLine.indexOf(";"));//Get value

                    int ivalue = 0;
                    float fvalue = 0f;
                    if (svalue.contains(".")) //if float - convert to float
                        fvalue = Float.parseFloat(svalue);
                    else//otherwise convert to int
                        ivalue = Integer.parseInt(svalue);

                    String setting = strLine.substring(0, strLine.indexOf("="));//Get command itself
                    RequestTypeSettings reqSetting = RequestTypeSettings.getType(setting);//Get enum type

                    switch (reqSetting) {
                        case dialPump: {//dialysis pump #number value
                            if (number == 1) dialPump1Flow = ivalue;
                            if (number == 2) dialPump2Flow = ivalue;
                            if (number == 3) dialPump3Flow = ivalue;
                            break;
                        }

                        case dialPress: {//dialysis pressure #number value
                            if (number == 1) dialPress1Max = fvalue;
                            if (number == 2) dialPress1Max = fvalue;
                            if (number == 3) dialPress2Min = fvalue;
                            if (number == 4) dialPress2Max = fvalue;
                            if (number == 5) dialPress3Min = fvalue;
                            if (number == 6) dialPress3Max = fvalue;
                            break;
                        }

                        case dialCond: {//dialysis conductivity #number value
                            if (number == 1) dialCond1Min = fvalue;
                            if (number == 2) dialCond1Max = fvalue;
                            break;
                        }

                        case dialTemp: {//dialysis temperature #number value
                            if (number == 1) dialTemp1Min = fvalue;
                            if (number == 2) dialTemp1Max = fvalue;
                            break;
                        }
                        case dialCur: {//dialysis current #number value
                            //sendMessageBytes(bSETDCUR, bnumber, bvalue);
                            //lw.appendLog(logTag, "set dCur#" + number + " to " + svalue);
                            break;
                        }
                        case fillPump: {//filling pump #number value
                            if (number == 1) fillPump1Flow = ivalue;
                            if (number == 2) fillPump2Flow = ivalue;
                            if (number == 3) fillPump3Flow = ivalue;
                            break;
                        }
                        case flushPump: {//Send unfilling pump #number value
                            if (number == 1) flushPump1Flow = ivalue;
                            if (number == 2) flushPump2Flow = ivalue;
                            if (number == 3) flushPump3Flow = ivalue;
                            break;
                        }

                        default:
                            break;
                    }
                }
            }

            //Close the input stream
            lw.appendLog(TAG, "Settings file read complete");
            br.close();
        } catch (Exception e) {
            lw.appendLog(TAG, e.toString() + " while reading settings file"); // handle exception
        }
    }

    /**
     * Check if string in settings file is in desired format
     *
     * @param strLine string to check
     * @return true if string is correct
     */
    private Boolean isStringCorrect(String strLine) {
        strLine = strLine.toLowerCase();
        return (!strLine.startsWith(";") && strLine.endsWith(";") &&
                strLine.contains("=") && strLine.contains(":") &&
                (strLine.contains("dialpump") || strLine.contains("dialpress") || strLine.contains("dialcond") || strLine.contains("dialtemp") ||
                        strLine.contains("dialcur") || strLine.contains("fillpump") || strLine.contains("flushpump")));
    }

    /**
     * Enumerator for reading settings file
     */
    private enum RequestTypeSettings {

        dialPump("dialPump"),
        dialPress("dialPress"),
        dialCond("dialCond"),
        dialTemp("dialTemp"),
        dialCur("dialCur"),
        fillPump("fillPump"),
        flushPump("flushPump"),
        A0("0"), A1("1"), A2("2"), A3("3"), A4("4"), A5("5"), A6("6"), A7("7"), A8("8"), A9("9");

        private String typeValue;

        RequestTypeSettings(String type) {
            typeValue = type;
        }

        static public RequestTypeSettings getType(String pType) {
            for (RequestTypeSettings type : RequestTypeSettings.values()) {
                if (type.getTypeValue().equals(pType)) {
                    return type;
                }
            }
            throw new RuntimeException("unknown type");
        }

        public String getTypeValue() {
            return typeValue;
        }
    }
}
