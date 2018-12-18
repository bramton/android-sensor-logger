package nl.baozi.sensorlogger;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

import java.util.ArrayList;

public class BramSensorEventListener implements SensorEventListener {
    private static final String TAG = "blaat.main";
    // For determining the sample rate
    private static final int N = 1000;
    private long ts_prev = 0;
    private long dt_sum = 0;
    private ArrayList<Long> dts = new ArrayList<Long>();

    // Keep last measured value
    float lastVal[] = new float[3];

    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO: handle this
        Log.i(TAG, "Accuracy changed");
    }

    public final void onSensorChanged(SensorEvent se) {
        if (ts_prev == 0) {
            ts_prev = se.timestamp;
        }
        // Delay between two samples
        long dt = se.timestamp - ts_prev;
        if (dts.size() == N) {
            dt_sum -= dts.remove(0);
        }
        dts.add(dt);
        dt_sum += dt;

        // Update previous timestamp
        ts_prev = se.timestamp;
        lastVal[0]  = se.values[0];
        lastVal[1]  = se.values[1];
        lastVal[2]  = se.values[2];
    }

    public int getSampleRate() {
        // Average time delay [ns]
        float T = dt_sum/dts.size();
        // Convert to seconds
        T /= 1e9;
        // Return as frequency
        return (int) (1 / T);
    }

    public float[] getLastVal() {
        return lastVal;
    }
}
