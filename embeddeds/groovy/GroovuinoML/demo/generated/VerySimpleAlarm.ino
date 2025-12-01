// Wiring code generated from an ArduinoML model
// Application name: verySimpleAlarm!

long debounce = 200;

enum STATE {off, on};
STATE currentState = off;

boolean buttonBounceGuard = false;
long buttonLastDebounceTime = 0;

void setup(){
  pinMode(8, INPUT);  // button [Sensor]
  pinMode(12, OUTPUT); // led [Actuator]
  pinMode(11, OUTPUT); // buzzer [Actuator]
}

void loop() {
	switch(currentState){
		case off:
			digitalWrite(12,LOW);
			digitalWrite(11,LOW);
			buttonBounceGuard = millis() - buttonLastDebounceTime > debounce;
			if( digitalRead(8) == HIGH && buttonBounceGuard) {
				buttonLastDebounceTime = millis();
				currentState = on;
			}
		break;
		case on:
			digitalWrite(12,HIGH);
			digitalWrite(11,HIGH);
			buttonBounceGuard = millis() - buttonLastDebounceTime > debounce;
			if( digitalRead(8) == LOW && buttonBounceGuard) {
				buttonLastDebounceTime = millis();
				currentState = off;
			}
		break;
	}
}
