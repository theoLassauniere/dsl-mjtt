// Wiring code generated from an ArduinoML model
// Application name: ExceptionThrowing

long debounce = 200;

enum STATE {, off, on};
STATE currentState = off;

boolean button1BounceGuard = false;
long button1LastDebounceTime = 0;

boolean button2BounceGuard = false;
long button2LastDebounceTime = 0;

void setup(){
  pinMode(9, INPUT);  // button1 [Sensor]
  pinMode(8, INPUT);  // button2 [Sensor]
  pinMode(12, OUTPUT); // errorLed [Actuator]
  pinMode(11, OUTPUT); // greenLed [Actuator]
}

void loop() {
	switch(currentState){
		case error:
			for (int i = 0; i < 3; i++) {
				digitalWrite(12, HIGH);
				delay(100);
				digitalWrite(12, LOW);
				delay(100);
			}
			delay(3000);
			break;
		case off:
			digitalWrite(11,LOW);
			button1BounceGuard = millis() - button1LastDebounceTime > debounce;
			button2BounceGuard = millis() - button2LastDebounceTime > debounce;
			if( (digitalRead(9) == HIGH && button1BounceGuard) && (digitalRead(8) == HIGH && button2BounceGuard) ) {
				button1LastDebounceTime = millis();
				button2LastDebounceTime = millis();
				currentState = error;
			}
			button1BounceGuard = millis() - button1LastDebounceTime > debounce;
			button2BounceGuard = millis() - button2LastDebounceTime > debounce;
			if( (digitalRead(9) == HIGH && button1BounceGuard) || (digitalRead(8) == HIGH && button2BounceGuard) ) {
				button1LastDebounceTime = millis();
				button2LastDebounceTime = millis();
				currentState = on;
			}
			break;
		case on:
			digitalWrite(11,HIGH);
			button1BounceGuard = millis() - button1LastDebounceTime > debounce;
			button2BounceGuard = millis() - button2LastDebounceTime > debounce;
			if( (digitalRead(9) == HIGH && button1BounceGuard) && (digitalRead(8) == HIGH && button2BounceGuard) ) {
				button1LastDebounceTime = millis();
				button2LastDebounceTime = millis();
				currentState = error;
			}
			button1BounceGuard = millis() - button1LastDebounceTime > debounce;
			button2BounceGuard = millis() - button2LastDebounceTime > debounce;
			if( (digitalRead(9) == LOW && button1BounceGuard) || (digitalRead(8) == LOW && button2BounceGuard) ) {
				button1LastDebounceTime = millis();
				button2LastDebounceTime = millis();
				currentState = off;
			}
			break;
	}
}
