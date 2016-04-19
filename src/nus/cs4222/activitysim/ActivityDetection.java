package nus.cs4222.activitysim;

import java.io.*;
import java.util.*;
import java.text.*;

import android.hardware.*;
import android.util.*;

/**
   Class containing the activity detection algorithm.

   <p> You can code your activity detection algorithm in this class.
    (You may add more Java class files or add libraries in the 'libs' 
     folder if you need).
    The different callbacks are invoked as per the sensor log files, 
    in the increasing order of timestamps. In the best case, you will
    simply need to copy paste this class file (and any supporting class
    files and libraries) to the Android app without modification
    (in stage 2 of the project).

   <p> Remember that your detection algorithm executes as the sensor data arrives
    one by one. Once you have detected the user's current activity, output
    it using the {@link ActivitySimulator.outputDetectedActivity(UserActivities)}
    method. If the detected activity changes later on, then you need to output the
    newly detected activity using the same method, and so on.
    The detected activities are logged to the file "DetectedActivities.txt",
    in the same folder as your sensor logs.

   <p> To get the current simulator time, use the method
    {@link ActivitySimulator.currentTimeMillis()}. You can set timers using
    the {@link SimulatorTimer} class if you require. You can log to the 
    console/DDMS using either {@code System.out.println()} or using the
    {@link android.util.Log} class. You can use the {@code SensorManager.getRotationMatrix()}
    method (and any other helpful methods) as you would normally do on Android.

   <p> Note: Since this is a simulator, DO NOT create threads, DO NOT sleep(),
    or do anything that can cause the simulator to stall/pause. You 
    can however use timers if you require, see the documentation of the 
    {@link SimulatorTimer} class. 
    In the simulator, the timers are faked. When you copy the code into an
    actual Android app, the timers are real, but the code of this class
    does not need not be modified.
 */
public class ActivityDetection {

    /** Initialises the detection algorithm. */
    public void initDetection() 
        throws Exception {
        // Add initialisation code here, if any

        // Here, we just show a dummy example of a timer that runs every 10 min, 
        //  outputting WALKING and INDOOR alternatively.
        // You will most likely not need to use Timers at all, it is just 
        //  provided for convenience if you require.
        // REMOVE THIS DUMMY CODE (2 lines below), otherwise it will mess up your algorithm's output
    }

    /** De-initialises the detection algorithm. */
    public void deinitDetection() 
        throws Exception {
        // Add de-initialisation code here, if any
    }

    /** 
       Called when the accelerometer sensor has changed.

       @param   timestamp    Timestamp of this sensor event
       @param   x            Accl x value (m/sec^2)
       @param   y            Accl y value (m/sec^2)
       @param   z            Accl z value (m/sec^2)
       @param   accuracy     Accuracy of the sensor data (you can ignore this)
     */
    private float displacement = 0;
    private float prevDisplacement = 0;
    private int accCount = 0;
    private float accArray[] = new float[120];
    private float accAnswer = 0;
    private float standardDev = 0;
    public void onAcclSensorChanged( long timestamp , 
                                     float x , 
                                     float y , 
                                     float z , 
                                     int accuracy ) {
        accArray[accCount%120] = (float) Math.sqrt(Math.pow(x,2)+Math.pow(y,2)+Math.pow(z,2));
        accCount++;
        Arrays.sort(accArray);

        if (accCount >= 120) {
            accAnswer = phi(accArray);

            standardDev = standardDeviation(0, accArray.length, accArray);

            if (standardDev < 0.01) {
                isIdle = true;
            } else if (accAnswer > 0.7) {
                isWalking = true;
            }
        }


        displacement = (float) Math.sqrt(Math.pow(x,2)+Math.pow(y,2)+Math.pow(z,2));

        if ((Math.abs(displacement) - Math.abs(prevDisplacement)) > 4.0) {
            isAcclUpdated = true;
        }

        prevDisplacement = displacement;


        // You will most likely not need to use Timers at all, it is just
        //  provided for convenience if you require.

        // Here, we just show a dummy example of creating a timer
        //  to execute a task 10 minutes later.
        // Be careful not to create too many timers!

        /*if ( isFirstAcclReading ) {
            SimulatorTimer timer = new SimulatorTimer();
            timer.schedule( this.task ,        // Task to be executed
                    1 );  // Delay in millisec (10 min)
        }*/
        accCount = 0;
    }

