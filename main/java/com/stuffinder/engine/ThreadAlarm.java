package com.stuffinder.engine;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by nthproprio on 11/04/2015.
 *
 * This class is a kind of thread alarm. It interrupts a thread (after waiting a time) to wake it up.
 */
public class ThreadAlarm extends Thread
{
    private Thread threadToInterrupt;
    private long time;

    public ThreadAlarm(Thread threadToInterrupt, long time) {
        this.threadToInterrupt = threadToInterrupt;
        this.time = time;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(time);
            threadToInterrupt.interrupt();
            Logger.getLogger(getClass().getName()).log(Level.INFO, "Alarm slept for " + time + ". Thread \"" + threadToInterrupt + "\"will be interrupt to be wake up.");
        } catch (InterruptedException e) {
            Logger.getLogger(getClass().getName()).log(Level.INFO, "Alarm interrupted.");
        }
    }

    public void stopAlarm()
    {
        Logger.getLogger(getClass().getName()).log(Level.INFO, "Alarm will be interrupted.");
        interrupt();
    }
}
