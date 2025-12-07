
//Wiring code generated from an ArduinoML model
// Application name: DualCheck

long debounce = 200;
enum STATE {off, on};

STATE currentState = off;

bool button1BounceGuard = false;
long button1LastDebounceTime = 0;

            

bool button2BounceGuard = false;
long button2LastDebounceTime = 0;

            

bool button3BounceGuard = false;
long button3LastDebounceTime = 0;

            

bool button4BounceGuard = false;
long button4LastDebounceTime = 0;

            

bool button5BounceGuard = false;
long button5LastDebounceTime = 0;

            

	void setup(){
		pinMode(12, OUTPUT); // red_led [Actuator]
		pinMode(9, INPUT); // button1 [Sensor]
		pinMode(8, INPUT); // button2 [Sensor]
		pinMode(7, INPUT); // button3 [Sensor]
		pinMode(6, INPUT); // button4 [Sensor]
		pinMode(5, INPUT); // button5 [Sensor]
	}
	void loop() {
			switch(currentState){

				case off:
                digitalWrite(12,LOW);
        
                if (
            digitalRead(button1) == HIGH && button1BounceGuard
         && 
            digitalRead(button2) == HIGH && button2BounceGuard
         && 
            digitalRead(button3) == HIGH && button3BounceGuard
         && 
            digitalRead(button4) == HIGH && button4BounceGuard
         || 
            digitalRead(button5) == HIGH && button5BounceGuard
        ) {
                    currentState = on;
                }
            
				break;
				case on:
                digitalWrite(12,HIGH);
        
                if (
            digitalRead(button1) == LOW && button1BounceGuard
         || 
            digitalRead(button2) == LOW && button2BounceGuard
         || 
            digitalRead(button3) == LOW && button3BounceGuard
        ) {
                    currentState = off;
                }
            
				break;
		}
	}
	