    /** 
       Called when the gravity sensor has changed.

       @param   timestamp    Timestamp of this sensor event
       @param   x            Gravity x value (m/sec^2)
       @param   y            Gravity y value (m/sec^2)
       @param   z            Gravity z value (m/sec^2)
       @param   accuracy     Accuracy of the sensor data (you can ignore this)
     */
    public void onGravitySensorChanged( long timestamp , 
                                        float x , 
                                        float y , 
                                        float z , 
                                        int accuracy ) {
    }

    /** 
       Called when the linear accelerometer sensor has changed.

       @param   timestamp    Timestamp of this sensor event
       @param   x            Linear Accl x value (m/sec^2)
       @param   y            Linear Accl y value (m/sec^2)
       @param   z            Linear Accl z value (m/sec^2)
       @param   accuracy     Accuracy of the sensor data (you can ignore this)
     */
    private float[] linearDispArray = new float[119];
    private int linearCount = 0;
    public void onLinearAcclSensorChanged( long timestamp , 
                                           float x , 
                                           float y , 
                                           float z , 
                                           int accuracy ) {
        if (linearCount < 119) {
            linearDispArray[linearCount] = (float) Math.sqrt(Math.pow(x,2)+Math.pow(y,2)+Math.pow(z,2));
            linearCount++;
        } else {
            Arrays.sort(linearDispArray);
            linearValue = linearDispArray[linearDispArray.length/2];
            linearCount = 0;
            isLinearUpdated = true;
        }
    }

    /** 
       Called when the magnetic sensor has changed.

       @param   timestamp    Timestamp of this sensor event
       @param   x            Magnetic x value (microTesla)
       @param   y            Magnetic y value (microTesla)
       @param   z            Magnetic z value (microTesla)
       @param   accuracy     Accuracy of the sensor data (you can ignore this)
     */
    public void onMagneticSensorChanged( long timestamp , 
                                         float x , 
                                         float y , 
                                         float z , 
                                         int accuracy ) {
    }

    /** 
       Called when the gyroscope sensor has changed.

       @param   timestamp    Timestamp of this sensor event
       @param   x            Gyroscope x value (rad/sec)
       @param   y            Gyroscope y value (rad/sec)
       @param   z            Gyroscope z value (rad/sec)
       @param   accuracy     Accuracy of the sensor data (you can ignore this)
     */
    public void onGyroscopeSensorChanged( long timestamp , 
                                          float x , 
                                          float y , 
                                          float z , 
                                          int accuracy ) {
    }

    /** 
       Called when the rotation vector sensor has changed.

       @param   timestamp    Timestamp of this sensor event
       @param   x            Rotation vector x value (unitless)
       @param   y            Rotation vector y value (unitless)
       @param   z            Rotation vector z value (unitless)
       @param   scalar       Rotation vector scalar value (unitless)
       @param   accuracy     Accuracy of the sensor data (you can ignore this)
     */
    public void onRotationVectorSensorChanged( long timestamp , 
                                               float x , 
                                               float y , 
                                               float z , 
                                               float scalar ,
                                               int accuracy ) {
    }

    /** 
       Called when the barometer sensor has changed.

       @param   timestamp    Timestamp of this sensor event
       @param   pressure     Barometer pressure value (millibar)
       @param   altitude     Barometer altitude value w.r.t. standard sea level reference (meters)
       @param   accuracy     Accuracy of the sensor data (you can ignore this)
     */
    public void onBarometerSensorChanged( long timestamp , 
                                          float pressure , 
                                          float altitude , 
                                          int accuracy ) {
    }

    /** 
       Called when the light sensor has changed.

       @param   timestamp    Timestamp of this sensor event
       @param   light        Light value (lux)
       @param   accuracy     Accuracy of the sensor data (you can ignore this)
     */
    private float[] lightArray = new float[50];
    private int count = 0;
    public void onLightSensorChanged( long timestamp , 
                                      float light , 
                                      int accuracy ) {
        if (count < 50) {
            lightArray[count] = light;
            count++;
        } else {
            Arrays.sort(lightArray);
            lightValue = lightArray[lightArray.length/2];
            count = 0;
            /*SimulatorTimer timer = new SimulatorTimer();
            timer.schedule(task,             // Task to be executed
                    10 * 10);  // Delay in millisec (10 min)*/
            isLightUpdated = true;
        }
    }

