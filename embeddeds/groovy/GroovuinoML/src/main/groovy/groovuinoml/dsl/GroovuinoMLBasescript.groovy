package main.groovy.groovuinoml.dsl

import io.github.mosser.arduinoml.kernel.behavioral.ErrorState
import io.github.mosser.arduinoml.kernel.behavioral.TimeUnit
import io.github.mosser.arduinoml.kernel.behavioral.Action
import io.github.mosser.arduinoml.kernel.behavioral.State
import io.github.mosser.arduinoml.kernel.behavioral.SignalTransition
import io.github.mosser.arduinoml.kernel.structural.Actuator
import io.github.mosser.arduinoml.kernel.structural.Sensor
import io.github.mosser.arduinoml.kernel.structural.SIGNAL

abstract class GroovuinoMLBasescript extends Script {

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

    // state "name" means actuator becomes signal [and actuator becomes signal]*n
    def state(String name) {
        List<Action> actions = new ArrayList<Action>()
        ((GroovuinoMLBinding) this.getBinding()).getGroovuinoMLModel().createState(name, actions)

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
        }

        return [means: closure]
    }

    def errorState(String name, int times, String actuatorName = "errorLed") {
        // récupère l'actuator par son nom depuis le binding
        Actuator actuator = (Actuator) ((GroovuinoMLBinding) this.getBinding()).getVariable(actuatorName)

        ErrorState errorState = new ErrorState()
        errorState.setName(name)
        errorState.setActuator(actuator)
        errorState.setTimes(times)
        errorState.setDelay(100) // délai de 100ms

        // Ajoute l'état au modèle et au binding
        ((GroovuinoMLBinding) this.getBinding()).getGroovuinoMLModel().addState(errorState)
    }


    // initial state
    def initial(state) {
        ((GroovuinoMLBinding) this.getBinding()).getGroovuinoMLModel().setInitialState(
            state instanceof String ? (State) ((GroovuinoMLBinding) this.getBinding()).getVariable(state) : (State) state
        )
    }

    def from(state1) {
        [
                to: { state2 ->
                    def model = ((GroovuinoMLBinding) this.getBinding()).getGroovuinoMLModel()

                    // Résolution des états
                    State fromState = state1 instanceof String ? (State) binding.getVariable(state1) : (State) state1
                    State toState = state2 instanceof String ? (State) binding.getVariable(state2) : (State) state2

                    // Closure pour ajouter des actions à une transition
                    def addActionsToTransition = { transition ->
                        def actionClosure
                        actionClosure = { actuator ->
                            [
                                    becomes: { signalValue ->
                                        def act = actuator instanceof String ? (Actuator) binding.getVariable(actuator) : (Actuator) actuator
                                        def sig = signalValue instanceof String ? (SIGNAL) binding.getVariable(signalValue) : (SIGNAL) signalValue
                                        model.addActionToTransition(transition, act, sig)
                                        [and: actionClosure]
                                    }
                            ]
                        }
                        return [then: actionClosure]
                    }

                    // Logique pour les transitions basées sur les signaux (when)
                    SignalTransition signalTransitionRef = null
                    def resolveSensor = { s -> s instanceof String ? (Sensor) binding.getVariable(s) : (Sensor) s }
                    def resolveSignal = { v -> v instanceof String ? (SIGNAL) binding.getVariable(v) : (SIGNAL) v }

                    def doAddCondition = { sensor, signal, boolean isOr ->
                        if (signalTransitionRef == null) {
                            signalTransitionRef = model.createSignalTransition(fromState, toState, resolveSensor(sensor), resolveSignal(signal))
                        } else {
                            signalTransitionRef.addCondition(resolveSensor(sensor), resolveSignal(signal), isOr)
                        }
                    }

                    def andClosure, orClosure
                    andClosure = { sensor ->
                        [
                                becomes: { signal ->
                                    doAddCondition(sensor, signal, false)
                                    def result = [and: andClosure, or: orClosure]
                                    result.putAll(addActionsToTransition(signalTransitionRef))
                                    result
                                }
                        ]
                    }
                    orClosure = { sensor ->
                        [
                                becomes: { signal ->
                                    doAddCondition(sensor, signal, true)
                                    def result = [and: andClosure, or: orClosure]
                                    result.putAll(addActionsToTransition(signalTransitionRef))
                                    result
                                }
                        ]
                    }

                    // Logique pour les transitions temporelles (after)
                    def afterClosure = { delay ->
                        def timeTransition = model.createTimeTransition(fromState, toState, delay)
                        addActionsToTransition(timeTransition)
                    }

                    // Point d'entrée du DSL pour la transition
                    [
                            when : andClosure,
                            after: afterClosure
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
