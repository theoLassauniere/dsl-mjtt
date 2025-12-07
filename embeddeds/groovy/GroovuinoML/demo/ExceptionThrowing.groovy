sensor "button1" onPin 9
sensor "button2" onPin 8

actuator "errorLed" pin 12
actuator "greenLed" pin 11

errorState "error", 3

state "off" means "greenLed" becomes "low"
state "on"  means "greenLed" becomes "high"

initial "off"

from "off" to "error" when "button1" becomes "high" and "button2" becomes "high"

from "off" to "on" when "button1" becomes "high" or "button2" becomes "high"

from "on" to "error" when "button1" becomes "high" and "button2" becomes "high"
from "on" to "off" when "button1" becomes "low" or "button2" becomes "low"

export "ExceptionThrowing"