    /** 
       Called when the proximity sensor has changed.

       @param   timestamp    Timestamp of this sensor event
       @param   proximity    Proximity value (cm)
       @param   accuracy     Accuracy of the sensor data (you can ignore this)
     */
    public void onProximitySensorChanged( long timestamp , 
                                          float proximity , 
                                          int accuracy ) {
    }

    /** 
       Called when the location sensor has changed.

       @param   timestamp    Timestamp of this location event
       @param   provider     "gps" or "network"
       @param   latitude     Latitude (deg)
       @param   longitude    Longitude (deg)
       @param   accuracy     Accuracy of the location data (you may use this) (meters)
       @param   altitude     Altitude (meters) (may be -1 if unavailable)
       @param   bearing      Bearing (deg) (may be -1 if unavailable)
       @param   speed        Speed (m/sec) (may be -1 if unavailable)
     */
    private float[] locArray = new float[5];
    private int locCount = 0;
    public void onLocationSensorChanged( long timestamp , 
                                         String provider , 
                                         double latitude , 
                                         double longitude , 
                                         float accuracy , 
                                         double altitude , 
                                         float bearing , 
                                         float speed ) {
        if (locCount < 5 && speed > -1) {
            locArray[locCount] = speed;
            locCount++;
        } else {
            Arrays.sort(locArray);
            locValue = locArray[locArray.length/2];
            locCount = 0;
            SimulatorTimer timer = new SimulatorTimer();
            timer.schedule(this.task,             // Task to be executed
                    10 * 100);  // Delay in millisec (10 min)
            isLocUpdated = true;
        }
    }

    /** Helper method to convert UNIX millis time into a human-readable string. */
    private static String convertUnixTimeToReadableString( long millisec ) {
        return sdf.format( new Date( millisec ) );
    }

    /** To format the UNIX millis time as a human-readable string. */
    private static final SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd-h-mm-ssa" );

    private boolean isFirstAcclReading = true;
    private boolean isUserOutside = false;
    private boolean isLightUpdated = false;
    private boolean isAcclUpdated = false;
    private boolean isLocUpdated = false;
    private boolean isLinearUpdated = false;
    private boolean isGyroUpdated = false;
    private float lightValue = 0;
    private float locValue = 0;
    private float linearValue = 0;
    private float gyroValue = 0;
    private int numberTimers = 1;

    private int delayCount = 0;

    private int[] statesArray = new int[5];

    private boolean isIdle = false;

    private boolean isWalking = false;

    private String currentState = "";
    private String prevState = "";

    public static int mean (int start, int end, float[] accelerationVals){
        float sum = 0;
        for(int i=start; i<end; i++){
            sum += accelerationVals[i];
        }
        return (int) sum/(end - start + 1);
    }

    public static float standardDeviation(int start, int end, float[] accelerationVals) {
        float deviation = 0;
        float mean = mean(start, end, accelerationVals);
        float sum = 0;
        for (int i = start; i < end; i++) {
            accelerationVals[i] = (float) Math.pow((accelerationVals[i]-mean),2);
            sum += accelerationVals[i];
        }
        deviation = (float) Math.sqrt((sum/(end-start+1)));
        return deviation;
    }

    public static float autoC(float[] accelerationVals, int tau){
        float numerator = 0;
        float sum = 0;
        for(int i=0; i<tau; i++){
            numerator = ((accelerationVals[i]-mean(0,tau,accelerationVals))*(accelerationVals[(i+tau)]-mean(tau,2*tau,accelerationVals)));
            sum = numerator + sum;
        }
        float denominator = tau * standardDeviation(0,tau,accelerationVals) * standardDeviation(tau,2*tau,accelerationVals);
        float answer = sum/denominator;
        return answer;
    }

    public static float phi(float[] accelerationVals){
        float[] answers = new float[100];
        int taumin = 20;
        int taumax = 60;
        for(int i=taumin; i<=taumax; i++){
            answers[i] = autoC(accelerationVals, i);
        }
        float answermax = 0;
        for(int i=taumin; i<=taumax; i++){
            if(answers[i] >= answermax)
                answermax = answers[i];
        }
        return answermax;
    }

