#include <Arduino.h>
#include <ESP8266WiFi.h>
#include <FirebaseESP8266.h>
#include <ArduinoJson.h>
#include <String.h>
#include <DNSServer.h>
#include <WiFiClient.h>
#include <EEPROM.h>
#include <ESP8266WebServer.h>
#define RL1 5
#define RL2 4
#define RL3 0

#define Touch1 14
#define Touch2 12
#define Touch3 13


#define FIREBASE_HOST "myhomie-72e49-default-rtdb.firebaseio.com"
#define FIREBASE_AUTH "WJfbu2S9GVfHcwDIgBvZcbYFhv2LBiiPmsoSro7T"
FirebaseData firebaseData;
FirebaseData firebaseData1;
FirebaseData firebaseData2;
FirebaseData firebaseData3;

String path      = "/";
String path1     = "/MayLanh";
String path2     = "/Quat";
String path3     = "/Den";


const IPAddress apIP(192, 168, 1, 1);
const char* apSSID = "DEVICE_SETUP";
boolean settingMode;
String ssidList;          // Bien liet ke cac mang wifi xung quanh


DNSServer dnsServer;
ESP8266WebServer webServer(80);


volatile bool state = false;
volatile bool but_1 = false;
void Touch1_Callback(void);
void Touch2_Callback(void);
void Touch3_Callback(void);

void printResult(FirebaseData &data);

volatile bool ML = false;
volatile bool Q = false;
volatile bool D = false;

volatile bool state1 = false;
volatile bool state2 = false;
volatile bool state3 = false;

void setup()
{
 Serial.begin(115200);

 pinMode(RL1, OUTPUT);;
 pinMode(RL2, OUTPUT);
 pinMode(RL3, OUTPUT);

 attachInterrupt(digitalPinToInterrupt(Touch1), Touch1_Callback, CHANGE);
 attachInterrupt(digitalPinToInterrupt(Touch2), Touch2_Callback, CHANGE);
 attachInterrupt(digitalPinToInterrupt(Touch3), Touch3_Callback, CHANGE);

 
 EEPROM.begin(512);       //
 delay(10);
  if (restoreConfig()) {                        //neu co du lieu wifi trong EEPROM thi ket noi va kiem tra ket noi voi wifi do
    if (checkConnection()) {
      settingMode = false;                      //set che do cai dat la false de bo qua buoc settingMode
      startWebServer();                         //khoi tao 1 webserver de truy cap
      Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
      Firebase.reconnectWiFi(true);

      
      firebaseData1.setBSSLBufferSize(1024, 1024);
      firebaseData1.setResponseSize(1024);
      if (!Firebase.beginStream(firebaseData1, path1))
      {
        Serial.println("------------------------------------");
        Serial.println("Can't begin stream connection...");
        Serial.println("REASON: " + firebaseData1.errorReason());
        Serial.println("------------------------------------");
        Serial.println();
      }


      firebaseData2.setBSSLBufferSize(1024, 1024);  
      firebaseData2.setResponseSize(1024);
      if (!Firebase.beginStream(firebaseData2, path2))
      {
        Serial.println("------------------------------------");
        Serial.println("Can't begin stream connection...");
        Serial.println("REASON: " + firebaseData2.errorReason());
        Serial.println("------------------------------------");
        Serial.println();
      }

      firebaseData3.setBSSLBufferSize(1024, 1024); 
      firebaseData3.setResponseSize(1024);
      if (!Firebase.beginStream(firebaseData3, path3))
      {
        Serial.println("------------------------------------");
        Serial.println("Can't begin stream connection...");
        Serial.println("REASON: " + firebaseData3.errorReason());
        Serial.println("------------------------------------");
        Serial.println();
      }
      
      return;                                   
    }
  }
  settingMode = true;                           // set che do cai dat lai thanh true
  setupMode(); 
  
}

