package gwicks.com.earswatch;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static android.content.Context.SENSOR_SERVICE;

public class Sensors implements SensorEventListener {

    private static final String TAG = "Sensors";


    SensorManager mSensorManager;
    Sensor mHeartRateSensor;
    Sensor mAccel;
    Sensor mGyro;
    SensorEventListener sensorEventListener;

    Context mContext;

    private static long LAST_TS_ACC = 0;
    private static long LAST_TS_GYRO = 0;

    StringBuilder accelBuffer;
    StringBuilder gryoBuffer;
    StringBuilder lightBuffer;
    StringBuilder heartBuffer;

    private boolean writingAccelToFile = false;
    private boolean writingGyroToFile = false;
    private boolean writingLightToFile = false;

    float previousLightReading;

    private static Float[] LAST_VALUES_ACC = null;
    private static Float[] LAST_VALUES_GRYO = null;

    double THRESHOLD = 0.01;
    double ACCEL_THRESHOLD = 0.05;
    float lightReading = 0;
    long timeStampLight = 0;

    File AccelFile;
    File GyroFile;
    File LightFile;
    File DestroyFile;
    String folder;
    File heartFile;
    String heartOutFile;

    public Sensors(Context context) {

        Log.d(TAG, "Sensors: in constructor");
        mContext = context;
        mSensorManager = ((SensorManager) mContext.getSystemService(SENSOR_SERVICE));
        mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        mAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mAccel, SensorManager.SENSOR_DELAY_NORMAL);

        mSensorManager.registerListener(this, mGyro, SensorManager.SENSOR_DELAY_NORMAL);



