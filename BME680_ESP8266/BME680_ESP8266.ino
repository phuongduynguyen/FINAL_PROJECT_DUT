/* D1 (GPIO5): SCL
 * D2 (GPIO4): SDA
   D5 (GPIO14): buzzer
*/

#include <Wire.h>
#include "bsec.h"
#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>
#include "FirebaseESP8266.h"
#include <ArduinoJson.h>
#include <String.h>
#include <DNSServer.h>
#include <WiFiClient.h>
#include <EEPROM.h>
#include <ESP8266WebServer.h>
//#include <Adafruit_Sensor.h>
#include "SSD1306.h"
#define SEALEVELPRESSURE_HPA (1013.25)
#define FIREBASE_HOST "myhomie-72e49-default-rtdb.firebaseio.com"
#define FIREBASE_AUTH "WJfbu2S9GVfHcwDIgBvZcbYFhv2LBiiPmsoSro7T"
#define BUZ 14

FirebaseData firebaseData;
FirebaseJson json;
String path      = "/";

Bsec iaqSensor;
String output;
void checkIaqSensorStatus(void);
void errLeds(void);

const IPAddress apIP(192, 168, 1, 1);
const char* apSSID = "ESP8266_BME680_SETUP";
boolean settingMode;
String ssidList;          // Bien liet ke cac mang wifi xung quanh

DNSServer dnsServer;
ESP8266WebServer webServer(80);


SSD1306  display(0x3c, 4, 5);

void setup() {
  // put your setup code here, to run once:
  Serial.begin(115200);
  Wire.begin();
  EEPROM.begin(512);
  pinMode(BUZ, OUTPUT);

  iaqSensor.begin(BME680_I2C_ADDR_SECONDARY, Wire);
  output = "\nBSEC library version " + String(iaqSensor.version.major) + "." + String(iaqSensor.version.minor) + "." + String(iaqSensor.version.major_bugfix) + "." + String(iaqSensor.version.minor_bugfix);
 
  Serial.println(output);
  checkIaqSensorStatus();
  bsec_virtual_sensor_t sensorList[10] = {
    BSEC_OUTPUT_RAW_TEMPERATURE,
    BSEC_OUTPUT_RAW_PRESSURE,
    BSEC_OUTPUT_RAW_HUMIDITY,
    BSEC_OUTPUT_RAW_GAS,
    BSEC_OUTPUT_IAQ,
    BSEC_OUTPUT_STATIC_IAQ,
    BSEC_OUTPUT_CO2_EQUIVALENT,
    BSEC_OUTPUT_BREATH_VOC_EQUIVALENT,
    BSEC_OUTPUT_SENSOR_HEAT_COMPENSATED_TEMPERATURE,
    BSEC_OUTPUT_SENSOR_HEAT_COMPENSATED_HUMIDITY,
  };
 
  iaqSensor.updateSubscription(sensorList, 10, BSEC_SAMPLE_RATE_LP);
  checkIaqSensorStatus();

  display.init();
  display.setFont(ArialMT_Plain_16);
  display.drawString(0, 0, "Hello world");
  display.display();
  display.clear();


  if (restoreConfig()) {                        //neu co du lieu wifi trong EEPROM thi ket noi va kiem tra ket noi voi wifi do
    if (checkConnection()) {
      settingMode = false;  //set che do cai dat la false de bo qua buoc settingMode
      Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
      Firebase.reconnectWiFi(true);
      startWebServer();                         //khoi tao 1 webserver de truy cap
      return;                                   
    }
  }
  settingMode = true;                           // set che do cai dat lai thanh true
  setupMode();    
  }
 
