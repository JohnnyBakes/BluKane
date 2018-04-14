package com.example.kevstar.blukane;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.util.LinkedList;

public class Vibration extends AppCompatActivity {

    private GestureDetectorCompat gestureObject;
    TextView textView; //textview object
    Vibrator vibrator; //vibrator object
    Thread thread; //thread
    boolean stopThread; //boolean variable
    BlueKane BlueKane; //Bluekane object
    String output=""; //String variable used to catch the output sent from the JY-MCU (HC-06) bluetooth serial module
    byte buffer[]; //buffer to hold data being read
    Button startButton, stopButton; //button object
    LinkedList<Float> VibOutput;
    boolean  valueofMetersOn = true; //used to store boolean value for the status of MetersOn

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vibration);
        setTitle("BluKane (Vibration mode)");
        startButton = findViewById(R.id.buttonStart);
        stopButton =  findViewById(R.id.buttonStop);
        stopButton.setEnabled(false); //make stop button unclickable
        textView = findViewById(R.id.textView);
        VibOutput = new LinkedList<>(); //used to hold distances
        BlueKane = new BlueKane();


        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            valueofMetersOn = extras.getBoolean("MetersOn"); //gets value from Main Activity to determine
                                                                 //if meters mode is on or off
        }


        gestureObject = new GestureDetectorCompat(this, new LearnGesture());

    }//end onCreate()
/*************************************************************************************************************************/

@Override
    public boolean onTouchEvent(MotionEvent event)
    {
        this.gestureObject.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

 /**************************************************************************************************************************/
    class LearnGesture extends GestureDetector.SimpleOnGestureListener
    {
        @Override
        public boolean onFling (MotionEvent event1, MotionEvent event2, float velocityX, float velocityY)
        {
            if (event2.getX() > event1.getX())
            {
                BlueKane.playCurrentLocationActivityTone(); //Plays a tone before Current Location activity is opened
                Intent location = new Intent(Vibration.this,CurrentLocation.class);
                location.putExtra("MetersOn",valueofMetersOn ); //Passes variable valueofMetersOn to current location activity
                finish(); //finish vibration activity
                startActivity(location); //start current location activity
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right); //transition
            }
            else
            if (event2.getX() <event1.getX())
            {
                BlueKane.playMainActivityTone(); //Plays a tone before Main activity is opened
                Intent main = new Intent(Vibration.this,MainActivity.class);
                finish(); //finish vibration activity
                startActivity(main); //start main activity
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left); //transition
            }
            return true;

        }
    }// end LearnGesture
 /***********************************************************************************************************************/

 public void onClickStart(View view)
 {

     if(BlueKane.BTinit() && VibInit()) //checks to see if bluetooth was established and vibration is present
     {
         if(BlueKane.BTconnect()) //checks if connection was established between HC-06 and mobile device
         {
             beginListenForData(); //Begins to listen for data if connection was successful
             Toast.makeText(getApplicationContext(), "Connection Opened", Toast.LENGTH_SHORT).show();
             startButton.setEnabled(false);// make start button unclickable
             stopButton.setEnabled(true); // make the stop button clickable
         }//end if
         else // if the smart cane is off
         {
             Toast.makeText(getApplicationContext(), "Please turn on the Smart Cane first", Toast.LENGTH_SHORT).show();
         }
     }//end if

 }//end onClickStart()

  /****************************************************************************************************************************/

    public boolean VibInit() //Determines if device has vibration and will be used to vibrate when a button is pressed
    {
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator.hasVibrator()) //checks to see if the device supports vibration
        {
            long[] arr = {0,100,100}; //the vibration pattern for the notification vibration
            vibrator.vibrate(arr, -1);//a test to notify the user that the "Begin" button has been selected
            return true; //returns true if device supports vibration
        } //end if
        else
        {
            return false; //returns false if device does not support vibration
        } //end else
    } // end VibInit()
/****************************************************************************************************************************/
    public void buttonVibrate()
    {

    }
