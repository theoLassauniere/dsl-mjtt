package main.groovy.groovuinoml.dsl

import io.github.mosser.arduinoml.kernel.behavioral.TimeUnit
import io.github.mosser.arduinoml.kernel.behavioral.Action
import io.github.mosser.arduinoml.kernel.behavioral.State
import io.github.mosser.arduinoml.kernel.structural.Actuator
import io.github.mosser.arduinoml.kernel.structural.Sensor
import io.github.mosser.arduinoml.kernel.structural.SIGNAL

abstract class GroovuinoMLBasescript extends Script {
//	public static Number getDuration(Number number, TimeUnit unit) throws IOException {
//		return number * unit.inMillis;
//	}

	// sensor "name" pin n
	def sensor(String name) {
		[pin: { n -> ((GroovuinoMLBinding)this.getBinding()).getGroovuinoMLModel().createSensor(name, n) },
		onPin: { n -> ((GroovuinoMLBinding)this.getBinding()).getGroovuinoMLModel().createSensor(name, n)}]
	}
	
	// actuator "name" pin n
	def actuator(String name) {
		[pin: { n -> ((GroovuinoMLBinding)this.getBinding()).getGroovuinoMLModel().createActuator(name, n) }]
	}

	// state "name" means actuator becomes signal [and actuator becomes signal]*n
	def state(String name) {
		List<Action> actions = new ArrayList<Action>()
		((GroovuinoMLBinding) this.getBinding()).getGroovuinoMLModel().createState(name, actions)
		// recursive closure to allow multiple and statements
		def closure
		closure = { actuator ->
			[becomes: { signal ->
				Action action = new Action()
				action.setActuator(actuator instanceof String ? (Actuator)((GroovuinoMLBinding)this.getBinding()).getVariable(actuator) : (Actuator)actuator)
				action.setValue(signal instanceof String ? (SIGNAL)((GroovuinoMLBinding)this.getBinding()).getVariable(signal) : (SIGNAL)signal)
				actions.add(action)
				[and: closure]
			}]
		}
		[means: closure]
	}

	// initial state
	def initial(state) {
		((GroovuinoMLBinding) this.getBinding()).getGroovuinoMLModel().setInitialState(state instanceof String ? (State)((GroovuinoMLBinding)this.getBinding()).getVariable(state) : (State)state)
	}
	
	// from state1 to state2 when sensor becomes signal
    def from(state1) {
        [to: { state2 ->
            def model = ((GroovuinoMLBinding) this.getBinding()).getGroovuinoMLModel()
            def s1 = state1 instanceof String
                    ? (State) ((GroovuinoMLBinding) this.getBinding()).getVariable(state1)
                    : (State) state1
            def s2 = state2 instanceof String
                    ? (State) ((GroovuinoMLBinding) this.getBinding()).getVariable(state2)
                    : (State) state2

            [
                    when : { sensor ->
                        [becomes: { signal ->
                            // création de la transition par capteur
                            def sen = sensor instanceof String
                                    ? (Sensor) ((GroovuinoMLBinding) this.getBinding()).getVariable(sensor)
                                    : (Sensor) sensor
                            def sig = signal instanceof String
                                    ? (SIGNAL) ((GroovuinoMLBinding) this.getBinding()).getVariable(signal)
                                    : (SIGNAL) signal

                            model.createTransition(s1, s2, sen, sig)

                            // permet d'enchaîner ... then { ... }
                            [
                                    then: { actionsClosure ->
                                        List<Action> actions = s2.getActions()
                                        if (actions == null) {
                                            actions = new ArrayList<Action>()
                                            s2.setActions(actions)
                                        }

                                        // même logique que pour `state "name" means ...`
                                        def closure
                                        closure = { act ->
                                            [becomes: { sig2 ->
                                                Action a = new Action()
                                                a.setActuator(
                                                        act instanceof String
                                                                ? (Actuator) ((GroovuinoMLBinding) this.getBinding()).getVariable(act)
                                                                : (Actuator) act
                                                )
                                                a.setValue(
                                                        sig2 instanceof String
                                                                ? (SIGNAL) ((GroovuinoMLBinding) this.getBinding()).getVariable(sig2)
                                                                : (SIGNAL) sig2
                                                )
                                                actions.add(a)
                                                [and: closure]
                                            }]
                                        }

                                        // exécuter le bloc utilisateur: led becomes low and buzzer becomes low
                                        actionsClosure.delegate = [means: closure]
                                        actionsClosure.resolveStrategy = Closure.DELEGATE_FIRST
                                        actionsClosure.call()
                                    }
                            ]
                        }]
                    },
                    after: { delay ->
                        model.createTransition(s1, s2, delay)
                    }
            ]
        }]
    }


    // export name
	def export(String name) {
		println(((GroovuinoMLBinding) this.getBinding()).getGroovuinoMLModel().generateCode(name).toString())
	}
	
	// disable run method while running
	int count = 0
	abstract void scriptBody()
	def run() {
		if(count == 0) {
			count++
			scriptBody()
		} else {
			println "Run method is disabled"
		}
	}
}
