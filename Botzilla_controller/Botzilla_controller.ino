// Choose active input mode (uncomment ONE)
#define USE_JOYSTICK
// #define USE_BUTTONS

// --- Compile-Time Guard ---
#if defined(USE_JOYSTICK) && defined(USE_BUTTONS)
  #error "Configuration Error: Both USE_JOYSTICK and USE_BUTTONS are defined!"
#elif !defined(USE_JOYSTICK) && !defined(USE_BUTTONS)
  #error "Configuration Error: You must define either USE_JOYSTICK or USE_BUTTONS!"
#endif

// Most ESP32 dev boards use GPIO 2 for the built-in LED.
// If your board doesn't define LED_BUILTIN, GPIO 2 is the standard fallback.
#ifndef LED_BUILTIN
  #define LED_BUILTIN 2
#endif

#include <WiFi.h>
#include <WiFiUdp.h>

#include <esp_wifi.h>
#include <esp_netif.h>

#include "../Botzilla/utils.h"

const char *ssid = "botzilla-ap";
const char *password = "botzilla rocks";

#define FORWARD_BTN 4
#define BACK_BTN    5
#define RIGHT_BTN   22
#define LEFT_BTN    23

#define JOY_X_PIN 34  // Controls turn (Left / Right)
#define JOY_Y_PIN 35  // Controls forward (Forward / Back)
#define DEADZONE  15

// UDP Configuration
WiFiUDP udp;
// Default AP subnet is usually 192.168.4.x; sending to 192.168.4.255 broadcasts to all connected clients
const char * udpAddress = "192.168.4.255"; 
const int udpPort = 4210;

static ControlPacket packet;

void clear_packet() {
  for (int i = 0; i < sizeof(packet); i++) {
    packet.buffer[i] = 0;
  }
}
void setup_controller() {
  clear_packet();

#if defined(USE_BUTTONS)
  // Note: INPUT_PULLUP is typically preferred for mechanical buttons to avoid floating states
  const uint8_t buttonPins[] = {FORWARD_BTN, BACK_BTN, RIGHT_BTN, LEFT_BTN};
  for (uint8_t pin : buttonPins) {
    pinMode(pin, INPUT);
  }
#elif defined(USE_JOYSTICK)
  analogReadResolution(12);
#endif
}


void setup() {
  // Initialize serial communication at 115200 baud
  Serial.begin(115200);
  
  // Set the LED pin as an output
  pinMode(LED_BUILTIN, OUTPUT);

  if (WiFi.softAP(ssid, password)) {
    Serial.println("Access Point successfully started!");
  } else {
    Serial.println("Failed to start Access Point.");
  }

  IPAddress apIP = WiFi.softAPIP();
  Serial.print("Network SSID : ");
  Serial.println(ssid);
  Serial.print("AP IP Address: ");
  Serial.println(apIP);
  Serial.println("--------------------------------\n");

  // WiFi.setSleepMode(WIFI_NONE_SLEEP);

  udp.begin(udpPort);

  // Print startup message
  Serial.println("Navigator Started!");
  setup_controller();
}

void create_packet() {
#if defined(USE_BUTTONS)
  if (digitalRead(FORWARD_BTN)) {
    packet.data.forward += 127;
  }
  if (digitalRead(BACK_BTN)) {
    packet.data.forward -= 127;
  }
  if (digitalRead(RIGHT_BTN)) {
    packet.data.turn += 127;
  }
  if (digitalRead(LEFT_BTN)) {
    packet.data.turn -= 127;
  }
#elif defined(USE_JOYSTICK)
  int turnVal    = map(analogRead(JOY_X_PIN), 0, 4095, -127, 127);
  int forwardVal = map(analogRead(JOY_Y_PIN), 0, 4095, -127, 127);

  packet.data.turn    = (abs(turnVal) < DEADZONE) ? 0 : (int8_t)turnVal;
  packet.data.forward = (abs(forwardVal) < DEADZONE) ? 0 : (int8_t)forwardVal;
#endif

  packet.data.speed = 0xFF;
  
  int sum = 0;
  for (int i = 0; i < sizeof(ControlPacket) - 1; i++) {
    sum += packet.buffer[i];
  }
  packet.data.checksum = sum & 0xFF;
}

void sendUdpData() {
  // Begin transmission
  udp.beginPacket(udpAddress, udpPort);
  udp.write(packet.buffer, sizeof(ControlPacket));
  udp.endPacket();
}


int light = HIGH;
void loop() {
  uint8_t connectedDevices = WiFi.softAPgetStationNum();
  if (connectedDevices > 0) {
    create_packet();
    sendUdpData();
    clear_packet();
    digitalWrite(LED_BUILTIN, HIGH);
  }
  else {
    digitalWrite(LED_BUILTIN, light);
    light = !light;
    delay(250);
#ifdef DEBUG
    Serial.printf("Forward %d, back %d right %d left %d \n", 
      digitalRead(FORWARD_BTN), digitalRead(BACK_BTN),
      digitalRead(RIGHT_BTN), digitalRead(LEFT_BTN));
#endif
  }
}
