sensor "button" onPin 8
actuator "led" pin 12
actuator "buzzer" pin 11

state "off" means "led" becomes "low" and "buzzer" becomes "low"
state "state1" means led becomes low and "buzzer" becomes high
state "state2" means led becomes high and "buzzer" becomes low

initial "off"

from "off" to "state1" when "button" becomes "high"
from "state1" to "state2" when "button" becomes "high"
from "state2" to "off" when "button" becomes "high"

export "multistate!"
