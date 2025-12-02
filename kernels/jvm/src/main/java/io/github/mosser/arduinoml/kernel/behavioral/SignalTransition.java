package io.github.mosser.arduinoml.kernel.behavioral;

import io.github.mosser.arduinoml.kernel.generator.Visitor;
import io.github.mosser.arduinoml.kernel.structural.Condition;
import io.github.mosser.arduinoml.kernel.structural.SIGNAL;
import io.github.mosser.arduinoml.kernel.structural.Sensor;

import java.util.ArrayList;
import java.util.List;

public class SignalTransition extends Transition {


    private List<Condition> conditions = new ArrayList<Condition>();

    public List<Condition> getConditions() {
        return conditions;
    }

    public void addCondition(Sensor sensor, SIGNAL value) {
        conditions.add(new Condition(sensor, value,false));
    }

    public void addCondition(Sensor sensor, SIGNAL value, boolean orWithPrevious) {
        conditions.add(new Condition(sensor, value,orWithPrevious));
    }


    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
