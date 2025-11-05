// Define Pins here

// Right side is A channel and left is B
#define MIN1 D5
#define MIN2 D6
#define MIN3 D7
#define MIN4 D8

#define ENA D1
#define ENB D2

void initialize_bot() {
  pinMode(MIN1, OUTPUT);
  pinMode(MIN2, OUTPUT);
  pinMode(MIN3, OUTPUT);
  pinMode(MIN4, OUTPUT);

  pinMode(ENA, OUTPUT);
  pinMode(ENA, OUTPUT);
}

void handle_packet(ControlData* packet) {
  // Convert unsigned joystick to signed range (-127 to 127)
  int8_t forward = packet->forward;
  int8_t turn = packet->turn;
  uint8_t speedFactor = packet->speed;

  // Arcade drive calculation
  int leftSpeed = forward + turn;
  int rightSpeed = forward - turn;

  // Scale by speed factor (0-255)
  leftSpeed = (leftSpeed * speedFactor) / 128;  // 128 = max value of signed byte
  rightSpeed = (rightSpeed * speedFactor) / 128;

  int pwmValueL = abs(leftSpeed);
  int pwmValueR = abs(rightSpeed); 

  int mask_GPOC = 0;
  int mask_GPOS = 0;

  if (rightSpeed > 0) {
    mask_GPOS |= (1 << MIN1);
    mask_GPOC |= (1 << MIN2);
  } else {
    mask_GPOS |= (1 << MIN2);
    mask_GPOC |= (1 << MIN1);
  }

  if (leftSpeed > 0) {
    mask_GPOS |= (1 << MIN3);
    mask_GPOC |= (1 << MIN4);
  } else {
    mask_GPOS |= (1 << MIN4);
    mask_GPOC |= (1 << MIN3);
  }

  Serial.printf("left : %d; right speed: %d", leftSpeed, rightSpeed);

  GPOC = mask_GPOC;
  GPOS = mask_GPOS;
  analogWrite(ENA, pwmValueR);
  analogWrite(ENB, pwmValueL);

  delay(10);
}

void handle_no_packet() {
  GPOC = (1 << MIN1) | (1 << MIN2) | (1 << MIN3) | (1 << MIN4);
}
