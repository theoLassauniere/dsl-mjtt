
sensor "button1" onPin 9

sensor "button2" onPin 8

actuator "errorLed" pin 12

errorState "error3", 3

state "off" means "errorLed" becomes "low"

state "on" means "errorLed" becomes "high"

initial "off"

from "off" to "error3" when "button1" becomes "high" and "button2" becomes "high"
from "off" to "on"     when "button1" becomes "high"
from "off" to "on"     when "button2" becomes "high"

from "on" to "error3" when "button1" becomes "high" and "button2" becomes "high"
from "on" to "off"    when "button1" becomes "low"  and "button2" becomes "high"
from "on" to "off"    when "button2" becomes "low"  and "button1" becomes "high"

export "ExceptionThrowing"
