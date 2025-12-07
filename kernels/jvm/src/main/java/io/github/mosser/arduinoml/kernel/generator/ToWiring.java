package io.github.mosser.arduinoml.kernel.generator;

import io.github.mosser.arduinoml.kernel.App;
import io.github.mosser.arduinoml.kernel.behavioral.*;
import io.github.mosser.arduinoml.kernel.structural.*;



/**
 * Quick and dirty visitor to support the generation of Wiring code
 */
public class ToWiring extends Visitor<StringBuffer> {
	enum PASS {ONE, TWO}

	public ToWiring() {
		this.result = new StringBuffer();
	}

	private void w(String s) {
		result.append(String.format("%s",s));
	}

	@Override
	public void visit(App app) {
		//first pass, create global vars
		context.put("pass", PASS.ONE);
		w("// Wiring code generated from an ArduinoML model\n");
		w(String.format("// Application name: %s\n", app.getName())+"\n");

		w("long debounce = 200;\n");
		w("\nenum STATE {");
		String sep ="";
		for(State state: app.getStates()){
			w(sep);
			state.accept(this);
			sep=", ";
		}
		w("};\n");
		if (app.getInitial() != null) {
			w("STATE currentState = " + app.getInitial().getName()+";\n");
		}

		for(Brick brick: app.getBricks()){
			brick.accept(this);
		}

		//second pass, setup and loop
		context.put("pass",PASS.TWO);
		w("\nvoid setup(){\n");
		for(Brick brick: app.getBricks()){
			brick.accept(this);
		}
		w("}\n");

		w("\nvoid loop() {\n" +
			"\tswitch(currentState){\n");
		for(State state: app.getStates()){
			state.accept(this);
		}
		w("\t}\n" +
			"}");
	}

	@Override
	public void visit(Actuator actuator) {
		if(context.get("pass") == PASS.ONE) {
			return;
		}
		if(context.get("pass") == PASS.TWO) {
			w(String.format("  pinMode(%d, OUTPUT); // %s [Actuator]\n", actuator.getPin(), actuator.getName()));
			return;
		}
	}


	@Override
	public void visit(Sensor sensor) {
		if(context.get("pass") == PASS.ONE) {
			w(String.format("\nboolean %sBounceGuard = false;\n", sensor.getName()));
			w(String.format("long %sLastDebounceTime = 0;\n", sensor.getName()));
			return;
		}
		if(context.get("pass") == PASS.TWO) {
			w(String.format("  pinMode(%d, INPUT);  // %s [Sensor]\n", sensor.getPin(), sensor.getName()));
			return;
		}
	}

    @Override
    public void visit(State state) {
        if (context.get("pass") == PASS.ONE) {
            w(state.getName());
            return;
        }
        if (context.get("pass") == PASS.TWO) {
            w("\t\tcase " + state.getName() + ":\n");
            for (Action action : state.getActions()) {
                action.accept(this);
            }

            for (Transition t : state.getTransitions()) {
                t.accept(this);
            }
            w("\t\t\tbreak;\n");
        }
    }


    @Override
    public void visit(SignalTransition transition) {
        if (context.get("pass") == PASS.ONE) {
            return;
        }
        if (context.get("pass") == PASS.TWO) {

            // 1) debounce pour tous les capteurs concernés
            for (Condition c : transition.getConditions()) {
                String sensorName = c.getSensor().getName();
                w(String.format("\t\t\t%sBounceGuard = millis() - %sLastDebounceTime > debounce;\n",
                        sensorName, sensorName));
            }

            // 2) construction de l'expression if avec AND / OR
            w("\t\t\tif( ");
            StringBuilder expr = new StringBuilder();
            boolean first = true;
            for (Condition c : transition.getConditions()) {
                String sensorName = c.getSensor().getName();
                String cond = String.format("(digitalRead(%d) == %s && %sBounceGuard)",
                        c.getSensor().getPin(), c.getValue(), sensorName);

                if (first) {
                    expr.append(cond);
                    first = false;
                } else {
                    if (c.isOrWithPrevious()) {
                        expr.append(" || ");
                    } else {
                        expr.append(" && ");
                    }
                    expr.append(cond);
                }
            }
            w(expr.toString());
            w(" ) {\n");

            // 3) mise à jour du debounce pour tous les capteurs
            for (Condition c : transition.getConditions()) {
                String sensorName = c.getSensor().getName();
                w(String.format("\t\t\t\t%sLastDebounceTime = millis();\n", sensorName));
            }

            // 4) changement d'état
            w("\t\t\t\tcurrentState = " + transition.getNext().getName() + ";\n");
            w("\t\t\t}\n");
        }
    }

	@Override
	public void visit(TimeTransition transition) {
		if(context.get("pass") == PASS.ONE) {
			return;
		}
		if(context.get("pass") == PASS.TWO) {
			int delayInMS = transition.getDelay();
			w(String.format("\t\t\tdelay(%d);\n", delayInMS));
			w("\t\t\t\tcurrentState = " + transition.getNext().getName() + ";\n");
			w("\t\t\t}\n");
			return;
		}
	}

	@Override
	public void visit(Action action) {
		if(context.get("pass") == PASS.ONE) {
			return;
		}
        if(context.get("pass") == PASS.TWO) {
            w(String.format("\t\t\tdigitalWrite(%d,%s);\n",action.getActuator().getPin(),action.getValue()));
            if (action.getDelay() > 0) {
                w(String.format("\t\t\tdelay(%d);\n", action.getDelay()));
            }
        }
	}

    @Override
    public void visit(ErrorState errorState) {
        if (context.get("pass") == PASS.ONE) {
            return;
        }
        if (context.get("pass") == PASS.TWO) {
            w("\t\tcase " + errorState.getName() + ":\n");
            w(String.format("\t\t\tfor (int i = 0; i < %d; i++) {\n", errorState.getTimes()));
            w(String.format("\t\t\t\tdigitalWrite(%d, HIGH);\n", errorState.getActuator().getPin()));
            w(String.format("\t\t\t\tdelay(%d);\n", errorState.getDelay()));
            w(String.format("\t\t\t\tdigitalWrite(%d, LOW);\n", errorState.getActuator().getPin()));
            w(String.format("\t\t\t\tdelay(%d);\n", errorState.getDelay()));
            w("\t\t\t}\n");
            w("\t\t\tdelay(3000);\n");
            w("\t\t\tbreak;\n");
        }

    }

}
