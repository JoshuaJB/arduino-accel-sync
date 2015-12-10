/**
 * CompressAndSendAcceleration.c
 * Copyright 2015 The University of North Carolina at Chapel Hill
 */

// Interrupt code
#include <avr/io.h>
#include <avr/interrupt.h>
// Arduino std lib
#include "Arduino.h"

// Constants
#define TAG 0xC0000000
#define X_MASK 0x3FF00000
#define Y_MASK 0x000FFC00
#define Z_MASK 0x000003FF
#define BUFFER_MAX_SIZE 2
// In Hz. Beware: timer accuracy.
#define SAMPLE_RATE 1
#define CLOCK_RATE 1.6e7
#define TIMER_PRESCALER 1024
// Set accelerometer sensitivity to the maximum (+/- 16g)
const uint8_t SENSITIVITY = 16;

// Function prototypes (setup, loop, and ISR and declared elsewhere)
void setup_timers();
uint32_t compress_reading(AccelerationReading reading);
int queue_message(unsigned int bytes);

// Global circular serial buffer
int buffer_head = 0;
int buffer_tail = 0;
byte buffer[BUFFER_MAX_SIZE][6];

void setup() {
  /**
   * Bean Serial is at a fixed baud rate. Changing the value in Serial.begin()
   * has no effect.
   */
  Serial.begin();
  Bean.setAccelerationRange(SENSITIVITY);
  setup_timers();
}
void setup_timers() {
  /*** Setup timer1 for realtime sampling ***/
  
  // Critical section, disable global interrupts
  cli(); // CLear Interrupts
  // Clear any previous settings
  TCCR1B = 0;
  TCCR1A = 0;
  // Set compare match value
  OCR1A = CLOCK_RATE / (TIMER_PRESCALER * SAMPLE_RATE) - 1;// 1.6e7 / (1024 * 30)
  // Turn on clear timer on compare (CTC) mode
  TCCR1B |= 1 << WGM12;
  // Set CS10 and CS12 bits for 1024 prescaler
  TCCR1B |= 1 << CS12 | 1 << CS10; // Set bits CS10 and CS12
  // Enable timer compare interrupt
  TIMSK1 |= 1 << OCIE1A;
  // Reenable global interrupts
  sei(); // SEt Interrupts
}

void loop() {
  // Make sure that the Bean is connected
  if (!Serial || !Bean.getConnectionState())
    return;
  // Send data from our accelerometer buffer if it's non-empty
  if (buffer_head != buffer_tail) {
    /*Serial.write("Sending data ");
    Serial.print(buffer[buffer_head], BIN);
    Serial.write(" from idx ");
    Serial.print(buffer_head);
    Serial.write(".\n");*/
    // Send 6 bytes of data (48 bits)
    Serial.write(buffer[buffer_head], 6);
    Bean.setLed(0, 255, 0);
    buffer_head = (buffer_head + 1) % BUFFER_MAX_SIZE;
  }
}

int compress_reading(AccelerationReading acceleration, byte * location) {
  /**
   * Acceleration.<axis> is a 16-bit integer, but only 10 bits actually contain
   * data. We strip those extra bits here.
   */
  if (!memcpy(location, &acceleration.xAxis, 2) ||
    !memcpy(location + 2, &acceleration.yAxis, 2) ||
    !memcpy(location + 4, &acceleration.zAxis, 2))
    return 0;
  else
    return 1;
  /*
  uint32_t tempx = (uint32_t)acceleration.xAxis & X_MASK;
  uint32_t tempy = (uint32_t)acceleration.yAxis << 10 & Y_MASK;
  uint32_t tempz = (uint32_t)acceleration.zAxis << 20 & Z_MASK;
  if (Serial) {
    Serial.print(tempx, BIN);
    Serial.write('\n');
    Serial.print(tempy, BIN);
    Serial.write('\n');
    Serial.print(tempz, BIN);
    Serial.write('\n');
  }
  //Serial.write((unsigned char *)&buffer_head,1); // Junk
  return TAG 
    | ((uint32_t)acceleration.zAxis << 14 & Z_MASK)
    | ((uint32_t)acceleration.yAxis << 4 & Y_MASK)
    | ((uint32_t)acceleration.xAxis >> 6 & X_MASK);*/
}

int queue_message(byte bytes[6]) {
  int new_buffer_tail = (buffer_tail + 1) % BUFFER_MAX_SIZE;
  // if buffer full, return false
  if (buffer_head == new_buffer_tail) {
    return 0;
  }
  else {
    memcpy(buffer[buffer_tail], bytes, 6);
    // We use a circular queue for maximum efficiency
    buffer_tail = new_buffer_tail;
  }
  return 1;
}

// Handle the sampling interrupt with an Interrupt Service Routine
ISR(TIMER1_COMPA_vect) {
  Bean.setLed(255, 0, 0);
  /**
   * Get the current acceleration with range of ±16g, and a conversion of
   * 31.25e-3 g/unit.
   */
  AccelerationReading reading = Bean.getAcceleration();
  // Compress and queue the reading
  byte compressed_reading[6];
  if (!compress_reading(reading, compressed_reading) ||
    !queue_message(compressed_reading))
    Bean.setLed(0, 0, 255);
}


