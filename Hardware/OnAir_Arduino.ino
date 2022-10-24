#include <IRremoteESP8266.h>
#include <Arduino.h>
#include <IRsend.h>
#include <WiFi.h>
#include <IOXhop_FirebaseESP32.h>
#include <ir_Sharp.h>


#define FIREBASE_Host "https://remoteac-e5c64-default-rtdb.asia-southeast1.firebasedatabase.app/"                   // replace with your Firebase Host
#define FIREBASE_authorization_key "secret key" // replace with your secret key



const char* ssid = "ssid";
const char* password = "pass";

const uint16_t kIrLed = 4;  // ESP8266 GPIO pin to use. Recommended: 4 (D2).

IRSharpAc ac(kIrLed);  // Set the GPIO to be used to sending the message.


bool varbool = true;
int varfan = 0;
int varswing = 0;
int varmode = 0;

WiFiServer wifiServer(80);

void setup() {
  ac.begin();
  Serial.begin(115200);

  delay(1000);

  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.println("Connecting..");
  }

  Serial.print("Connected to WiFi. IP:");
  Serial.println(WiFi.localIP());

  wifiServer.begin();

  Firebase.begin(FIREBASE_Host, FIREBASE_authorization_key);
  Firebase.setInt("command", 0);
}

void acpower() {
  varbool = !varbool;
  ac.setPower(varbool);

}

void acfan() {
  if (varfan == 0) {
    ac.setFan(kSharpAcFanAuto);
    varfan++;
  }
  if (varfan == 1) {
    ac.setFan(kSharpAcFanMin);
    varfan++;
  }
  if (varfan == 2) {
    ac.setFan(kSharpAcFanMed);
    varfan++;
  }
  if (varfan == 3) {
    ac.setFan(kSharpAcFanHigh);
    varfan++;
  }
  if (varfan == 4) {
    ac.setFan(kSharpAcFanMax);
    varfan++;
  }
  else {
    varfan = 0;
  }
}

void acmode() {
  if (varmode == 0) {
    ac.setMode(kSharpAcDry);
    varmode++;
  }
  if (varmode == 1) {
    ac.setMode(kSharpAcCool);
    varmode++;
  }
  if (varmode == 2) {
    ac.setMode(kSharpAcHeat);
    varmode++;
  }
  else {
    varmode = 0;
  }
}

void acswing() {
  if (varswing == 0) {
    ac.setSwingV(kSharpAcSwingVLow);
    varswing++;
  }
  if (varswing == 1) {
    ac.setSwingV(kSharpAcSwingVOff);
    varswing++;
  }
  else {
    varswing = 0;
  }
}

void loop() {
  int command = Firebase.getInt("command");
  int temp = Firebase.getInt("temp");

  //Power ON
  if (command == 1) {
    acpower();  // Send a raw data capture at 38kHz.
    Serial.println("Turning ON/OFF");
    delay(1000);
    Firebase.setInt("command", 0);
  }

  //TempSet
  else if (command == 2) {
    ac.setTemp(temp);  // Send a raw data capture at 38kHz.
    Serial.println("Temperature Set");
    delay(1000);
    Firebase.setInt("command", 0);
  }

  //Fan
  else if (command == 3) {
    acfan();  // Send a raw data capture at 38kHz.
    Serial.println("Adjusting FAN");
    delay(1000);
    Firebase.setInt("command", 0);
  }

  //Mode
  else if (command == 4) {
    acmode();  // Send a raw data capture at 38kHz.
    Serial.println("Changing MODE");
    delay(1000);
    Firebase.setInt("command", 0);
  }

  //Swing
  else if (command == 5) {
    acswing();  // Send a raw data capture at 38kHz.
    Serial.println("Adjusting SWING");
    delay(1000);
    Firebase.setInt("command", 0);
  }

}
