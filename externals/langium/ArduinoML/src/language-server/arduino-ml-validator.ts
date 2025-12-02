import { ValidationAcceptor, ValidationChecks } from 'langium';
import { ArduinoMlAstType, App, NormalState, ErrorState } from './generated/ast';
import type { ArduinoMlServices } from './arduino-ml-module';

/**
 * Register custom validation checks.
 */
export function registerValidationChecks(services: ArduinoMlServices) {
    const registry = services.validation.ValidationRegistry;
    const validator = services.validation.ArduinoMlValidator;
    const checks: ValidationChecks<ArduinoMlAstType> = {
        App: validator.checkNothing,
        ErrorState: validator.checkErrorState,
        NormalState: validator.checkNormalState
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

    checkErrorState(state: ErrorState, accept: ValidationAcceptor): void {
        if ((state as any).actions?.length > 0) {
            accept('error', 'Error states cannot contain actions.', { node: state });
        }

        if ((state as any).transition) {
            accept('error', 'Error states cannot have transitions.', { node: state });
        }
    }

    checkNormalState(state: NormalState, accept: ValidationAcceptor): void {
        if (!state.transition) {
            accept('error', 'Normal states must define a transition.', { node: state });
        }
    }
}
