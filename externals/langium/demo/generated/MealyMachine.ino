
//Wiring code generated from an ArduinoML model
// Application name: MealySimple

long debounce = 200;
enum STATE {off, on};

STATE currentState = off;

bool buttonBounceGuard = false;
long buttonLastDebounceTime = 0;

            

	void setup(){
		pinMode(10, OUTPUT); // led1 [Actuator]
		pinMode(11, OUTPUT); // led2 [Actuator]
		pinMode(8, INPUT); // button [Sensor]
	}
	void loop() {
			switch(currentState){

				case off:
                digitalWrite(10,LOW);
        
                digitalWrite(11,LOW);
        
                if (
            digitalRead(button) == HIGH && buttonBounceGuard
        ) {
                    
                digitalWrite(10,HIGH);
        
                    
                digitalWrite(11,LOW);
        
                    currentState = on;
                }
            
				break;
				case on:
                digitalWrite(10,LOW);
        
                digitalWrite(11,LOW);
        
                if (
            digitalRead(button) == LOW && buttonBounceGuard
        ) {
                    
                digitalWrite(10,LOW);
        
                    
                digitalWrite(11,HIGH);
        
                    currentState = off;
                }
            
				break;
		}
	}
	
