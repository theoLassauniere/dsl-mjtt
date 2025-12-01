"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.generateInoFile = void 0;
const fs_1 = __importDefault(require("fs"));
const langium_1 = require("langium");
const path_1 = __importDefault(require("path"));
const cli_util_1 = require("./cli-util");
function generateInoFile(app, filePath, destination) {
    const data = (0, cli_util_1.extractDestinationAndName)(filePath, destination);
    const generatedFilePath = `${path_1.default.join(data.destination, data.name)}.ino`;
    const fileNode = new langium_1.CompositeGeneratorNode();
    compile(app, fileNode);
    if (!fs_1.default.existsSync(data.destination)) {
        fs_1.default.mkdirSync(data.destination, { recursive: true });
    }
    fs_1.default.writeFileSync(generatedFilePath, (0, langium_1.toString)(fileNode));
    return generatedFilePath;
}
exports.generateInoFile = generateInoFile;
function compile(app, fileNode) {
    var _a;
    fileNode.append(`
//Wiring code generated from an ArduinoML model
// Application name: ` + app.name + `

long debounce = 200;
enum STATE {
    ` + app.states.map(s => isErrorState(s) ? `error${s.code}` : s.name).join(', ') + `
};

STATE currentState = ` + (isErrorState(app.initial.ref)
        ? `error${app.initial.ref.code}`
        : (_a = app.initial.ref) === null || _a === void 0 ? void 0 : _a.name) + `;`, langium_1.NL);
    for (const brick of app.bricks) {
        if ("inputPin" in brick) {
            fileNode.append(`
bool ` + brick.name + `BounceGuard = false;
long ` + brick.name + `LastDebounceTime = 0;

            `, langium_1.NL);
        }
    }
    fileNode.append(`
	void setup(){`);
    for (const brick of app.bricks) {
        if ("inputPin" in brick) {
            compileSensor(brick, fileNode);
        }
        else {
            compileActuator(brick, fileNode);
        }
    }
    fileNode.append(`
	}
	void loop() {
			switch(currentState){`, langium_1.NL);
    for (const state of app.states) {
        compileState(state, fileNode);
    }
    fileNode.append(`
		}
	}
	`, langium_1.NL);
}
function isErrorState(state) {
    return state.code !== undefined;
}
function compileActuator(actuator, fileNode) {
    fileNode.append(`
		pinMode(` + actuator.outputPin + `, OUTPUT); // ` + actuator.name + ` [Actuator]`);
}
function compileSensor(sensor, fileNode) {
    fileNode.append(`
		pinMode(` + sensor.inputPin + `, INPUT); // ` + sensor.name + ` [Sensor]`);
}
function compileState(state, fileNode) {
    const stateName = isErrorState(state)
        ? `error${state.code}`
        : state.name;
    fileNode.append(`
                    case ${stateName}:`);
    if (isErrorState(state)) {
        const code = state.code;
        fileNode.append(`
                while(true) {
                    for(int i = 0; i < ${code}; i++) {
                        digitalWrite(12, HIGH);
                        delay(500);
                        digitalWrite(12, LOW);
                        delay(500);
                    }
                    delay(${code} * 500);
                }`);
        fileNode.append(`
                    break;`);
        return;
    }
    for (const action of state.actions) {
        compileAction(action, fileNode);
    }
    if (state.expression) {
        compileExpression(state.expression, fileNode);
    }
    fileNode.append(`
                    break;`);
}
function compileAction(action, fileNode) {
    var _a;
    fileNode.append(`
                digitalWrite(` + ((_a = action.actuator.ref) === null || _a === void 0 ? void 0 : _a.outputPin) + `,` + action.value.value + `);
        `);
}
function compileExpression(expression, fileNode) {
    const conditionCode = generateCondition(expression.condition);
    fileNode.append(`
                if (${conditionCode}) {
                    ${generateTransitionCode(expression.transition)}
                }
            `);
}
function generateTransitionCode(transition) {
    const nextState = stateName(transition.next.ref);
    return `
            currentState = ${nextState};
        `;
}
function stateName(state) {
    if (isErrorState(state)) {
        return `error${state.code}`;
    }
    return state.name;
}
function generateCondition(expr) {
    if (expr.$type === 'Condition') {
        return compileCondition(expr);
    }
    if (expr.$type === 'AndExpression') {
        return generateAndCondition(expr);
    }
    if (expr.$type === 'OrExpression') {
        return generateOrCondition(expr);
    }
    return "";
}
function compileCondition(condition) {
    var _a, _b;
    return `
            digitalRead(` + ((_a = condition.sensor.ref) === null || _a === void 0 ? void 0 : _a.name) + `) == ` + condition.value.value + ` && ` + ((_b = condition.sensor.ref) === null || _b === void 0 ? void 0 : _b.name) + `BounceGuard
        `;
}
function generateAndCondition(expr) {
    return `${generateCondition(expr.left)} && ${generateCondition(expr.right)}`;
}
function generateOrCondition(expr) {
    return `${generateCondition(expr.left)} || ${generateCondition(expr.right)}`;
}
//# sourceMappingURL=generator.js.map