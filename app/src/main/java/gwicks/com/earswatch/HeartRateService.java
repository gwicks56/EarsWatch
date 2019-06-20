package gwicks.com.earswatch;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

public class HeartRateService extends JobService implements SensorEventListener {

    SensorManager mSensorManager;
    Sensor mHeartRateSensor;
    SensorEventListener sensorEventListener;
    StringBuilder heartBuffer;
    File heartFile;
    String folder;
    String heartOutFile;

    private static final String TAG = "HeartRateService";
    @Override
    public boolean onStartJob(JobParameters params) {

        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
        mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_STATUS_ACCURACY_HIGH);
        heartBuffer = new StringBuilder();
        Log.d(TAG, "onStartJob: ins start");
        folder = getFilesDir().toString();
        heartOutFile = getExternalFilesDir(null).toString();
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "onStopJob: on stop'");
        return false;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d(TAG, "onSensorChanged: changed");
        if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
            long TS = System.currentTimeMillis();
            heartBuffer.append(TS + "," + event.values[0]  + "\n");
            Log.d(TAG, "onSensorChanged: heart: " + event.values[0] );
            Log.d(TAG, "onSensorChanged: heartbuffer: " + heartBuffer.toString());
            if(heartBuffer.length() > 1){
                heartFile = new File(heartOutFile + "/" + TS + "_HeartService.txt");

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //Log.d(TAG, "onSensorChanged: in accelbuffer");
                        // Log.d(TAG, "run: in runnable");
                        //writeToStream(accelBuffer);
                        writeStringBuilderToFile(heartFile, heartBuffer);
                        heartBuffer.setLength(0);


                    }
                }).start();

            }

        }
        else if(event.sensor.getType() == Sensor.TYPE_HEART_BEAT){
            Log.d(TAG, "onSensorChanged: heart beat");
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void writeStringBuilderToFile(File file, StringBuilder builder) {
        Log.d(TAG, "writeStringBuilderToFile: in stringbuilder to file");
        BufferedWriter writer = null;


        try {
            writer = new BufferedWriter(new java.io.FileWriter((file)));
            Log.d(TAG, "writeStringBuilderToFile: writiting");
            writer.append(builder);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