void loop() {
  // put your main code here, to run repeatedly
 // server.handleClient(); 
  if (settingMode) {                      //neu settingMode == 1 thi 
    dnsServer.processNextRequest();
  }
  webServer.handleClient();
  if (iaqSensor.run()) { // If new data is available 
  } else {
    checkIaqSensorStatus();
  }

  display.clear();
  if ((iaqSensor.staticIaq > 0)  && (iaqSensor.staticIaq  <= 50)) {
   // IAQsts = "Good"; 
    display.drawString(5, 45, "IAQ     : Good"   );
  }
  else if ((iaqSensor.staticIaq > 51)  && (iaqSensor.staticIaq  <= 100)) {
 //   IAQsts = "Average";
    display.drawString(5, 45, "IAQ     : Average"   );
  }
  else if ((iaqSensor.staticIaq > 101)  && (iaqSensor.staticIaq  <= 150)) {
   // IAQsts = "Little Bad";
    display.drawString(5, 45, "IAQ     : Little Bad" );
  }
  else if ((iaqSensor.staticIaq > 151)  && (iaqSensor.staticIaq  <= 200)) {
  //  IAQsts = "Bad";
    display.drawString(5, 45, "IAQ     : Bad"  );
  }
  else if ((iaqSensor.staticIaq > 201)  && (iaqSensor.staticIaq  <= 300)) {
    //IAQsts = "Worse";
    display.drawString(5, 45, "IAQ     : Worse" );
  }
  else if ((iaqSensor.staticIaq > 301)) {
   // IAQsts = "Very Bad";
    display.drawString(5,45, "IAQ     : Very Bad"  );
  } 

  if((iaqSensor.temperature)>=70 ){
    digitalWrite(BUZ,1);   
  }else {
    digitalWrite(BUZ,0);
  }
  
  Firebase.setString(firebaseData, path + "/IAQ", String(iaqSensor.staticIaq));
  Firebase.setString(firebaseData, path + "/NhietDo", String(iaqSensor.temperature));
  Firebase.setString(firebaseData, path + "/DoAm", String(iaqSensor.humidity));
  Firebase.setString(firebaseData, path + "/Co2", String(iaqSensor.co2Equivalent));
  Firebase.setString(firebaseData, path + "/ApSuat", String(iaqSensor.pressure  ));
 
  display.drawString(4, 5, "Temp  :" + String(iaqSensor.temperature) + "°C" );
  display.drawString(5, 25, "Humid:" + String(iaqSensor.humidity) + "%" );
 // display.drawString(5, 48, "IAQ  : Good");
  display.display();
  delay(200);
}
 
// Helper function definitions
void checkIaqSensorStatus(void)
{
  if (iaqSensor.status != BSEC_OK) {
    if (iaqSensor.status < BSEC_OK) {
      output = "BSEC error code : " + String(iaqSensor.status);
      Serial.println(output);
      for (;;)
        errLeds(); /* Halt in case of failure */
    } else {
      output = "BSEC warning code : " + String(iaqSensor.status);
      Serial.println(output);
    }
  }
 
  if (iaqSensor.bme680Status != BME680_OK) {
    if (iaqSensor.bme680Status < BME680_OK) {
      output = "BME680 error code : " + String(iaqSensor.bme680Status);
      Serial.println(output);
      for (;;)
        errLeds(); /* Halt in case of failure */
    } else {
      output = "BME680 warning code : " + String(iaqSensor.bme680Status);
      Serial.println(output);
    }
  }
}

