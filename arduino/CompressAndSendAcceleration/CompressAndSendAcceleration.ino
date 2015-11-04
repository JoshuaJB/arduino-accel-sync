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
#define BUFFER_MAX_SIZE 500
// In Hz. Beware: timer accuracy.
#define SAMPLE_RATE 30
#define CLOCK_RATE 1.6e7
#define TIMER_PRESCALER 1024


// Function prototypes (setup, loop, and ISR and declared elsewhere)
void setup_timers();
unsigned int compress_reading(AccelerationReading reading);
int queue_message(unsigned int bytes);

// Global circular serial buffer
int buffer_head = 0;
int buffer_tail = 0;
unsigned int buffer[BUFFER_MAX_SIZE];

void setup() {
  /**
   * Bean Serial is at a fixed baud rate. Changing the value in Serial.begin()
   * has no effect.
   */
  Serial.begin();
  // Set accelerometer sensitivity to the maximum (+/- 16g)
  Bean.setAccelerationRange(16);
  setup_timers();
}

void setup_timers() {
  /*** Setup timer1 for realtime sampling ***/
  
  // Critical section, disable interrupts
  cli(); // CLear Interrupts
  // Set compare value
  OCR1A = CLOCK_RATE / (TIMER_PRESCALER * SAMPLE_RATE) - 1;
  // Set CTC (clear timer on compare mode)
  TCCR1B |= 1 << WGM12;
  // Enable interrupt on CTC event
  TIMSK1 |= 1 << OCIE1A;
  // Set prescaler to 1024
  OCR1B |= 1 << CS12 | 1 << CS10;
  // Reenable interrupts
  sei(); // SEt Interrupts
}

void loop() {
  // Send data from our accelerometer buffer if it's non-empty
  if (buffer_head != buffer_tail) {
    // TODO: Check for connection first
    Serial.write(buffer[buffer_head]);
    buffer_head = (buffer_head + 1) % BUFFER_MAX_SIZE;
  }
  else
    Serial.write("Bla");
}

unsigned int compress_reading(AccelerationReading acceleration) {
  /**
   * Acceleration.<axis> is a 16-bit integer, but only 10 bits actually contain
   * data. We strip those extra bits here.
   */
  return TAG 
    | (acceleration.zAxis << 20 & Z_MASK)
    | (acceleration.yAxis << 10 & Y_MASK)
    | (acceleration.xAxis & X_MASK);
}

int queue_message(unsigned int bytes) {
  // if buffer full, return false
  if (buffer_head == (buffer_tail + 1) % BUFFER_MAX_SIZE) {
    return 0;
  }
  else {
    buffer[buffer_tail] = bytes;
    // We use a circular queue for maximum efficiency
    buffer_tail = (buffer_tail + 1) % BUFFER_MAX_SIZE;
  }
  return 1;
}

// Handle the sampling interrupt with an Interrupt Service Routine
ISR(TIMER_COMPA_vect) {
  /**
   * Get the current acceleration with range of ±16g, and a conversion of
   * 31.25e-3 g/unit.
   */
  AccelerationReading reading = Bean.getAcceleration();
  // Compress and queue the reading
  unsigned int compressed_reading = compress_reading(reading);
  queue_message(compressed_reading);
}


