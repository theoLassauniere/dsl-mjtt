package io.github.mosser.arduinoml.kernel.behavioral;

import io.github.mosser.arduinoml.kernel.generator.Visitable;
import io.github.mosser.arduinoml.kernel.generator.Visitor;
import io.github.mosser.arduinoml.kernel.structural.*;

import java.util.ArrayList;
import java.util.List;

public abstract class Transition implements Visitable {

	protected State next;

    private List<Action> actions = new ArrayList<>();

    public State getNext() {
		return next;
	}

	public void setNext(State next) {
		this.next = next;
	}


    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    public void addAction(Action action) {
        this.actions.add(action);
    }

	@Override
	public abstract void accept(Visitor visitor);
}
