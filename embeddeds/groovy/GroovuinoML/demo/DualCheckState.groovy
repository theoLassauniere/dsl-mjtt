sensor "button1" onPin 8
sensor "button2" onPin 9

actuator "led" pin 12

state "on" means "led" becomes "high"
state "off" means "led" becomes "low"

initial "off"

from "on" to "off" when "button1" becomes "high" and "button2" becomes "high"
from "off" to "on" when "button1" becomes "low" or "button2" becomes "low"

export "Switch!"