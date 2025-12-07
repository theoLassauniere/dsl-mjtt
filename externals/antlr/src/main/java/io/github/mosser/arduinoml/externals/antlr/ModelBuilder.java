package io.github.mosser.arduinoml.externals.antlr;

import io.github.mosser.arduinoml.externals.antlr.grammar.*;


import io.github.mosser.arduinoml.kernel.App;
import io.github.mosser.arduinoml.kernel.behavioral.Action;
import io.github.mosser.arduinoml.kernel.behavioral.SignalTransition;
import io.github.mosser.arduinoml.kernel.behavioral.State;
import io.github.mosser.arduinoml.kernel.structural.Actuator;
import io.github.mosser.arduinoml.kernel.structural.SIGNAL;
import io.github.mosser.arduinoml.kernel.structural.Sensor;

import java.util.HashMap;
import java.util.Map;

public class ModelBuilder extends ArduinomlBaseListener {

    /********************
     ** Business Logic **
     ********************/

    private App theApp = null;
    private boolean built = false;

    public App retrieve() {
        if (built) { return theApp; }
        throw new RuntimeException("Cannot retrieve a model that was not created!");
    }

    /*******************
     ** Symbol tables **
     *******************/

    private Map<String, Sensor>   sensors   = new HashMap<>();
    private Map<String, Actuator> actuators = new HashMap<>();
    private Map<String, State>    states  = new HashMap<>();
    private Map<String, TransitionBinding>  bindings  = new HashMap<>();

    private class TransitionBinding {
        String to;
        SIGNAL signal;
        Sensor sensor;
    }

    private State currentState = null;

    /**************************
     ** Listening mechanisms **
     **************************/

    @Override
    public void enterRoot(ArduinomlParser.RootContext ctx) {
        built = false;
        theApp = new App();
    }

    @Override
    public void exitRoot(ArduinomlParser.RootContext ctx) {
        // Resolving states in transitions
        bindings.forEach((stateKey, binding) -> {
            State state = states.get(stateKey);
            SignalTransition transition = new SignalTransition();
            if (binding.sensor != null && binding.signal != null) {
                transition.setSensor(binding.sensor);
                transition.setValue(binding.signal);
            }
            transition.setNext(states.get(binding.to));
            state.setTransition(transition);
        });
        this.built = true;
    }

    @Override
    public void enterDeclaration(ArduinomlParser.DeclarationContext ctx) {
        theApp.setName(ctx.name.getText());
    }

    @Override
    public void enterSensor(ArduinomlParser.SensorContext ctx) {
        Sensor sensor = new Sensor();
        sensor.setName(ctx.location().id.getText());
        sensor.setPin(Integer.parseInt(ctx.location().port.getText()));
        this.theApp.getBricks().add(sensor);
        sensors.put(sensor.getName(), sensor);
    }

    @Override
    public void enterActuator(ArduinomlParser.ActuatorContext ctx) {
        Actuator actuator = new Actuator();
        actuator.setName(ctx.location().id.getText());
        actuator.setPin(Integer.parseInt(ctx.location().port.getText()));
        this.theApp.getBricks().add(actuator);
        actuators.put(actuator.getName(), actuator);
    }

    @Override
    public void enterState(ArduinomlParser.StateContext ctx) {
        State local = new State();
        local.setName(ctx.name.getText());
        this.currentState = local;
        this.states.put(local.getName(), local);
    }

    @Override
    public void exitState(ArduinomlParser.StateContext ctx) {
        this.theApp.getStates().add(this.currentState);
        this.currentState = null;
    }

    @Override
    public void enterAction(ArduinomlParser.ActionContext ctx) {
        Action action = new Action();
        action.setActuator(actuators.get(ctx.receiver.getText()));
        action.setValue(SIGNAL.valueOf(ctx.value.getText()));
        currentState.getActions().add(action);
    }

    @Override
    public void exitTransition(ArduinomlParser.TransitionContext ctx) {
        TransitionBinding binding = new TransitionBinding();
        binding.to = ctx.next.getText();
        if (ctx.expr() != null) {
            ArduinomlParser.AtomContext atom = extractSingleAtom(ctx.expr());
            if (atom != null) {
                String sensorName = atom.IDENTIFIER().getText();
                String signalValue = atom.SIGNAL().getText();
                binding.sensor = sensors.get(sensorName);
                binding.signal = SIGNAL.valueOf(signalValue);
            }
        }
        bindings.put(currentState.getName(), binding);
    }

    @Override
    public void enterInitial(ArduinomlParser.InitialContext ctx) {
        this.theApp.setInitial(this.currentState);
    }

    private ArduinomlParser.AtomContext extractSingleAtom(ArduinomlParser.ExprContext expr) {
        if (expr == null) return null;
        ArduinomlParser.OrExprContext orExpr = expr.orExpr();
        if (orExpr == null || orExpr.andExpr().isEmpty()) return null;
        
        ArduinomlParser.AndExprContext andExpr = orExpr.andExpr(0);
        if (andExpr == null || andExpr.atom().isEmpty()) return null;
        
        return andExpr.atom(0);
    }
}
