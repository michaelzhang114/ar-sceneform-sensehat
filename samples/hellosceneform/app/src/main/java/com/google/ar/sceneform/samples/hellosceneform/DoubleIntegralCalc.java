package com.google.ar.sceneform.samples.hellosceneform;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DoubleIntegralCalc {

    private Double[] pos = {0.0, 0.0, 0.0};
    private Double[] vel = {0.0, 0.0, 0.0};
    private Double[] speed = {0.0, 0.0, 0.0};
    private Double[] acc = new Double[3];
    private Date time = null;

    public DoubleIntegralCalc() {}

    public void addNewAccReading(Double[] newAcc, String newTime) throws Exception {
        SimpleDateFormat sdt = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        Date nt = sdt.parse(newTime);
        calcs(newAcc, nt);
    }

    public void printAll() {
        System.out.println("POS");
        printList(pos);

        System.out.println("VEL");
        printList(vel);

        System.out.println("SPEED");
        printList(speed);

        System.out.println("ACC");
        printList(acc);

        System.out.println(time.getTime());
    }

    public Double[] getPos() {
        return pos;
    }

    private void calcs(Double[] newAcc, Date newTime) {
        //initialize the time
        if (time == null) {
            time = newTime;
        }

        // new vel calc
        Double[] velNext = new Double[3];
        velNext[0] = vel[0] + newAcc[0] * (getTimeDiffInSeconds(newTime, time));
        velNext[1] = vel[1] + newAcc[1] * (getTimeDiffInSeconds(newTime, time));
        velNext[2] = vel[2] + newAcc[2] * (getTimeDiffInSeconds(newTime, time));

        // new speed calc
        Double[] speedNext = new Double[3];
        speedNext[0] = (vel[0] + velNext[0]) / 2;
        speedNext[1] = (vel[1] + velNext[1]) / 2;
        speedNext[2] = (vel[2] + velNext[2]) / 2;

        // new pos calc
        Double[] posNext = new Double[3];
        posNext[0] = pos[0] + speedNext[0] * getTimeDiffInSeconds(newTime, time);
        posNext[1] = pos[1] + speedNext[1] * getTimeDiffInSeconds(newTime, time);
        posNext[2] = pos[2] + speedNext[2] * getTimeDiffInSeconds(newTime, time);

        // update everything
        vel = velNext;
        speed = speedNext;
        pos = posNext;
        acc = newAcc;
        time = newTime;
    }

    private Double getTimeDiffInSeconds(Date newTime, Date oldTime) {
        return (newTime.getTime()-oldTime.getTime())/1000.0;
    }

    private void printList(Double[] tmp) {
        for (Double d : tmp) {
            System.out.print(d + ", ");
        }
        System.out.println();
    }


}