void loop() 
{
  if (settingMode) {                      //neu settingMode == 1 thi 
    dnsServer.processNextRequest();
    }
  webServer.handleClient();
  
    if(ML){
      digitalWrite(RL1, state1);
      ML = false;
      if(state1){
        Firebase.setString(firebaseData, path + "/MayLanh", "ON");
      }else {
        Firebase.setString(firebaseData, path + "/MayLanh", "OFF");
      }   
    }

//-------------------------------------------------------------------------------------------
    if(Q){
        digitalWrite(RL2, state2);
        Q = false;
        if(state2){
          Firebase.setString(firebaseData, path + "/Quat", "ON");
        }else {
          Firebase.setString(firebaseData, path + "/Quat", "OFF");
        }   
      }
//-------------------------------------------------------------------------------------------
    if(D){
        digitalWrite(RL3, state3);
        D = false;
        if(state3){
          Firebase.setString(firebaseData, path + "/Den", "ON");
        }else {
          Firebase.setString(firebaseData, path + "/Den", "OFF");
        }   
      }
//-------------------------------------------------------------------------------------------
// may lanh
  if (!Firebase.readStream(firebaseData1))
  {
    Serial.println("------------------------------------");
    Serial.println("Can't read stream data...");
    Serial.println("REASON: " + firebaseData1.errorReason());
    Serial.println("------------------------------------");
    Serial.println();
  }
  if (firebaseData1.streamTimeout())
  {
    Serial.println("Stream timeout, resume streaming...");
    Serial.println();
  }
  if (firebaseData1.streamAvailable())
  {
    if(firebaseData1.stringData()== "ON"){
      state1 = true;
      digitalWrite(RL1,state1);
      Serial.println("May Lanh : ON");
      }
    else{
        state1 = false;
       digitalWrite(RL1,state1);
       Serial.println("May Lanh : OFF");
      }
  }

//---------------------------------------------------------------------------

// quat
   if (!Firebase.readStream(firebaseData2))
  {
    Serial.println("------------------------------------");
    Serial.println("Can't read stream data...");
    Serial.println("REASON: " + firebaseData2.errorReason());
    Serial.println("------------------------------------");
    Serial.println();
  }
  if (firebaseData2.streamTimeout())
  {
    Serial.println("Stream timeout, resume streaming...");
    Serial.println();
  }
  if (firebaseData2.streamAvailable())
  {
//    Serial.println("------------------------------------");
//    Serial.println("Stream Data available... Printing");
//    Serial.println("STREAM PATH: " + firebaseData2.streamPath());
//    Serial.println("EVENT PATH: " + firebaseData2.dataPath());
//    Serial.println("DATA TYPE: " + firebaseData2.dataType());
//    Serial.println("EVENT TYPE: " + firebaseData2.eventType());
//    Serial.print("VALUE: ");
    if(firebaseData2.stringData()== "ON"){
       state2 = true;
      digitalWrite(RL2,state2);
      Serial.println("Quat : ON");
      }
    else{
       state2 = false;
       digitalWrite(RL2,state2);
       Serial.println("Quat : OFF");
        }
  }

//------------------------------------------------------------------------------------------
 
// den   
   if (!Firebase.readStream(firebaseData3))
  {
    Serial.println("------------------------------------");
    Serial.println("Can't read stream data...");
    Serial.println("REASON: " + firebaseData3.errorReason());
    Serial.println("------------------------------------");
    Serial.println();
  }
  if (firebaseData3.streamTimeout())
  {
    Serial.println("Stream timeout, resume streaming...");
    Serial.println();
  }
  if (firebaseData3.streamAvailable())
  {

    if(firebaseData3.stringData()== "ON"){
       state3 = true;
      digitalWrite(RL3,state3);
      Serial.println("Den : ON");
      }
    else{
       state3 = false;
       digitalWrite(RL3,state3);
       Serial.println("Den : OFF");
        }
  } 
}


void IRAM_ATTR Touch1_Callback (){
   ML = true;  
   state1 = !state1;   
}

void IRAM_ATTR Touch2_Callback (){
   Q = true;  
   state2 = !state2;   
}

void IRAM_ATTR Touch3_Callback (){
   D = true;  
   state3 = !state3;   
}
 //Firebase.setString(firebaseData, path + "/NhietDo", "37");
 
// Firebase.getInt(firebaseData, path + "/NhietDo");
// x = firebaseData.stringData();
//// Serial.print("Nhiet Do: ");
//// Serial.println(x);
// uart1 += x;
// Firebase.getInt(firebaseData, path + "/DoAm");
// y = firebaseData.stringData();
////Serial.print("Do Am: ");
// Serial.println(y);
// uart1 += y;
// //Serial.println(uart1);
// uart1 = "";

