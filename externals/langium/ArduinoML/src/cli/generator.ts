import fs from 'fs';
import { CompositeGeneratorNode, NL, toString } from 'langium';
import path from 'path';
import {
    Action,
    Actuator, AndExpression,
    App,
    Condition,
    Expression, OrExpression,
    Sensor,
    State,
    Transition
} from '../language-server/generated/ast';
import { extractDestinationAndName } from './cli-util';

export function generateInoFile(app: App, filePath: string, destination: string | undefined): string {
    const data = extractDestinationAndName(filePath, destination);
    const generatedFilePath = `${path.join(data.destination, data.name)}.ino`;

    const fileNode = new CompositeGeneratorNode();
    compile(app,fileNode)


    if (!fs.existsSync(data.destination)) {
        fs.mkdirSync(data.destination, { recursive: true });
    }
    fs.writeFileSync(generatedFilePath, toString(fileNode));
    return generatedFilePath;
}


function compile(app:App, fileNode:CompositeGeneratorNode){
    fileNode.append(
	`
//Wiring code generated from an ArduinoML model
// Application name: `+app.name+`

long debounce = 200;
enum STATE {
    `+app.states.map(s => isErrorState(s) ? `error${s.code}` : s.name).join(', ')+`
};

STATE currentState = ` + (
    isErrorState(app.initial.ref!)
    ? `error${(app.initial.ref as any).code}`
    : app.initial.ref?.name)+`;`
    ,NL);

    for(const brick of app.bricks){
        if ("inputPin" in brick){
            fileNode.append(`
bool `+brick.name+`BounceGuard = false;
long `+brick.name+`LastDebounceTime = 0;

            `,NL);
        }
    }
    fileNode.append(`
	void setup(){`);
    for(const brick of app.bricks){
        if ("inputPin" in brick){
       		compileSensor(brick,fileNode);
		}else{
            compileActuator(brick,fileNode);
        }
	}


    fileNode.append(`
	}
	void loop() {
			switch(currentState){`,NL)
			for(const state of app.states){
				compileState(state, fileNode)
            }
	fileNode.append(`
		}
	}
	`,NL);
    }

	function isErrorState(state: State): state is any {
		return "code" in state;
	}

	function compileActuator(actuator: Actuator, fileNode: CompositeGeneratorNode) {
        fileNode.append(`
		pinMode(`+actuator.outputPin+`, OUTPUT); // `+actuator.name+` [Actuator]`)
    }

	function compileSensor(sensor:Sensor, fileNode: CompositeGeneratorNode) {
    	fileNode.append(`
		pinMode(`+sensor.inputPin+`, INPUT); // `+sensor.name+` [Sensor]`)
	}

    function compileState(state: State, fileNode: CompositeGeneratorNode) {
		const stateName = isErrorState(state)
			? `error${state.code}`
			: state.name;

		fileNode.append(`
					case ${stateName}:`)

		if (isErrorState(state)) {
			const code = state.code;
			fileNode.append(`
				while(true) {
					// répéter 'code' fois HIGH/LOW
					for(int i = 0; i < ${code}; i++) {
						digitalWrite(12, HIGH);
						delay(500);
						digitalWrite(12, LOW);
						delay(500);
					}
					delay(${code} * 500);
				}`)
			fileNode.append(`
				break;`)
			return;
		}

		for (const action of state.actions) {
			compileAction(action, fileNode);
		}

		if (state.expression !== null){
			compileExpression(state.expression, fileNode);
		}

		fileNode.append(`
					break;`)
	}


    function compileAction(action: Action, fileNode:CompositeGeneratorNode) {
        fileNode.append(`
                digitalWrite(`+action.actuator.ref?.outputPin+`,`+action.value.value+`);
        `);
    }

    function compileExpression(expression: Expression, fileNode: CompositeGeneratorNode) {
        const conditionCode = generateCondition(expression.condition);

        fileNode.append(`
                if (${conditionCode}) {
                    ${generateTransitionCode(expression.transition)}
                }
            `);
    }

    function generateTransitionCode(transition: Transition) {
        return `
            currentState = `+transition.next.ref?.name+`;
        `;
    }

    function generateCondition(expr: Expression): string {
        if (expr.$type === 'Condition') {
            return compileCondition(expr as Condition);
        }
        if (expr.$type === 'AndExpression') {
            return generateAndCondition(expr as AndExpression);
        }
        if (expr.$type === 'OrExpression') {
            return generateOrCondition(expr as OrExpression);
        }
        return "";
    }

    function compileCondition(condition: Condition) {
        return `
            digitalRead(`+condition.sensor.ref?.name+`) == `+condition.value.value+` && `+condition.sensor.ref?.name+`BounceGuard
        `;
    }

    function generateAndCondition(expr: AndExpression): string {
        return `${generateCondition(expr.left)} && ${generateCondition(expr.right)}`;
    }

    function generateOrCondition(expr: OrExpression): string {
        return `${generateCondition(expr.left)} || ${generateCondition(expr.right)}`;
    }

