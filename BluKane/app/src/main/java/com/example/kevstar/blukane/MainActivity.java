package com.example.kevstar.blukane;

import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.content.Intent;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity {

    Button startButton, stopButton; //Button objects
    private TextView textView; //TextView object
    private GestureDetectorCompat gestureObject;
    Thread thread;
    byte buffer[]; //Buffer used to hold data read
    String output; //String variable used to catch the output sent from the JY-MCU (HC-06) bluetooth serial module
    private TextToSpeech toSpeech; //TextToSpeech object named toSpeech (Used for text to speech)
    int textResult;
    private BlueKane BlueKane;
    Switch Toggle; //switch object
    double distance; //Double variable used when convering meters into feet
    boolean parsedOutput; //boolean value used to determine if the output has been parsed or not
    String tempOutput =""; //variable used to temporarily hold output, will be used to compare the new output with the old output



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startButton = findViewById(R.id.buttonStart);
        stopButton = findViewById(R.id.buttonStop);
        stopButton.setEnabled(false); //make stop button unclickable
        Toggle = findViewById(R.id.Toggle);
        textView = findViewById(R.id.textView);
        setTitle("BluKane (Text-To-Speech mode)");
        BlueKane = new BlueKane();
        BlueKane.BTinit();

            //Function used to listen when toggle is switched
        Toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                {
                    Toast.makeText(getApplicationContext(), "Distance: Feet", Toast.LENGTH_SHORT).show();
                    BlueKane.setMeterPresent(false);//Changes variable MetersOn to false
                    if (toSpeech != null)
                    {
                        toSpeech.stop();
                        toSpeech.speak("Feet Mode",TextToSpeech. QUEUE_ADD,null); //Speaks "Feet mode"
                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Distance: Meters", Toast.LENGTH_SHORT).show();
                    BlueKane.setMeterPresent(true); //Changes variable MetersOn to true
                    if (toSpeech != null)
                    {
                        toSpeech.stop();
                        toSpeech.speak("Meters Mode",TextToSpeech. QUEUE_ADD,null); //Speaks "Meters mode"
                    }
                }
            }
        });


        Thread test  = new Thread(new Runnable()
        {
            public void run() {   //create new ThreadTT
                    TTSinit();
            }

            });
        test.start();
        gestureObject = new GestureDetectorCompat(this, new LearnGesture());
    }//end onCreate()

    /************************************************************************************************************/

    /************************************************************************************************************************/

    //Function used to initialize Text to speech
    public void TTSinit()
    {

      //  TTSthread  = new Thread(new Runnable()//create new Thread
        //{
         //   public void run()
          //  {
            toSpeech = new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status)
            {

                if (status == TextToSpeech.SUCCESS)
                {
                    textResult = toSpeech.setLanguage(Locale.UK); //Text-to-speech language set to UK
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Text to speech not supported on your device", Toast.LENGTH_SHORT).show();
                }

            }

        });
   // }
//});
      //  TTSthread.start();
}




@Override
public boolean onTouchEvent(MotionEvent event)
{
    this.gestureObject.onTouchEvent(event);
    return super.onTouchEvent(event);
}


/***************************************************************************************************/
    class LearnGesture extends GestureDetector.SimpleOnGestureListener
    {

            @Override
            public boolean onFling (MotionEvent event1, MotionEvent event2,float velocityX,
            float velocityY)
            {

                if (event2.getX() > event1.getX())
                {
                    BlueKane.playVibrationActivityTone(); //Plays a tone before Vibration activity is opened
                    Intent Vibration = new Intent(MainActivity.this, Vibration.class);
                    Vibration.putExtra("MetersOn", BlueKane.getMeterPresent()); //Passes variable valueofMetersOn to vibration activity
                    finish(); //Finishes Main Activity
                    startActivity(Vibration); //Starts vibration activity
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right); //transition
                } else if (event2.getX() < event1.getX())
                {
                    BlueKane.playCurrentLocationActivityTone(); //Plays a tone before Current Location activity is opened
                    Intent location = new Intent(MainActivity.this, CurrentLocation.class);
                    location.putExtra("MetersOn", BlueKane.getMeterPresent()); //Passes variable valueofMetersOn to current location activity
                    finish();//Finishes Main Activity
                    startActivity(location); //Starts vibration activity
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left); //transition
                }
                return true;
                    }

    }
