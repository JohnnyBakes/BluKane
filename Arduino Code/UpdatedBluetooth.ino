const int pingPin = 5;

//#include <NewSoftSerial.h>   //Software Serial Port
#include <SoftwareSerial.h>               //new line
#define RxD 0
#define TxD 1
#define inputPin A1

#define DEBUG_ENABLED  1

//NewSoftSerial blueToothSerial(RxD,TxD);
SoftwareSerial blueToothSerial(RxD, TxD); //new line


void setup()
{
  // initialize serial communication:
  Serial.begin(9600);
  pinMode(pingPin, OUTPUT);
  pinMode(RxD, INPUT);
  pinMode(TxD, OUTPUT);
  setupBlueToothConnection();

}

void loop()
{
  char recvChar;
  //while(1){
  if (blueToothSerial.available()) { //check if there's any data sent from the remote bluetooth shield
    recvChar = blueToothSerial.read();
    Serial.print(recvChar);
  }
  if (Serial.available()) { //check if there's any data sent from the local serial terminal, you can add the other applications here
    recvChar  = Serial.read();
    blueToothSerial.print(recvChar);
  }
  //}

  // establish variables for duration of the ping,
  // and the distance result in inches, centimeters and meters:
  long inches, cm;
  float meters, duration;

  // The PING))) is triggered by a HIGH pulse of 2 or more microseconds.
  // Give a short LOW pulse beforehand to ensure a clean HIGH pulse:
  pinMode(pingPin, OUTPUT);
  digitalWrite(pingPin, LOW);
  delayMicroseconds(2);
  digitalWrite(pingPin, HIGH);
  delayMicroseconds(5);
  digitalWrite(pingPin, LOW);

  // The same pin is used to read the signal from the PING))): a HIGH
  // pulse whose duration is the time (in microseconds) from the sending
  // of the ping to the reception of its echo off of an object.
  pinMode(pingPin, INPUT);
  duration = pulseIn(pingPin, HIGH);

  // convert the time into a distance
  inches = microsecondsToInches(duration);
  cm = microsecondsToCentimeters(duration);
  meters = microsecondsToMeters(duration);

  Serial.print("B");  //comment out to stop output from going to serial port
  blueToothSerial.print("B");
  //If blueToothSerial.print() is executed when there isn't a Bluetooth device connected, the Bluetooth shield switches from Inquiring to Disconnected.
  Serial.print(meters,1);  //comment out to stop output from going to serial port (the 1 is for the amount of decimal places in the float)
  blueToothSerial.print(meters,1); 
  Serial.print("K");  //comment out to stop output from going to serial port
  blueToothSerial.print("K");
  //blueToothSerial.print(String("B") + String(meters, 1) + String("K"));
  //Serial.print(String("B") + String(meters, 1) + String("K"));
  Serial.flush();  //comment out to stop output from going to serial port

  delay(1000);
}

long microsecondsToInches(long microseconds) {
  // According to Parallax's datasheet for the PING))), there are
  // 73.746 microseconds per inch (i.e. sound travels at 1130 feet per
  // second).  This gives the distance travelled by the ping, outbound
  // and return, so we divide by 2 to get the distance of the obstacle.
  // See: http://www.parallax.com/dl/docs/prod/acc/28015-PING-v1.3.pdf
  return microseconds / 74 / 2;
}

long microsecondsToCentimeters(long microseconds) {
  // The speed of sound is 340 m/s or 29 microseconds per centimeter.
  // The ping travels out and back, so to find the distance of the
  // object we take half of the distance travelled.
  return microseconds / 29 / 2;
}
float microsecondsToMeters (float microseconds) {
  // Since the speed of sound is 340 m/s or 29 microseconds per centimeter,
  // and 100 centimeter = 1 meter, then to calculate microseconds to meters
  // will be (microseconds / 29 / 2)/100
  return (microseconds /29 / 2) / 100;
}


void setupBlueToothConnection()
{
  blueToothSerial.begin(38400); //Set BluetoothBee BaudRate to default baud rate 38400
  blueToothSerial.print("\r\n+STWMOD=0\r\n"); //set the bluetooth work in slave mode
  blueToothSerial.print("\r\n+STNA=SeeedBTSlave\r\n"); //set the bluetooth name as "SeeedBTSlave"
  blueToothSerial.print("\r\n+STOAUT=1\r\n"); // Permit Paired device to connect me
  blueToothSerial.print("\r\n+STAUTO=0\r\n"); // Auto-connection should be forbidden here
  delay(2000); // This delay is required.
  blueToothSerial.print("\r\n+INQ=1\r\n"); //make the slave bluetooth inquirable
  //This line is sent then the arduino continues executing the rest of the code.
  //Seemingly, the Bluetooth module itself executes this command separate from the Arduino
  Serial.println("The slave bluetooth is inquirable!");
  delay(2000); // This delay is required.

  while (analogRead(inputPin) == 0) {
    Serial.println("Not yet connected"); //0/LOW is returned from the pin when there is no Bluetooth connection
  }

  //blueToothSerial.flush();
  Serial.println("Connected");
  delay(5000);//This five second delay is to ensure that the device is fully connected.
}
