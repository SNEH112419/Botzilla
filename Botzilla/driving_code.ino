// Define Pins here
#define MIN1 D5
#define MIN2 D6
#define MIN3 D7
#define MIN4 D8

void initialize_bot() {
  pinMode(MIN1, OUTPUT);
  pinMode(MIN2, OUTPUT);
  pinMode(MIN3, OUTPUT);
  pinMode(MIN4, OUTPUT);
}

void handle_packet(char* packet) {
  // assumes that the the handling packet size to be 5 and should be 5

  switch (packet[0]) {
    case 'f': 
      GPOS = (1 << MIN1) | (1 << MIN3); // Set MIN1 and MIN3 HIGH
      GPOC = (1 << MIN2) | (1 << MIN4); // Set MIN2 and MIN4 LOW
      
      break;

    case 'b':
      GPOC = (1 << MIN1) | (1 << MIN3); // Set MIN1 and MIN3 HIGH
      GPOS = (1 << MIN2) | (1 << MIN4); // Set MIN2 and MIN4 LOW

      break;

    case 'r':
      GPOC = (1 << MIN1) | (1 << MIN2) | (1 << MIN4);
      GPOS = (1 << MIN3);

      // GPOC = (1 << MIN2) | (1 << MIN3);
      // GPOS = (1 << MIN1) | (1 << MIN4);

      break;

    case 'l':
      GPOC = (1 << MIN2) | (1 << MIN3) | (1 << MIN4);
      GPOS = (1 << MIN1);
      
      // GPOS = (1 << MIN2) | (1 << MIN3);
      // GPOC = (1 << MIN1) | (1 << MIN4);

      break;

    default:
      handle_no_packet();
      break;
  }
  delay(10);
}

void handle_no_packet()
{
  // Stop
  GPOC = (1 << MIN1) | (1 << MIN2) | (1 << MIN3) | (1 << MIN4);
}
