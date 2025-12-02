
//Wiring code generated from an ArduinoML model
// Application name: RedButton

long debounce = 200;
enum STATE {off, state1, state2};

STATE currentState = off;

bool buttonBounceGuard = false;
long buttonLastDebounceTime = 0;

            

	void setup(){
		pinMode(12, OUTPUT); // red_led [Actuator]
		pinMode(11, OUTPUT); // buzzer [Actuator]
		pinMode(8, INPUT); // button [Sensor]
	}
	void loop() {
			switch(currentState){

				case off:
					digitalWrite(12,LOW);
					digitalWrite(11,LOW);
		 			buttonBounceGuard = millis() - buttonLastDebounceTime > debounce;
					if( digitalRead(8) == HIGH && buttonBounceGuard) {
						buttonLastDebounceTime = millis();
						currentState = state1;
					}
		
				break;
				case state1:
					digitalWrite(12,LOW);
					digitalWrite(11,HIGH);
		 			buttonBounceGuard = millis() - buttonLastDebounceTime > debounce;
					if( digitalRead(8) == HIGH && buttonBounceGuard) {
						buttonLastDebounceTime = millis();
						currentState = state2;
					}
		
				break;
				case state2:
					digitalWrite(12,HIGH);
					digitalWrite(11,LOW);
		 			buttonBounceGuard = millis() - buttonLastDebounceTime > debounce;
					if( digitalRead(8) == HIGH && buttonBounceGuard) {
						buttonLastDebounceTime = millis();
						currentState = off;
					}
		
				break;
		}
	}
	
