"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.ArduinoMlValidator = exports.registerValidationChecks = void 0;
/**
 * Register custom validation checks.
 */
function registerValidationChecks(services) {
    const registry = services.validation.ValidationRegistry;
    const validator = services.validation.ArduinoMlValidator;
    const checks = {
        App: [
            validator.checkNothing,
            validator.checkActuatorsExist
        ]
    };
    registry.register(checks, validator);
}
exports.registerValidationChecks = registerValidationChecks;
/**
 * Implementation of custom validations.
 */
class ArduinoMlValidator {
    checkNothing(app, accept) {
        if (app.name) {
            const firstChar = app.name.substring(0, 1);
            if (firstChar.toUpperCase() !== firstChar) {
                accept('warning', 'App name should start with a capital.', { node: app, property: 'name' });
            }
        }
    }
    checkActuatorsExist(app, accept) {
        for (const state of app.states) {
            // Moore
            if (state.actions) {
                for (const action of state.actions) {
                    if (!action.actuator.ref) {
                        accept('error', `Actuator '${action.actuator.$refText}' is not declared.`, {
                            node: action,
                            property: 'actuator'
                        });
                    }
                }
            }
            // Mealy
            if (state.expression.mealyActions) {
                for (const action of state.expression.mealyActions) {
                    if (!action.actuator.ref) {
                        accept('error', `Actuator '${action.actuator.$refText}' is not declared in transition.`, {
                            node: action,
                            property: 'actuator'
                        });
                    }
                }
            }
        }
    }
}
exports.ArduinoMlValidator = ArduinoMlValidator;
//# sourceMappingURL=arduino-ml-validator.js.map