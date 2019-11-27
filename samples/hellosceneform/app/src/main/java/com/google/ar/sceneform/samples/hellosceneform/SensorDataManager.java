package com.google.ar.sceneform.samples.hellosceneform;

import android.util.Log;

import java.util.Arrays;

public class SensorDataManager {

    private int numRequestsMade;
    private final double ACC_THRESHOLD = 2.3;
    private String[] prevData = {};
    private String[] currentData = {"2019-10-02 17:16:43.842364",
            "33.865814208984375", "1003.250732421875", "33.61266326904297",
            "234.5598739301857", "3.3857785593150345", "0.7045741936090419",
            "-10.0715913772583", "12.196593284606934", "36.64239501953125",
            "-0.0614", "0.0131", "1.0230",
            "0.0014440324157476425", "-0.0007706731557846069", "1.433398574590683e-05"};
    private String[] header = {"datetime",
            "temperature", "pressure", "humidity",
            "yaw", "pitch", "roll",
            "mag_x", "mag_y", "mag_z",
            "acc_x", "acc_y", "acc_z",
            "gyro_x", "gyro_y", "gyro_z"};

    private DoubleIntegralCalc doubleIntegralCalc;

    public SensorDataManager() {
        this.numRequestsMade = 0;
        doubleIntegralCalc = new DoubleIntegralCalc();
    }

    public void addNewDataPoint(String[] data) throws Exception {
        numRequestsMade++;
        prevData = currentData;
        currentData = data;
        Log.e("added new data point",  Integer.toString(numRequestsMade));


        int ind = findIndex(header, "datetime");

        int indx = findIndex(header, "acc_x");
        int indy = findIndex(header, "acc_y");
        int indz = findIndex(header, "acc_z");

        Double[] accArray = new Double[3];
        accArray[0] = Double.valueOf(currentData[indx]);
        accArray[1] = Double.valueOf(currentData[indy]);
        accArray[2] = Double.valueOf(currentData[indz]);

        String ttime = currentData[ind];
        ttime = ttime.replaceAll("\\\\", "");
        ttime = ttime.replaceAll("\\[", "");
        ttime = ttime.replaceAll("\"", "");

        Log.e("acc!!", Arrays.toString(accArray));
        Log.e("time", ttime);

        doubleIntegralCalc.addNewAccReading(accArray, ttime);
    }

    public Float[] getPositionVector() {
        Double[] arr = doubleIntegralCalc.getPos();
        Float[] out = new Float[arr.length];
        for (int i = 0; i < arr.length; i++) {
            out[i] = arr[i].floatValue();
        }
        return out;
    }

    public int getNumRequestsMade() {
        return numRequestsMade;
    }

    public boolean isShaking() {

        if (currentData == null) {
            return false;
        }
        // Get index of accelerometers
        int accIndexX = findIndex(header, "acc_x");
        int accIndexY = findIndex(header, "acc_y");
        int accIndexZ = findIndex(header, "acc_z");

        double accX = Double.valueOf(currentData[accIndexX]);
        double accY = Double.valueOf(currentData[accIndexY]);
        double accZ = Double.valueOf(currentData[accIndexZ]);

        Log.e("accMax", Double.toString(Math.max(Math.max(accX, accY), accZ)));
        return (accX > ACC_THRESHOLD || accY > ACC_THRESHOLD || accZ > ACC_THRESHOLD);
    }

    private int findIndex(String[] arr, String item) throws RuntimeException {
        for (int i = 0; i < arr.length; i++) {
            String curr = arr[i];
            if (curr.equals(item)) {
                return i;
            }
        }
        throw new RuntimeException();
    }

    public float orientationYaw() {
        double output = 0;
        int ind = findIndex(header, "yaw");
        double val = Double.valueOf(currentData[ind]);
        Log.e("orii yaw", Double.toString(val));
        double output_start = 0;
        double output_end = -360;
        double input_start = 1;
        double input_end = 360;
        output = output_start + ((output_end - output_start) / (input_end - input_start)) * (val - input_start);
        return (float) output;
    }

    public float orientationPitch() {
        double output = 0;
        int ind = findIndex(header, "pitch");
        double val = Double.valueOf(currentData[ind]);
        Log.e("orii pitch", Double.toString(val));
        double output_start = 0;
        double output_end = -360;
        double input_start = 1;
        double input_end = 360;
        output = output_start + ((output_end - output_start) / (input_end - input_start)) * (val - input_start);
        return (float) output;
    }

    public float orientationRoll() {
        double output = 0;
        int ind = findIndex(header, "roll");
        double val = Double.valueOf(currentData[ind]);
        Log.e("orii roll", Double.toString(val));
        double output_start = 0;
        double output_end = 360;
        double input_start = 1;
        double input_end = 360;
        output = output_start + ((output_end - output_start) / (input_end - input_start)) * (val - input_start);
        return (float) output;
    }

}