    private Runnable task = new Runnable() {
            public void run() {

                // Logging to the DDMS (in the simulator, the DDMS log is to the console)
                //System.out.println();
                Log.i( "ActivitySim" , "Timer " + numberTimers + ": Current simulator time: " +
                       convertUnixTimeToReadableString( ActivitySimulator.currentTimeMillis() ) );
                /*System.out.println( "Timer " + numberTimers + ": Current simulator time: " +
                                    convertUnixTimeToReadableString( ActivitySimulator.currentTimeMillis() ) );*/

                /*if (isIdle) {
                    ActivitySimulator.outputDetectedActivity( UserActivities.IDLE_INDOOR );
                    isIdle = false;
                } else if (isWalking) {
                    ActivitySimulator.outputDetectedActivity( UserActivities.WALKING );
                    isWalking = false;
                }*/

                /*if ( isFirstAcclReading ) {
                    ActivitySimulator.outputDetectedActivity( UserActivities.WALKING );
                    isFirstAcclReading = false;
                }*/



                if ( isAcclUpdated && isLocUpdated && locValue >= 2.5 ) {
                    ActivitySimulator.outputDetectedActivity( UserActivities.BUS );
                    isLocUpdated = false;
                    isAcclUpdated = false;
                    isLinearUpdated = false;
                    isGyroUpdated = false;
                    currentState = "BUS";
                } else if ( isLinearUpdated && linearValue <= 1.5 && lightValue < 150.0 && locValue < 1.5 ) {
                    ActivitySimulator.outputDetectedActivity( UserActivities.IDLE_INDOOR );
                    isLightUpdated = false;
                    isLinearUpdated = false;
                    isGyroUpdated = false;
                    currentState = "IDLE_INDOOR";
                } else if ( isAcclUpdated && isLinearUpdated && linearValue >= 1.85 && locValue < 1.5 && lightValue > 0.0 ) {
                    ActivitySimulator.outputDetectedActivity( UserActivities.WALKING );
                    System.out.println( linearValue );
                    isLocUpdated = false;
                    isAcclUpdated = false;
                    isLinearUpdated = false;
                    isGyroUpdated = false;
                    currentState = "WALKING";
                } else if ( isLinearUpdated && linearValue <= 0.5 && lightValue >= 150.0 && locValue < 1 && (currentState != "IDLE_INDOOR") ) {
                    ActivitySimulator.outputDetectedActivity( UserActivities.IDLE_OUTDOOR );
                    isLightUpdated = false;
                    isLinearUpdated = false;
                    isGyroUpdated = false;
                    currentState = "IDLE_OUTDOOR";
                }

                if (delayCount == 0) {
                    statesArray = new int[]{0,0,0,0,0};
                }

                /*if (delayCount < 2) {
                    if (currentState == "BUS") {
                        statesArray[0]++;
                    } else if (currentState == "WALKING") {
                        statesArray[1]++;
                    } else if (currentState == "IDLE_INDOOR") {
                        statesArray[2]++;
                    } else if (currentState == "IDLE_OUTDOOR") {
                        statesArray[3]++;
                    } else {
                        statesArray[4]++;
                    }
                    delayCount++;
                } else {
                    delayCount = 0;
                    int maxCount = 0;
                    int maxState = 0;
                    maxCount = statesArray[0];
                    for (int i=1; i<statesArray.length; i++) {
                        if (statesArray[i] > maxCount) {
                            maxCount = statesArray[i];
                            maxState = i;
                        }
                    }
                    if (maxState == 0) {
                        ActivitySimulator.outputDetectedActivity( UserActivities.BUS );
                    } else if (maxState == 1) {
                        ActivitySimulator.outputDetectedActivity( UserActivities.WALKING );
                    } else if (maxState == 2) {
                        ActivitySimulator.outputDetectedActivity( UserActivities.IDLE_INDOOR );
                    } else if (maxState == 3) {
                        ActivitySimulator.outputDetectedActivity( UserActivities.IDLE_OUTDOOR );
                    } else {
                        ActivitySimulator.outputDetectedActivity( UserActivities.JOGGING );
                    }

                    statesArray = new int[]{0,0,0,0,0};
                }*/
                // Set the next timer to execute the same task 10 min later
                ++numberTimers;
                SimulatorTimer timer = new SimulatorTimer();
                timer.schedule(task,             // Task to be executed
                                10 * 60 * 1000 );  // Delay in millisec (10 min)
            }
        };
}
