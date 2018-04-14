package com.example.kevstar.blukane;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

public class CurrentLocation extends AppCompatActivity {

    BlueKane BlueKane;
    private GestureDetectorCompat gestureObject;
    Button currentLocationButton; //button object
    boolean isWifiEnabled; //boolean value that stores if wifi is enabled or not
    boolean  valueofMetersOn = true; //boolean value that is used to store the status of MetersOn

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_location);
        setTitle("BluKane (Current Location)");
        BlueKane = new BlueKane();


        currentLocationButton =(Button) findViewById(R.id.currentlocation);

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            valueofMetersOn = extras.getBoolean("MetersOn"); //gets boolean value from Main Activity to determine
                                                                  //if meters mode is on or off
        }

        currentLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent map = new Intent(CurrentLocation.this,Map.class);
                map.putExtra("MetersOn", valueofMetersOn); //Passes variable valueofMetersOn to map activity
                startActivity(map); //Launch map activity
            }

        });

        WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        isWifiEnabled = wifiManager.isWifiEnabled(); //checks to see if wifi is on or off and stores in a boolean variable

        if (isWifiEnabled == false) //checks to see if wifi is off
            wifiManager.setWifiEnabled(true); //turns on wifi (this is done, so that users can be connected
                                              // to the internet in order to use the map on the app)


        gestureObject = new GestureDetectorCompat(this, new LearnGesture());
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
                BlueKane.playMainActivityTone(); //Plays a tone before Main activity is opened
                Intent main = new Intent(CurrentLocation.this, MainActivity.class);
                finish(); //Finishes Main Activity
                startActivity(main); //Starts main activity
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right); //transition
            } else if (event2.getX() < event1.getX())
            {
                BlueKane.playVibrationActivityTone(); //Plays a tone before Vibration activity is opened
                Intent Vibration = new Intent(CurrentLocation.this, Vibration.class);
                finish();//Finishes Main Activity
                startActivity(Vibration); //Starts vibration activity
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left); //transition
            }
            return true;
        }

    }
/************************************************************************************************************************/
}