/************************************************************************************************************************/

    /***************************************************************************************************************************/

    public void onClickStart(View view)
    {
        if(BlueKane.BTinit()) //checks to see if bluetooth was established
        {
            if(BlueKane.BTconnect()) //checks if connection was established between HC-06 and mobile device
            {
                beginListenForData(); //Begins to listen for data if connection was successful
                Toast.makeText(getApplicationContext(), "Connection Opened", Toast.LENGTH_SHORT).show();
                startButton.setEnabled(false);// make start button unclickable
                stopButton.setEnabled(true); // make the stop button clickable
            } //end if
            else //if the smart cane is off
            {
                Toast.makeText(getApplicationContext(), "Please turn on the Smart Cane first", Toast.LENGTH_SHORT).show();
            }
        } //end if

    } //end onClickStart()



/************************************************************************************************************************/
//This function is used to stop the Text-to-speech when the Main page is destroyed
    @Override
    public void onDestroy()
    {
        if (toSpeech != null)
        {
        toSpeech.stop(); //stops text-to-speech
        toSpeech.shutdown(); //shuts down text-to-speech
        BlueKane.setThread(true);//stop the thread running the text-to-speech
        }
        try
        {
            if (BlueKane.getInputStream() != null )
            BlueKane.getInputStream().close(); // close inputSteam

           /* if (BlueKane.getSocket() != null)
            BlueKane.getSocket().close(); // close socket connection*/
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        super.onDestroy();

    }

/***************************************************************************************************************************/

    void beginListenForData()
    {
        parsedOutput = false;

        final Handler handler = new Handler(); //Allows you to send and process Message and Runnable objects associated with a thread's MessageQueue.
        BlueKane.setThread(false); //boolean used to determine if thread was stopped (Although Thread.isInterrupted can be used alone),
                           //It is used in the onClickStop() as well
        buffer = new byte[256];

          thread  = new Thread(new Runnable()
        {
            public void run()
            {   //create new Thread
                while(!thread.currentThread().isInterrupted() && !BlueKane.getThread() && textResult==1) //Run unless thread is interrupted/stopped
                {
                    try
                    {
                        int byteCount = BlueKane.getInputStream().available(); //if there are bytes available in inputSteam
                        if(byteCount > 0) //if there are bytes available to read proceed
                        {
                            byte[] rawBytes = new byte[byteCount];
                            BlueKane.getInputStream().read(rawBytes);

                            output=new String(rawBytes,"UTF-8");

                            if (output.charAt(0) != 'B' || output.charAt(output.length()-1) != 'K')
                            {
                                tempOutput = tempOutput + output;
                            }
                            if (tempOutput.charAt(0) == 'B' && tempOutput.charAt(tempOutput.length()-1) == 'K')
                            {
                                output = tempOutput.replaceAll("[^\\.0123456789]", "");
                                /*extracts the float number from the string since the bluetooth module sends distances in the format
                                * B(distance)K
                                *(A letter was placed in the beginning and end of a each distance to be able to distinguish each distance
                                *  and for synchronization purposes
                                * [^\\.0123456789] replaces anything that is not a digit or a period with ""
                                *Example B2.4K will be ""2.4""
                                */
                                parsedOutput = true; //parsedOutput turns to true when the final output has been obtained
                                if (!BlueKane.getMeterPresent() && output !="") //if Toggle is on feet, will convert distance in meters to feet
                                {
                                    distance = Double.parseDouble(output); //changes string to double
                                    distance = distance / 0.3048; //calculation to change meters to feet
                                    output = String.format("%.1f",distance); //ensures the distance in feet is one decimal place (rounds up)
                                }

                            }




                            handler.post(new Runnable() {
                                public void run()
                                {  //handler posts to the main UI

                                    //checks if the Language for the text to speech is present and supported
                                    if (textResult == TextToSpeech.LANG_MISSING_DATA || textResult == TextToSpeech.LANG_NOT_SUPPORTED)
                                    {
                                        Toast.makeText(getApplicationContext(), "Text to speech language is missing or not supported on this device", Toast.LENGTH_SHORT).show();
                                    }//end if
                                    else
                                    {
                                        if (parsedOutput)
                                        {
                                                textView.append(output + " "); //Outputs the distance received to screen
                                                toSpeech.speak(output, TextToSpeech.QUEUE_ADD, null);//Here output is turned to speech
                                                tempOutput = ""; // reset the temporary output to "" so that the new output can be parsed over
                                                parsedOutput = false;
                                        }

                                    } //end else
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
        thread.interrupt(); //interrupt thread
        toSpeech.stop(); //stop the Text-to-Speech
        startButton.setEnabled(true);// make start button clickable
        stopButton.setEnabled(false); // make the stop button unclickable
        Toast.makeText(getApplicationContext(), "Connection Closed", Toast.LENGTH_SHORT).show();
    } //end
}//end of MainActivity
