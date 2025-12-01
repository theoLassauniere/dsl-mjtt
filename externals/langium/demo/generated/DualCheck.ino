
//Wiring code generated from an ArduinoML model
// Application name: DualCheck

long debounce = 200;
enum STATE {off, on};

STATE currentState = off;

bool button1BounceGuard = false;
long button1LastDebounceTime = 0;

            

bool button2BounceGuard = false;
long button2LastDebounceTime = 0;

            

	void setup(){
		pinMode(12, OUTPUT); // red_led [Actuator]
		pinMode(9, INPUT); // button1 [Sensor]
		pinMode(8, INPUT); // button2 [Sensor]
	}
	void loop() {
			switch(currentState){

				case off:
					digitalWrite(12,LOW);
		 			button1BounceGuard = millis() - button1LastDebounceTime > debounce;
        
            if( (digitalRead(9) == HIGH && button1BounceGuard) && (digitalRead(9) == HIGH && button1BounceGuard)  {
                button1LastDebounceTime = millis();
                currentState = on;
        
				break;
				case on:
					digitalWrite(12,HIGH);
		 			button1BounceGuard = millis() - button1LastDebounceTime > debounce;
        
            if( (digitalRead(9) == LOW && button1BounceGuard) || (digitalRead(9) == LOW && button1BounceGuard)  {
                button1LastDebounceTime = millis();
                currentState = off;
        
				break;
		}
	}
	
