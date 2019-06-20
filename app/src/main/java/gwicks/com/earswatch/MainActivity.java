package gwicks.com.earswatch;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class MainActivity extends WearableActivity{// implements SensorEventListener {

    private static final String TAG = "MainActivity";
    private TextView mTextViewHeart;
    SensorManager mSensorManager;
    Sensor mHeartRateSensor;
    Sensor mAccel;
    Sensor mGyro;
    SensorEventListener sensorEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextViewHeart = (TextView) findViewById(R.id.heart);

        Sensors mSensors = new Sensors(this);


        final JobInfo job = new JobInfo.Builder(1, new ComponentName(this, HeartRateService.class))
                .setPeriodic(TimeUnit.MINUTES.toMillis(15))
                .setPersisted(true)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .build();

        final JobScheduler jobScheduler =
                (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);

        jobScheduler.schedule(job);
        Log.d(TAG, "onCreate: Job Scehduled");

//        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
//        mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
//        mAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//        mGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
//        mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
//        mSensorManager.registerListener(this, mAccel, SensorManager.SENSOR_DELAY_NORMAL);
//
//        mSensorManager.registerListener(this, mGyro, SensorManager.SENSOR_DELAY_NORMAL);
//
//        Log.i(TAG, "LISTENER REGISTERED.");
//        mTextViewHeart.setText("Something here");
//
//
//        mSensorManager.registerListener(sensorEventListener, mHeartRateSensor, mSensorManager.SENSOR_DELAY_FASTEST);


    }

    public void listFiles(View v){
        Log.d(TAG, "listFiles: CLICKED!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        PackageManager m = getPackageManager();
        String s = getPackageName();
        try {
            PackageInfo p = m.getPackageInfo(s, 0);
            s = p.applicationInfo.dataDir;
        } catch (PackageManager.NameNotFoundException e) {
            Log.w("yourtag", "Error Package name not found ", e);
        }

        Context context = getApplicationContext();
        Log.d(TAG, "listFiles: 1");

        File folder = new File(context.getFilesDir().toString());
        Log.d(TAG, "listFiles: path: is : " + folder.toString());
        File[] listOfFiles = folder.listFiles();
        Log.d(TAG, "listFiles: 2");
        Log.d(TAG, "listFiles: length: " + listOfFiles.length);

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                Log.d(TAG, "listFiles: file is: " + listOfFiles[i]);
            } else if (listOfFiles[i].isDirectory()) {
                Log.d(TAG, "listFiles: directory");
                //System.out.println("Directory " + listOfFiles[i].getName());
            }
        }
    }


    public void onResume(){
        super.onResume();
    }

//    public void onAccuracyChanged(Sensor sensor, int accuracy) {
//        Log.d(TAG, "onAccuracyChanged - accuracy: " + accuracy);
//    }
//
//    public void onSensorChanged(SensorEvent event) {
//        if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
//            String msg = "" + (int)event.values[0];
//            mTextViewHeart.setText(msg);
//            Log.d(TAG, msg);
//        }
//        else
//            Log.d(TAG, "Unknown sensor type");
//            Log.d(TAG, "onSensorChanged: event: " + event.sensor.getType());
//        Log.d(TAG, "onSensorChanged: sensor: " + event.values[0]);
//    }

}