// if (mySerial.available())
//  {
//    String str = mySerial.readString();
//    String nhietdo = str.substring(0, 2);
//    Firebase.setString(firebaseData, path + "/NhietDo", nhietdo);
//    Serial.println(nhietdo);
//
//
//    String doam = str.substring(2, 4);
//    Firebase.setString(firebaseData, path + "/DoAm", doam);
//    Serial.println(doam);
//  }
//
//  String co = String(mq7.getPPM(),0); 
//  if( co == "nan"){
//      Firebase.setString(firebaseData, path + "/Alert", "ON");
//  }else
//  {
//      Firebase.setString(firebaseData, path + "/Alert", "OFF");
//  }
//  Serial.println(co);
//  Firebase.setString(firebaseData, path + "/Co2",co);
//
//  Firebase.setString(firebaseData, path + "/NhietDo", String(dht.readTemperature(),0));
//  Firebase.setString(firebaseData, path + "/DoAm", String(dht.readHumidity(),0));
//

  

//mySerial.print("OFF/ONN/ONN\n");
// Firebase.getInt(firebaseData, path + "/MayLanh");
// MayLanh  = firebaseData.stringData();
// Serial.print("May Lanh: ");
// Serial.print(MayLanh);
// if (MayLanh == "ON")
// {
//  uart2 += "ONN/";
// }
// else {
//   uart2 += MayLanh;
//   uart2 += "/";
// }
//;
//
// Firebase.getInt(firebaseData, path + "/Den");
// Den  = firebaseData.stringData();
// Serial.print("    ,Den: ");
// Serial.print(Den);
//if (Den == "ON")
// {
//  uart2 += "ONN/";
// }
//
// else {
//   uart2 += Den;
//   uart2 += "/";
//
// } 
//
// 
// Firebase.getInt(firebaseData, path + "/Quat");
// Quat  = firebaseData.stringData();
// Serial.print("    ,Quat: ");
// Serial.println(Quat);
//if (Quat == "ON")
// {
//  uart2 += "ONN";
// }
//
// else {
//   uart2 += Quat;
// }
//  uart2 += '\n';
//  mySerial.print(uart2);
//  Serial.println(uart2);
 //mySerial.println(uart2);
// uart2 = "";


