#include<Servo.h>
// Define Pins here

// Right side is A channel and left is B
#define MIN1 D5
#define MIN2 D6
#define MIN3 D7
#define MIN4 D8

#define ENA D1
#define ENB D2

#define ELEVATION_SERVO D4
#define GRIP_SERVO D3

Servo elevation_servo;
Servo grip_servo;

void initialize_bot() {
  pinMode(MIN1, OUTPUT);
  pinMode(MIN2, OUTPUT);
  pinMode(MIN3, OUTPUT);
  pinMode(MIN4, OUTPUT);

  pinMode(ENA, OUTPUT);
  pinMode(ENA, OUTPUT);

  elevation_servo.attach(ELEVATION_SERVO, 500, 2000);
  grip_servo.attach(GRIP_SERVO, 500, 2000);
}

void handle_packet(ControlData* packet) {
  if (packet->flags & 1 != 0) {
    GPOS = (1 << MIN1) | (1 << MIN2) | (1 << MIN3) | (1 << MIN4);
    analogWrite(ENA, 255);
    analogWrite(ENB, 255);
    handle_gripper(packet);
    delay(10);
    return;
  }

  // Convert unsigned joystick to signed range (-127 to 127)
  int8_t forward = packet->forward;
  int8_t turn = packet->turn;
  uint8_t speedFactor = packet->speed;

  // Arcade drive calculation
  int leftSpeed = forward + turn;
  int rightSpeed = forward - turn;

  // Scale by speed factor (0-255)
  leftSpeed = (leftSpeed * speedFactor) / 127;  // 128 = max value of signed byte
  rightSpeed = (rightSpeed * speedFactor) / 127;

  int pwmValueL = constrain(abs(leftSpeed), 0, 255);
  int pwmValueR = constrain(abs(rightSpeed), 0, 255);

  int mask_GPOC = 0;
  int mask_GPOS = 0;

  if (rightSpeed == 0) {
    mask_GPOS |= (1 << MIN2) | (1 << MIN1);
    pwmValueR = 255;
  }
  else if (rightSpeed > 0) {
    mask_GPOS |= (1 << MIN1);
    mask_GPOC |= (1 << MIN2);
  } else {
    mask_GPOS |= (1 << MIN2);
    mask_GPOC |= (1 << MIN1);
  }

  if (leftSpeed == 0) {
    mask_GPOS |= (1 << MIN3) | (1 << MIN4);
    pwmValueL = 255;
  }
  else if (leftSpeed > 0) {
    mask_GPOS |= (1 << MIN3);
    mask_GPOC |= (1 << MIN4);
  } else {
    mask_GPOS |= (1 << MIN4);
    mask_GPOC |= (1 << MIN3);
  }

#ifdef DEBUG
  Serial.printf("left : %d; right speed: %d", pwmValueL, pwmValueR);
#endif

  GPOC = mask_GPOC;
  GPOS = mask_GPOS;
  analogWrite(ENA, pwmValueR);
  analogWrite(ENB, pwmValueL);

  handle_gripper(packet);
  delay(10);
}

void handle_gripper(ControlData* packet) {
  if (packet->elevation <= 180)
  {
    elevation_servo.write(packet->elevation);
  }
  
  if (packet->grip <= 180) 
  {
    grip_servo.write(packet->grip);
  }
}

void handle_no_packet() {
  GPOS = (1 << MIN1) | (1 << MIN2) | (1 << MIN3) | (1 << MIN4);
}
