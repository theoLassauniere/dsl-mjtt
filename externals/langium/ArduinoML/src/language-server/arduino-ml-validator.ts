import { ValidationAcceptor, ValidationChecks } from 'langium';
import { ArduinoMlAstType, App } from './generated/ast';
import type { ArduinoMlServices } from './arduino-ml-module';

/**
 * Register custom validation checks.
 */
export function registerValidationChecks(services: ArduinoMlServices) {
    const registry = services.validation.ValidationRegistry;
    const validator = services.validation.ArduinoMlValidator;
    const checks: ValidationChecks<ArduinoMlAstType> = {
        App: [
            validator.checkNothing,
            validator.checkActuatorsExist
        ]
    };
    registry.register(checks, validator);
}

/**
 * Implementation of custom validations.
 */
export class ArduinoMlValidator {

    checkNothing(app: App, accept: ValidationAcceptor): void {
        if (app.name) {
            const firstChar = app.name.substring(0, 1);
            if (firstChar.toUpperCase() !== firstChar) {
                accept('warning', 'App name should start with a capital.', { node: app, property: 'name' });
            }
        }
    }
    checkActuatorsExist(app: App, accept: ValidationAcceptor): void {
    for (const state of app.states) {
        // Moore actions
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

        // Mealy actions
        if (state.expression.transition && state.expression.transition.mealyActions) {
            for (const action of state.actions) {
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