/***************************************************************************************************************************/
    void beginListenForData()
    {
        final Handler handler = new Handler(); //Allows you to send and process Message and Runnable objects associated with a thread's MessageQueue.
        BlueKane.setThread(false);//boolean used to determine if thread was stopped (Although Thread.isInterrupted can be used alone),
        //It is used in the onClickStop() as well
        buffer = new byte[256];

        thread  = new Thread(new Runnable()
        {
            public void run()
            {   //create new Thread
                while(!thread.currentThread().isInterrupted() && !BlueKane.getThread()) //Run unless thread is interrupted/stopped
                {
                    try
                    {
                        int byteCount = BlueKane.getInputStream().available(); //if there are bytes available in inputSteam
                        if(byteCount > 0) //if there are bytes available to read proceeed
                        {
                            byte[] rawBytes = new byte[byteCount];
                            BlueKane.getInputStream().read(rawBytes);
                            output=new String(rawBytes,"UTF-8");
                            output = output.replaceAll("[^\\d.]", ""); //extracts the float from the string
                                                        //[^\\d.] replaces anything that is not a digit or a period with ""

                            if (!output.isEmpty())
                            VibOutput.add(Float.parseFloat(output));


                            handler.post(new Runnable() {
                                public void run()
                                {  //handler posts to the main UI
                                    textView.append(output + " "); //Outputs the distance received to screen

                                    long[] vibClose = {0, 300, 1000}; // vibration pattern for close distance (0 to 1 meters)
                                    long[] vibMid = {0, 100, 50}; // vibration pattern for mid-range distance (1 to 3 meters)
                                    long[] vibFar = {0, 20, 50}; // vibration pattern for far distance (3 to 4 meters,
                                                                // 4 meters is the furthest the Ultrasonic Distance Sensor can pick up)

                                    try {
                                        if (VibOutput.getLast() != null)
                                        {
                                            if (VibOutput.getLast() >= 0 && VibOutput.getLast() <= 1)
                                            {
                                                vibrator.vibrate(vibClose, -1);
                                            }
                                            else if (VibOutput.getLast() > 1 && VibOutput.getLast() <= 3)
                                            {
                                                vibrator.vibrate(vibMid, -1);
                                            }
                                            else
                                            {
                                                vibrator.vibrate(vibFar, -1);
                                            }
                                        }
                                    }
                                    catch(Exception e)
                                    {
                                        e.printStackTrace();
                                    }

                                }
                            });

                        }//end if
                    }//end try
                    catch (IOException ex)
                    {
                        ex.printStackTrace();
                        BlueKane.setThread(true);
                    }// end catch
                    try
                    {
                        Thread.sleep(500); //sleep thread for 0.5 seconds
                    } //end try
                    catch(InterruptedException e)
                    {
                        e.printStackTrace();
                    }//end catch

                }//end while
            }
        });

        thread.start();
    }
    /*************************************************************************************************************************/
    public void onClickStop(View view) throws IOException
    {
        textView.setText("");
        BlueKane.getInputStream().close(); // close inputSteam
        BlueKane.getSocket().close(); // close socket connection
        BlueKane.setThread(true);
        vibrator.cancel(); // cancel vibration
        thread.interrupt(); //interrupt beginListenForData thread
        startButton.setEnabled(true);// make start button clickable
        stopButton.setEnabled(false); // make the stop button unclickable
        Toast.makeText(getApplicationContext(), "Connection Closed", Toast.LENGTH_SHORT).show();
    }

    /************************************************************************************************************************/
//This function is used to stop the Vibration when the Main page is destroyed
    @Override
    public void onDestroy()
    {

        BlueKane.setThread(true);//stop the vibration thread
        try
        {
            if (BlueKane.getInputStream() != null )
                BlueKane.getInputStream().close(); // close inputSteam

            /*if (BlueKane.getSocket() != null)
                BlueKane.getSocket().close(); // close socket connection*/
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        super.onDestroy();

    }

/***************************************************************************************************************************/

/***************************************************************************************************************************/

    /**************************************************************************************************************************/
}//end Vibration class
