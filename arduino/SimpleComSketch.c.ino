void setup() {
  // put your setup code here, to run once:
  Serial.begin();
}

void loop() {
  // put your main code here, to run repeatedly:
  bool connected = Bean.getConnectionState();
  if (connected){
  AccelerationReading accel = {0,0,0};
  accel = Bean.getAcceleration();
  Serial.write(accel.xAxis);
  Serial.write(accel.yAxis);
  Serial.write(accel.zAxis);
  delay(20);
  }
  else{
    Bean.sleep(0xFFFFFFFF);
  }
}
