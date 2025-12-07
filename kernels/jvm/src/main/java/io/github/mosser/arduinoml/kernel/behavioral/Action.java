package io.github.mosser.arduinoml.kernel.behavioral;

import io.github.mosser.arduinoml.kernel.generator.Visitable;
import io.github.mosser.arduinoml.kernel.generator.Visitor;
import io.github.mosser.arduinoml.kernel.structural.Actuator;
import io.github.mosser.arduinoml.kernel.structural.SIGNAL;

public class Action implements Visitable {

	private SIGNAL value;
	private Actuator actuator;
    private long delay = 0;


	public SIGNAL getValue() {
		return value;
	}

	public void setValue(SIGNAL value) {
		this.value = value;
	}

	public Actuator getActuator() {
		return actuator;
	}

	public void setActuator(Actuator actuator) {
		this.actuator = actuator;
	}

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        if (delay < 0) {
            throw new IllegalArgumentException("Le délai ne peut pas être négatif.");
        }
        this.delay = delay;
    }

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
}
