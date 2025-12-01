package main.groovy.groovuinoml.dsl

import io.github.mosser.arduinoml.kernel.behavioral.TimeUnit
import io.github.mosser.arduinoml.kernel.behavioral.Action
import io.github.mosser.arduinoml.kernel.behavioral.State
import io.github.mosser.arduinoml.kernel.behavioral.SignalTransition
import io.github.mosser.arduinoml.kernel.structural.Actuator
import io.github.mosser.arduinoml.kernel.structural.Sensor
import io.github.mosser.arduinoml.kernel.structural.SIGNAL

abstract class GroovuinoMLBasescript extends Script {
//	public static Number getDuration(Number number, TimeUnit unit) throws IOException {
//		return number * unit.inMillis;
//	}

    // sensor "name" pin n
    def sensor(String name) {
        [
                pin  : { n -> ((GroovuinoMLBinding) this.getBinding()).getGroovuinoMLModel().createSensor(name, n) },
                onPin: { n -> ((GroovuinoMLBinding) this.getBinding()).getGroovuinoMLModel().createSensor(name, n) }
        ]
    }

    // actuator "name" pin n
    def actuator(String name) {
        [
                pin: { n -> ((GroovuinoMLBinding) this.getBinding()).getGroovuinoMLModel().createActuator(name, n) }
        ]
    }

    // state "name" means actuator becomes signal [and actuator becomes signal]\*n
    def state(String name) {
        List<Action> actions = new ArrayList<Action>()
        ((GroovuinoMLBinding) this.getBinding())
                .getGroovuinoMLModel()
                .createState(name, actions)

        // recursive closure to allow multiple and statements
        def closure
        closure = { actuator ->
            [
                    becomes: { signal ->
                        Action action = new Action()
                        action.setActuator(
                                actuator instanceof String
                                        ? (Actuator) ((GroovuinoMLBinding) this.getBinding()).getVariable(actuator)
                                        : (Actuator) actuator
                        )
                        action.setValue(
                                signal instanceof String
                                        ? (SIGNAL) ((GroovuinoMLBinding) this.getBinding()).getVariable(signal)
                                        : (SIGNAL) signal
                        )
                        actions.add(action)
                        [and: closure]
                    }
            ]
        }[means: closure]
	}

    def errorState(String name, int times, String actuatorName = "errorLed") {
        List<Action> actions = new ArrayList<Action>()

        // récupère l'actuator par son nom depuis le binding
        Actuator actuator = (Actuator) ((GroovuinoMLBinding) this.getBinding()).getVariable(actuatorName)

        // on ajoute 2 * times actions: HIGH puis LOW pour chaque blink
        times.times {
            Action on = new Action()
            on.setActuator(actuator)
            on.setValue(SIGNAL.HIGH)
            actions.add(on)

            Action off = new Action()
            off.setActuator(actuator)
            off.setValue(SIGNAL.LOW)
            actions.add(off)
        }

        ((GroovuinoMLBinding) this.getBinding()).getGroovuinoMLModel().createState(name, actions)
    }

	// initial state
	def initial(state) {
		((GroovuinoMLBinding) this.getBinding()).getGroovuinoMLModel().setInitialState(state instanceof String ? (State)((GroovuinoMLBinding)this.getBinding()).getVariable(state) : (State)state
                )
    }

    // from state1 to state2 when s1 becomes v1 and/or s2 becomes v2 ...
    def from(state1) {
        [
                to: { state2 ->
                    def model = ((GroovuinoMLBinding) this.getBinding()).getGroovuinoMLModel()

                    State fromState = state1 instanceof String
                            ? (State) ((GroovuinoMLBinding) this.getBinding()).getVariable(state1)
                            : (State) state1

                    State toState = state2 instanceof String
                            ? (State) ((GroovuinoMLBinding) this.getBinding()).getVariable(state2)
                            : (State) state2

                    SignalTransition transitionRef = null

                    def resolveSensor = { s ->
                        s instanceof String
                                ? (Sensor) ((GroovuinoMLBinding) this.getBinding()).getVariable(s)
                                : (Sensor) s
                    }
                    def resolveSignal = { v ->
                        v instanceof String
                                ? (SIGNAL) ((GroovuinoMLBinding) this.getBinding()).getVariable(v)
                                : (SIGNAL) v
                    }

                    // fonction interne pour ajouter une condition AND ou OR
                    def doAddCondition = { sensor, signal, boolean isOr ->
                        Sensor s = resolveSensor(sensor)
                        SIGNAL v = resolveSignal(signal)

                        if (transitionRef == null) {
                            transitionRef = model.createSignalTransition(fromState, toState, s, v)
                        } else {
                            transitionRef.addCondition(s, v, isOr)
                        }
                    }

                    // on déclare d'abord les closures and / or,
                    // puis on les utilise dans addCondition
                    def andClosure
                    def orClosure

                    andClosure = { sensor ->
                        [
                                becomes: { signal ->
                                    doAddCondition(sensor, signal, false)
                                    [
                                            and: andClosure,
                                            or : orClosure
                                    ]
                                }
                        ]
                    }

                    orClosure = { sensor ->
                        [
                                becomes: { signal ->
                                    doAddCondition(sensor, signal, true)
                                    [
                                            and: andClosure,
                                            or : orClosure
                                    ]
                                }
                        ]
                    }

                    // point d'entrée pour le DSL: when ...
                    def addCondition = andClosure

                    [
                            when : addCondition,
                            after: { delay ->
                                model.createTimeTransition(fromState, toState, delay)
                            }
                    ]
                }
        ]
    }


    // export name
    def export(String name) {
        println(((GroovuinoMLBinding) this.getBinding()).getGroovuinoMLModel().generateCode(name).toString())
    }

    // disable run method while running
    int count = 0
    abstract void scriptBody()

    def run() {
        if (count == 0) {
            count++
            scriptBody()
        } else {
            println "Run method is disabled"
        }
    }
}