//void handle_OnConnect() {
//
//  temperature = iaqSensor.temperature;
//  humidity = iaqSensor.humidity;
//  pressure = iaqSensor.pressure / 100.0;
//  IAQ = iaqSensor.staticIaq;
//  carbon = iaqSensor.co2Equivalent;
//  VOC = iaqSensor.breathVocEquivalent;
//  
//  server.send(200, "text/html", SendHTML(temperature, humidity, pressure, IAQ, carbon, VOC, IAQsts));
//}
//
//void handle_NotFound() {
//  server.send(404, "text/plain", "Not found");
//}
//
//String SendHTML(float temperature, float humidity, float pressure, float IAQ, float carbon, float VOC, const char* IAQsts) {
//String html = "<!DOCTYPE html>";
//html += "<html>";
//html += "<head>";
//html += "<title>BME680 Webserver</title>";
//html += "<meta name='viewport' content='width=device-width, initial-scale=1.0'>";
//html += "<link rel='stylesheet' href='https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.7.2/css/all.min.css'>";
//html += "<link rel='stylesheet' type='text/css' href='styles.css'>";
//html += "<style>";
//html += "body { background-color: #fff; font-family: sans-serif; color: #333333; font: 12px Helvetica, sans-serif box-sizing: border-box;}";
//html += "#page { margin: 18px; background-color: #fff;}";
//html += ".container { height: inherit; padding-bottom: 18px;}";
//html += ".header { padding: 18px;}";
//html += ".header h1 { padding-bottom: 0.3em; color: #f4a201; font-size: 25px; font-weight: bold; font-family: Garmond, 'sans-serif'; text-align: center;}";
//html += "h2 { padding-bottom: 0.2em; border-bottom: 1px solid #eee; margin: 2px; text-align: center;}";
//html += ".box-full { padding: 18px; border 1px solid #ddd; border-radius: 1em 1em 1em 1em; box-shadow: 1px 7px 7px 1px rgba(0,0,0,0.4); background: #fff; margin: 18px; width: 300px;}";
//html += "@media (max-width: 494px) { #page { width: inherit; margin: 5px auto; } #content { padding: 1px;} .box-full { margin: 8px 8px 12px 8px; padding: 10px; width: inherit;; float: none; } }";
//html += "@media (min-width: 494px) and (max-width: 980px) { #page { width: 465px; margin 0 auto; } .box-full { width: 380px; } }";
//html += "@media (min-width: 980px) { #page { width: 930px; margin: auto; } }";
//html += ".sensor { margin: 10px 0px; font-size: 2.5rem;}";
//html += ".sensor-labels { font-size: 1rem; vertical-align: middle; padding-bottom: 15px;}";
//html += ".units { font-size: 1.2rem;}";
//html += "hr { height: 1px; color: #eee; background-color: #eee; border: none;}";
//html += "</style>";
//
////Ajax Code Start
//  html += "<script>\n";
//  html += "setInterval(loadDoc,1000);\n";
//  html += "function loadDoc() {\n";
//  html += "var xhttp = new XMLHttpRequest();\n";
//  html += "xhttp.onreadystatechange = function() {\n";
//  html += "if (this.readyState == 4 && this.status == 200) {\n";
//  html += "document.body.innerHTML =this.responseText}\n";
//  html += "};\n";
//  html += "xhttp.open(\"GET\", \"/\", true);\n";
//  html += "xhttp.send();\n";
//  html += "}\n";
//  html += "</script>\n";
//  //Ajax Code END
//  
//html += "</head>";
//html += "<body>";
//html += "<div id='page'>";
//html += "<div class='header'>";
//html += "<h1>BME680 IAQ Monitoring System</h1>";
//html += "</div>";
//html += "<div id='content' align='center'>";
//html += "<div class='box-full' align='left'>";
//html += "<h2>";
//html += "IAQ Status: ";
//html += IAQsts;
//html += "</h2>";
//html += "<div class='sensors-container'>";
//
////For Temperature
//html += "<div class='sensors'>";
//html += "<p class='sensor'>";
//html += "<i class='fas fa-thermometer-half' style='color:#0275d8'></i>";
//html += "<span class='sensor-labels'> Temperature </span>";
//html += temperature;
//html += "<sup class='units'>°C</sup>";
//html += "</p>";
//html += "<hr>";
//html += "</div>";
//
////For Humidity
//html += "<p class='sensor'>";
//html += "<i class='fas fa-tint' style='color:#0275d8'></i>";
//html += "<span class='sensor-labels'> Humidity </span>";
//html += humidity;
//html += "<sup class='units'>%</sup>";
//html += "</p>";
//html += "<hr>";
//
////For Pressure
//html += "<p class='sensor'>";
//html += "<i class='fas fa-tachometer-alt' style='color:#ff0040'></i>";
//html += "<span class='sensor-labels'> Pressure </span>";
//html += pressure;
//html += "<sup class='units'>hPa</sup>";
//html += "</p>";
//html += "<hr>";
//
////For VOC IAQ
//html += "<div class='sensors'>";
//html += "<p class='sensor'>";
//html += "<i class='fab fa-cloudversify' style='color:#483d8b'></i>";
//html += "<span class='sensor-labels'> IAQ </span>";
//html += IAQ;
//html += "<sup class='units'>PPM</sup>";
//html += "</p>";
//html += "<hr>";
//
////For C02 Equivalent
//html += "<p class='sensor'>";
//html += "<i class='fas fa-smog' style='color:#35b22d'></i>";
//html += "<span class='sensor-labels'> Co2 Eq. </span>";
//html += carbon;
//html += "<sup class='units'>PPM</sup>";
//html += "</p>";
//html += "<hr>";
//
////For Breath VOC
//html += "<p class='sensor'>";
//html += "<i class='fas fa-wind' style='color:#0275d8'></i>";
//html += "<span class='sensor-labels'> Breath VOC </span>";
//html += VOC;
//html += "<sup class='units'>PPM</sup>";
//html += "</p>";
//
//
//html += "</div>";
//html += "</div>";
//html += "</div>";
//html += "</div>";
//html += "</div>";
//html += "</body>";
//html += "</html>";
//return html;
//}
// 
void errLeds(void)
{
  pinMode(LED_BUILTIN, OUTPUT);
  digitalWrite(LED_BUILTIN, HIGH);
  delay(100);
  digitalWrite(LED_BUILTIN, LOW);
  delay(100);
}
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
