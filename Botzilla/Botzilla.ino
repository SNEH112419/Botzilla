#include <ESP8266WiFi.h>
#include <WiFiUdp.h>
#include "utils.h"

const char* ssid = "Sugat 5G";
const char* password = "Sugat.dev";

WiFiUDP Udp;
unsigned int localUdpPort = 4210;  // local port to listen on
ControlPacket incomingPacket;  // buffer for incoming packets

void setup()
{
  initialize_bot();
  pinMode(LED_BUILTIN, OUTPUT);
  digitalWrite(LED_BUILTIN, 0);

  Serial.begin(115200);
  Serial.println();

  Serial.printf("Connecting to %s ", ssid);
  WiFi.hostname("Botzilla MCU");
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED)
  {
    digitalWrite(LED_BUILTIN, 0);
    delay(250);
    digitalWrite(LED_BUILTIN, 1);
    delay(250);

    Serial.print(".");
  }
  digitalWrite(LED_BUILTIN, 0);
  Serial.println(" connected");

  // Disable Wi-Fi power saving
  WiFi.setSleepMode(WIFI_NONE_SLEEP);

  Udp.begin(localUdpPort);
  Serial.printf("Now listening at IP %s, UDP port %d\n", WiFi.localIP().toString().c_str(), localUdpPort);
}


void loop()
{
  int packetSize = Udp.parsePacket();
  if (packetSize == sizeof(ControlData))
  {
    // receive incoming UDP packets
#ifdef DEBUG
    Serial.printf("Received %d bytes from %s, port %d\n", packetSize, Udp.remoteIP().toString().c_str(), Udp.remotePort());
#endif

    int len = Udp.read(incomingPacket.buffer, sizeof(ControlPacket));
    if (len > 0 && validate_packet(&incomingPacket))
    {
      handle_packet(&incomingPacket.data);
    }
    else 
    {
#ifdef DEBUG
      Serial.printf("Rejecting packet");
      Serial.print("UDP packet contents (hex): ");
      for (int i = 0; i < len; i++)
        Serial.printf("%02X ", incomingPacket.buffer[i]);
      Serial.println();
#endif
    }
  }
  else
  {
      handle_no_packet();
  }
}

bool validate_packet(ControlPacket* p) {
  int sum = 0;
  for (int i = 0; i < sizeof(ControlData) - 1; i++) {
    sum += p->buffer[i];
  }
  return (sum & 0xFF) == p->data.checksum;
}

