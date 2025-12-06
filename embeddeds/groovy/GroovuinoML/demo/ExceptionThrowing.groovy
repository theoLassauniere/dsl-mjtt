sensor "button1" onPin 9
sensor "button2" onPin 8

actuator "errorLed" pin 12
actuator "greenLed" pin 11

errorState "error", 3

state "off" means "greenLed" becomes "low"
state "on"  means "greenLed" becomes "high"

initial "off"

// transitions depuis off
// priorité : si les deux boutons sont appuyés en même temps -> erreur (blocage)
from "off" to "error" when "button1" becomes "high" and "button2" becomes "high"

// utilisation exclusive : un seul bouton à la fois allume l'état ON (OR)
from "off" to "on" when "button1" becomes "high" or "button2" becomes "high"

// transitions depuis on
from "on" to "error" when "button1" becomes "high" and "button2" becomes "high"
// revenir à off quand un des boutons est relâché (OR)
from "on" to "off" when "button1" becomes "low" or "button2" becomes "low"

export "ExceptionThrowing"
