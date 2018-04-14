package com.example.kevstar.blukane;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.media.AudioManager;
import android.widget.Toast;
import android.media.ToneGenerator;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 * Created by kevstar on 2/7/2018.
 */

public class BlueKane extends Activity {
    private final String DEVICE_ADDRESS = "20:13:05:15:34:38"; // Mac address for the JY-MCU (HC-06) bluetooth serial module
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");//Serial Port Service ID
    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothDevice device; //created Bluetooth device object named device
    private BluetoothSocket socket; //created Bluetooth socket object named socket
    private InputStream inputStream; //created Inputstream object named inputSteam
    private OutputStream outputStream; //created Outputstream object (Not used since we are only reading data, however, can be used in future development)
    boolean stopThread;
    boolean MetersOn = true;
    ToneGenerator Tone = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);


    /************************************************************************************************************************/

    public boolean BTinit()
    {

        boolean found=false; //boolean variable used to determine if the HC-06 bluetooth module is found
        BluetoothAdapter bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) //checks to see if device supports bluetooth
        {
            Toast.makeText(getApplicationContext(),"Device doesnt Support Bluetooth",Toast.LENGTH_SHORT).show();
            return found;
        } //end if
        if(!bluetoothAdapter.isEnabled()) //Checks to see if the bluetooth for the device is off
        {
            bluetoothAdapter.enable(); //Enable bluetooth on the device

            try
            {
                Thread.sleep(1000);
            } //end try
            catch (InterruptedException e)
            {
                e.printStackTrace();
            } //end catch
        } //end if
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        if(bondedDevices.isEmpty())
        {
            Toast.makeText(getApplicationContext(),"Please Pair the Device first",Toast.LENGTH_SHORT).show();
        } //end if
        else
        {
            for (BluetoothDevice iterator : bondedDevices)
            {
                if(iterator.getAddress().equals(DEVICE_ADDRESS))
                {
                    device=iterator;
                    found=true;
                    break; //breaks if the MAC address is found for the HC-06 (Bluetooth module)
                }//end if
            }//end for
        }//end else
        return found; // return boolean variable found
    } //end BTinit()

    /******************************************************************************************************************************/

            public boolean BTconnect()
            {
                boolean connected=true; //boolean variable used to determine if devices have connected
                try
                {
                    socket = device.createRfcommSocketToServiceRecord(PORT_UUID); //create a socket for devices to communicate
                    socket.connect(); //connect devices via socket
                }
                catch (IOException e) //catches IO Exceptions
                {
                    e.printStackTrace();
                    connected=false;
                }
                if(connected) //If devices are connected
                {
                    try
                    {
                        outputStream=socket.getOutputStream(); //send output from mobile app (Not used for this project)
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    try
                    {
                        inputStream=socket.getInputStream(); //get data from HC-06
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }

                }//end if
                return connected; //return boolean variable
            }// end BTconnect()


    /**************************************************************************************************************************/
        public InputStream getInputStream() //Accessor function for inputStream
        {
            return inputStream;
        }

    /**************************************************************************************************************************/
        public BluetoothSocket getSocket() //Accessor function for socket
        {
            return socket;
        }

    /**************************************************************************************************************************/

        public void setThread(boolean x) //Mutator function for boolean variable stopThread
        {
            stopThread = x;
        }
    /*************************************************************************************************************************/
        public boolean getThread() //Accessor function for stopThread variable
        {
            return stopThread;
        }
    /*************************************************************************************************************************/
        public void setMeterPresent (boolean x) //function used to change Meters to true or false
        {                                   //this function will be used to determine if measurement will be in meters or feet
            MetersOn = x;
        }
    /*************************************************************************************************************************/
        public boolean getMeterPresent () //function used to get value of MetersOn (true or false)
        {                           //if false, measurement will be in feet, if true measurement will be in meters
           return MetersOn;
        }
    /*************************************************************************************************************************/
        public String getDeviceAddress () //Accessor function used to return DEVICE_ADDRESS variable
        {
            return DEVICE_ADDRESS;
        }
    /************************************************************************************************************************/
        public void setBluetoothDevice(BluetoothDevice x)
        {
            device = x;
        }
    /*************************************************************************************************************************/
        public void playMainActivityTone() // Play a Beep before the Main Activty page launched
        {
            Tone.startTone(ToneGenerator.TONE_PROP_BEEP,35); //Plays one beep sound for 35ms for Main Activity
        }
    /************************************************************************************************************************/
        public void playVibrationActivityTone() // Play a Beep before the Vibration Activty page is launched
        {
            for (int i = 0; i<=1; i++) //Play two beeps for the current location activity
            {
                Tone.startTone(ToneGenerator.TONE_PROP_BEEP, 35); //Makes beep sound for 35ms
                synchronized (Tone)
                {
                    try
                    {
                        Tone.wait(200);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    /************************************************************************************************************************/
        public void playCurrentLocationActivityTone()  // Play three beeps before the Current Location Activty is launched
        {
            for (int i = 0; i<=2; i++) //Play three beeps for the current location activity
            {
                Tone.startTone(ToneGenerator.TONE_PROP_BEEP, 35); //Makes beep sound for 35ms
                synchronized (Tone)
                {
                    try
                    {
                        Tone.wait(200);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }

        }
    /************************************************************************************************************************/


    /*
        This function returns the bearing between two locations in degrees

    * latitude1 is used as the current latitude, longitude1 used as the current longitude
    * latitude2 is used as the marker's latitude, longitude2 used as the marker's longitude
    *
    * This function can be used in future versions of this project, to determine the bearings of a destination
    */
    public double bearingBetweenLocations(double latitude1, double longitude1, double latitude2, double longitude2) {

        double PI = 3.14159;
        double lat1 = latitude1 * PI / 180;
        double long1 = longitude1 * PI / 180;
        double lat2 = latitude2 * PI / 180;
        double long2 = longitude2 * PI / 180;

        double dLon = (long2 - long1);

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
                * Math.cos(lat2) * Math.cos(dLon);

        double brng = Math.atan2(y, x);

        brng = Math.toDegrees(brng);
        brng = (brng + 360) % 360;

        return brng;
    }

    /***********************************************************************************************************************/
} //end of BlueKane class
