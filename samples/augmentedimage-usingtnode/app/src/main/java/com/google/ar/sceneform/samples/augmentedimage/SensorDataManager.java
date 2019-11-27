package com.google.ar.sceneform.samples.augmentedimage;

import android.util.Log;

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


    public SensorDataManager() {
        this.numRequestsMade = 0;
        // TODO: do a one time request queue that grabs the header

    }

    public void addNewDataPoint(String[] data) {
        numRequestsMade++;
        prevData = currentData;
        currentData = data;



        Log.e("added new data point",  Integer.toString(numRequestsMade));
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

        //Log.e("currentDAta", Integer.toString(currentData.length));

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

    public boolean pitchUp() {
        int pitchIndex = findIndex(header, "pitch");
        double pitch = Double.valueOf(currentData[pitchIndex]);
        return (1 <= pitch && pitch <= 179);
    }

}
