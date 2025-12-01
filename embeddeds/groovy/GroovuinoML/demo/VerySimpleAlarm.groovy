sensor "button" onPin 8
actuator "led" pin 12
actuator "buzzer" pin 11

state "off" means "led" becomes "low" and "buzzer" becomes "low"
state "on" means "led" becomes "high" and "buzzer" becomes "high"

initial "off"

from "off" to "on" when "button" becomes "high"
from "on" to "off" when "button" becomes "low"

export "verySimpleAlarm!"