//Doc duu lieu trong EEPROM va ket noi voi mang wifi
boolean restoreConfig() {
  Serial.println("Reading EEPROM...");
  String ssid = "";
  String pass = "";
  if (EEPROM.read(0) != 0) {                      //neu duu lieu doc ra tu EEPROM khac 0 thi doc du lieu
    for (int i = 0; i < 32; ++i) {                //32 o nho dau tieu la chua ten mang wifi SSID
      ssid += char(EEPROM.read(i));
    }
    Serial.print("SSID: ");
    Serial.println(ssid);
    for (int i = 32; i < 96; ++i) {               //o nho tu 32 den 96 la chua PASSWORD
      pass += char(EEPROM.read(i));
    }
    Serial.print("Password: ");
    Serial.println(pass);
    WiFi.begin(ssid.c_str(), pass.c_str());       //ket noi voi mang WIFI duoc luu trong EEPROM
    return true;
  }
  else {
    Serial.println("Config not found.");
    return false;
  }
}
//-----------Kiem tra lai ket noi voi WIFI-----------------------
boolean checkConnection() {
  int count = 0;
  Serial.print("Waiting for Wi-Fi connection");
  while ( count < 30 ) {
    if (WiFi.status() == WL_CONNECTED) {      //neu ket noi thanh cong thi in ra connected!
      Serial.println();
      Serial.println("Connected!");
      return (true);
    }
    delay(500);
    Serial.print(".");
    count++;
    
  }
  Serial.println("Timed out.");
  return false;
}
//-----------------Thiet lap mot WEBSERVER-------------------------------
void startWebServer() {
  if (settingMode) {                            //neu o chua settingMode la true thi thiet lap 1 webserver
    Serial.print("Starting Web Server at ");
    Serial.println(WiFi.softAPIP());
    webServer.on("/settings", []() {
      String s = "<h1>Wi-Fi Settings</h1><p>Please enter your password by selecting the SSID.</p>";
      s += "<form method=\"get\" action=\"setap\"><label>SSID: </label><select name=\"ssid\">";
      s += ssidList;
      s += "</select><br>Password: <input name=\"pass\" length=64 type=\"password\"><input type=\"submit\"></form>";
      webServer.send(200, "text/html", makePage("Wi-Fi Settings", s));
    });
    webServer.on("/setap", []() {
      for (int i = 0; i < 96; ++i) {
        EEPROM.write(i, 0);               //xoa bo nho EEPROM
      }
      String ssid = urlDecode(webServer.arg("ssid"));
      Serial.print("SSID: ");
      Serial.println(ssid);
      String pass = urlDecode(webServer.arg("pass"));
      Serial.print("Password: ");
      Serial.println(pass);
      Serial.println("Writing SSID to EEPROM...");
      for (int i = 0; i < ssid.length(); ++i) {
        EEPROM.write(i, ssid[i]);
      }
      Serial.println("Writing Password to EEPROM...");
      for (int i = 0; i < pass.length(); ++i) {
        EEPROM.write(32 + i, pass[i]);
      }
      EEPROM.commit();
      Serial.println("Write EEPROM done!");
      String s = "<h1>Setup complete.</h1><p>device will be connected to \"";
      s += ssid;
      s += "\" after the restart.";
      webServer.send(200, "text/html", makePage("Wi-Fi Settings", s));
      ESP.restart();
    });
    webServer.onNotFound([]() {
      String s = "<h1>AP mode</h1><p><a href=\"/settings\">Wi-Fi Settings</a></p>";
      webServer.send(200, "text/html", makePage("AP mode", s));
    });
  }
  else {
    Serial.print("Starting Web Server at ");
    Serial.println(WiFi.localIP());
    webServer.on("/", []() {
      String s = "<h1>STA mode</h1><p><a href=\"/reset\">Reset Wi-Fi Settings</a></p>";
      webServer.send(200, "text/html", makePage("STA mode", s));
    });
    webServer.on("/reset", []() {    // kiem tra duong dan"/reset" thi xoa EEPROM
      for (int i = 0; i < 96; ++i) {
        EEPROM.write(i, 0);
      }
      EEPROM.commit();
      String s = "<h1>Wi-Fi settings was reset.</h1><p>Please reset device.</p>";
      webServer.send(200, "text/html", makePage("Reset Wi-Fi Settings", s));
    });
  }
  webServer.begin();
}
//-----------------Che do cai dat wifi cho esp8266----------------------
void setupMode() {
  WiFi.mode(WIFI_STA);            //che do hoat dong la May Tram Station
  WiFi.disconnect();              //ngat ket noi wifi
  delay(100);
  int n = WiFi.scanNetworks();    //quet cac mang wifi xung quanh xem co bao nhieu mang
  delay(100);
  Serial.println("");
  for (int i = 0; i < n; ++i) {    //dua danh sach wifi vao list
    ssidList += "<option value=\"";
    ssidList += WiFi.SSID(i);
    ssidList += "\">";
    ssidList += WiFi.SSID(i);
    ssidList += "</option>";
  }
  delay(100);
  WiFi.mode(WIFI_AP);               // chuyen sang che dong Access point
  WiFi.softAPConfig(apIP, apIP, IPAddress(255, 255, 255, 0));
  WiFi.softAP(apSSID,"12345678");              //thiet lap 1 open netword WiFi.softAP(ssid, password)
  dnsServer.start(53, "*", apIP);
  startWebServer();
  Serial.print("Starting Access Point at \"");
  Serial.print(apSSID);
  Serial.println("\"");
}

String makePage(String title, String contents) {
  String s = "<!DOCTYPE html><html><head>";
  s += "<meta name=\"viewport\" content=\"width=device-width,user-scalable=0\">";
  s += "<title>";
  s += title;
  s += "</title></head><body>";
  s += contents;
  s += "</body></html>";
  return s;
}

String urlDecode(String input) {
  String s = input;
  s.replace("%20", " ");
  s.replace("+", " ");
  s.replace("%21", "!");
  s.replace("%22", "\"");
  s.replace("%23", "#");
  s.replace("%24", "$");
  s.replace("%25", "%");
  s.replace("%26", "&");
  s.replace("%27", "\'");
  s.replace("%28", "(");
  s.replace("%29", ")");
  s.replace("%30", "*");
  s.replace("%31", "+");
  s.replace("%2C", ",");
  s.replace("%2E", ".");
  s.replace("%2F", "/");
  s.replace("%2C", ",");
  s.replace("%3A", ":");
  s.replace("%3A", ";");
  s.replace("%3C", "<");
  s.replace("%3D", "=");
  s.replace("%3E", ">");
  s.replace("%3F", "?");
  s.replace("%40", "@");
  s.replace("%5B", "[");
  s.replace("%5C", "\\");
  s.replace("%5D", "]");
  s.replace("%5E", "^");
  s.replace("%5F", "-");
  s.replace("%60", "`");
  return s;
}
