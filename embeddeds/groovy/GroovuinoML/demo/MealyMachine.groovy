

actuator "led1" pin 10
actuator "led2" pin 11
sensor "button" onPin 8

state "off" means "led1" becomes "low" and "led2" becomes "low"
state "on" means "led1" becomes "low" and "led2" becomes "low"
initial "off"

from "off" to "on" when "button" becomes "high" then "led1" becomes "high" and "led2" becomes "low"
from "on" to "off" when "button" becomes "low" then "led1" becomes "low" and "led2" becomes "high"

export "Melymelo!"