        mSensorManager.registerListener(sensorEventListener, mHeartRateSensor, mSensorManager.SENSOR_DELAY_FASTEST);

        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor sensor : sensors) {
            Log.d("Sensors", "" + sensor.getName());
            Log.d("List sensors", "Name: "+sensor.getName() + " /Type_String: " +sensor.getStringType()+ " /Type_number: "+sensor.getType());
        }

        accelBuffer = new StringBuilder();
        gryoBuffer = new StringBuilder();
        lightBuffer = new StringBuilder();
        heartBuffer = new StringBuilder();

        folder = mContext.getFilesDir().toString();

        heartOutFile = mContext.getExternalFilesDir(null).toString();
        Log.d(TAG, "Sensors: heartOutFIle: " + heartOutFile);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long TS = System.currentTimeMillis();
            //Log.d(TAG, "onSensorChanged: The time stamp check is:  " + TS +" + " + LAST_TS_ACC );

            // Filter to remove readings that come too often
            if (TS < LAST_TS_ACC + 100) {
                //Log.d(TAG, "onSensorChanged: skipping");
                return;
            }

            if (LAST_VALUES_ACC != null && Math.abs(event.values[0] - LAST_VALUES_ACC[0]) < ACCEL_THRESHOLD
                    && Math.abs(event.values[1] - LAST_VALUES_ACC[1]) < ACCEL_THRESHOLD
                    && Math.abs(event.values[2] - LAST_VALUES_ACC[2]) < ACCEL_THRESHOLD) {
                return;
            }

            LAST_VALUES_ACC = new Float[]{event.values[0], event.values[1], event.values[2]};

            LAST_TS_ACC = System.currentTimeMillis();

            accelBuffer.append(LAST_TS_ACC + "," + event.values[0] + "," + event.values[1] + "," + event.values[2] + "\n");
            Log.d(TAG, "onSensorChanged: \n the buffer Accel length is: " + accelBuffer.length());
            //Log.d(TAG, "onSensorChanged: the buffer is: " + accelBuffer.toString());
            if ((accelBuffer.length() > 5000) && (writingAccelToFile == false)) {
                writingAccelToFile = true;

                AccelFile = new File(folder + "/" + LAST_TS_ACC + "_AccelService.txt");
                Log.d(TAG, "onSensorChanged: accelfile created at : " + AccelFile.getPath());

//                File parent = AccelFile.getParentFile();
//                if (!parent.exists() && !parent.mkdirs()) {
//                    throw new IllegalStateException("Couldn't create directory: " + parent);
//                }

                //Try threading to take of UI thread

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //Log.d(TAG, "onSensorChanged: in accelbuffer");
                        // Log.d(TAG, "run: in runnable");
                        //writeToStream(accelBuffer);
                        writeStringBuilderToFile(AccelFile, accelBuffer);
                        accelBuffer.setLength(0);
                        writingAccelToFile = false;

                    }
                }).start();

            }
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            // Log.d(TAG, "onSensorChanged: gyro");


            long TS = System.currentTimeMillis();
            if (TS < LAST_TS_GYRO + 100) {
                //Log.d(TAG, "onSensorChanged: skipping");
                return;
            }
            // Filter to remove readings that have too small a change from previous reading.
            if (LAST_VALUES_GRYO != null && Math.abs(event.values[0] - LAST_VALUES_GRYO[0]) < THRESHOLD
                    && Math.abs(event.values[1] - LAST_VALUES_GRYO[1]) < THRESHOLD
                    && Math.abs(event.values[2] - LAST_VALUES_GRYO[2]) < THRESHOLD) {
                return;
            }

            LAST_VALUES_GRYO = new Float[]{event.values[0], event.values[1], event.values[2]};


            LAST_TS_GYRO = System.currentTimeMillis();


            gryoBuffer.append(LAST_TS_GYRO + "," + event.values[0] + "," + event.values[1] + "," + event.values[2] + "\n");
            Log.d(TAG, "onSensorChanged: \n the buffer gyro length is: " + gryoBuffer.length());
            //Log.d(TAG, "onSensorChanged: the buffer is: " + accelBuffer.toString());
            if ((gryoBuffer.length() > 50000) && (writingGyroToFile == false)) {
                writingGyroToFile = true;

                GyroFile = new File(heartOutFile + "/" + LAST_TS_GYRO + "_GyroService.txt");
                Log.d(TAG, "onSensorChanged: file created at: " + GyroFile.getPath());

                File parent = GyroFile.getParentFile();
                if (!parent.exists() && !parent.mkdirs()) {
                    throw new IllegalStateException("Couldn't create directory: " + parent);
                }


                //Try threading to take of UI thread

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //Log.d(TAG, "onSensorChanged: in accelbuffer");
                        //Log.d(TAG, "run: in runnable");
                        //writeToStream(accelBuffer);
                        writeStringBuilderToFile(GyroFile, gryoBuffer);
                        gryoBuffer.setLength(0);
                        writingGyroToFile = false;

                    }
                }).start();

            }

        } else if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
            String msg = "" + (int) event.values[0];
            //mTextViewHeart.setText(msg);
            Log.d(TAG, msg);

            heartBuffer.append(LAST_TS_ACC + "," + event.values[0]  + "\n");
            Log.d(TAG, "onSensorChanged: \n the buffer Accel length is: " + heartBuffer.length());
            //Log.d(TAG, "onSensorChanged: the buffer is: " + accelBuffer.toString());
            if ((heartBuffer.length() > 50) && (writingAccelToFile == false)) {
                //writingAccelToFile = true;



                heartFile = new File(heartOutFile+ "/" + LAST_TS_ACC + "_HeartService1.txt");
                Log.d(TAG, "onSensorChanged: accelfile created at : " + AccelFile.getPath());

//                File parent = AccelFile.getParentFile();
//                if (!parent.exists() && !parent.mkdirs()) {
//                    throw new IllegalStateException("Couldn't create directory: " + parent);
//                }

                //Try threading to take of UI thread

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //Log.d(TAG, "onSensorChanged: in accelbuffer");
                        // Log.d(TAG, "run: in runnable");
                        //writeToStream(accelBuffer);
                        writeStringBuilderToFile(heartFile, heartBuffer);
                        heartBuffer.setLength(0);
                        writingAccelToFile = false;

                    }
                }).start();

            }




        } else
            Log.d(TAG, "Unknown sensor type");
        //Log.d(TAG, "onSensorChanged: event: " + event.sensor.getType());
        //Log.d(TAG, "onSensorChanged: sensor: " + event.values[0]);
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
