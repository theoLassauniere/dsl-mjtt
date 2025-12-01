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
        App: validator.checkNothing,
        ErrorState: validator.checkErrorState,
        NormalState: validator.checkNormalState
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
    checkErrorState(state, accept) {
        var _a;
        if (((_a = state.actions) === null || _a === void 0 ? void 0 : _a.length) > 0) {
            accept('error', 'Error states cannot contain actions.', { node: state });
        }
        if (state.expression) {
            accept('error', 'Error states cannot have expression.', { node: state });
        }
    }
    checkNormalState(state, accept) {
        if (!state.expression) {
            accept('error', 'Normal states must define an expression.', { node: state });
        }
    }
}
exports.ArduinoMlValidator = ArduinoMlValidator;
//# sourceMappingURL=arduino-ml-validator.js.map