/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
import {
  ChangeDetectorRef,
  Directive,
  Injector,
  OnChanges,
  OnDestroy,
  OnInit,
  SimpleChanges,
  effect,
  inject,
  input,
  linkedSignal,
  ModelSignal,
  output,
  runInInjectionContext,
  signal,
  untracked,
  WritableSignal,
} from '@angular/core';
import {
  AsyncValidatorFn,
  ControlContainer,
  ControlValueAccessor,
  FormControl,
  FormControlDirective,
  FormControlName,
  FormGroup,
  NgControl,
  NgModel,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';
import { FORM_FIELD, FormField, transformedValue } from '@angular/forms/signals';
import { Subscription } from 'rxjs';
import { coerceBooleanProperty } from '@angular/cdk/coercion';
import { MiscValidators } from '../validators/misc.validators';
import { fromValidationErrors, toValidationErrors } from '../signal-forms/signal-form-errors';

function isSameValue(a: unknown, b: unknown): boolean {
  if (Array.isArray(a) && Array.isArray(b)) {
    return a.length === b.length && a.every((item, index) => item === b[index]);
  }
  return a === b;
}

@Directive()
export class AbstractFormInputDirective implements ControlValueAccessor, OnInit, OnDestroy, OnChanges {
  private readonly injector: Injector;
  readonly #changeDetectorRef = inject(ChangeDetectorRef);
  // Set when the component is bound to a signal forms field with [formField]
  #formField: FormField<unknown> | null = null;
  // Errors computed by the component, reported to the signal forms field (see reportErrors)
  #reportedErrors: WritableSignal<ValidationErrors | null> | null = null;
  // Required state of the signal forms field, copied by the bindFormField effect (the field must not be read outside of it: it can be orphan)
  readonly #fieldRequired = signal(false);

  constructor(injector: Injector = inject(Injector)) {
    this.injector = injector;
  }

  readonly errorMessageMap = input<{
    [p: string]: string;
  }>(undefined);
  readonly required = input<boolean, any>(undefined, { transform: coerceBooleanProperty });
  readonly disabled = input<boolean, any>(undefined, { transform: coerceBooleanProperty });
  // Disabled state, initialized from the `disabled` input, that subclasses can override (e.g. in setDisabledState)
  protected readonly disabledState = linkedSignal(() => this.disabled());

  // eslint-disable-next-line @angular-eslint/no-output-native
  readonly change = output<any>(); // To be able to use (change)="..." on components

  protected control: FormControl;

  #subscription?: Subscription;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['required'] && this.control) {
      this.updateValidators();
    }
    if (changes['disabled'] && this.control) {
      this.updateDisabled();
    }
  }

  // Used by FormFieldValueWrapperComponent
  setControl(control: FormControl) {
    this.control.patchValue(control.value);
    this.control.addValidators((control as any)._rawValidators as ValidatorFn[]);
    this.control.addAsyncValidators((control as any)._rawAsyncValidators as AsyncValidatorFn[]);
    control.registerOnDisabledChange((disabled) => (disabled ? this.control.disable() : this.control.enable()));
    control.valueChanges.subscribe((value) => this.control.setValue(value));
    this.afterControlSet();
    return this.control;
  }

  // Used in FormFieldValueWrapperComponent to know if the "green check" should be enabled. For example, for SearchWithTypeSelectorComponent, we don't want the "green check" to be enabled if we only selected the search type and haven't typed a value
  canConfirmInWrapper(): boolean {
    return !!this.control.value;
  }

  /**
   * Callback method, called after setControl has been called.
   * It is useful to run some configuration code after changing the control (when wrapped by FormFieldValueWrapperComponent).
   */
  afterControlSet() {}

  ngOnInit() {
    const ngControl = this.injector.get(NgControl, null, { self: true, optional: true });

    if (ngControl instanceof NgModel) {
      this.control = ngControl.control;
      this.#subscription = ngControl.control.valueChanges.subscribe((value) => {
        if (ngControl.model !== value || ngControl.viewModel !== value) {
          ngControl.viewToModelUpdate(value);
        }
      });
    } else if (ngControl instanceof FormControlDirective) {
      this.control = ngControl.control;
    } else if (ngControl instanceof FormControlName) {
      const container = this.injector.get(ControlContainer).control as FormGroup;
      this.control = container.controls[ngControl.name] as FormControl;
    } else {
      this.control = new FormControl();
      // Resolved lazily: FormField depends on this component (as NG_VALUE_ACCESSOR)
      this.#formField = this.injector.get(FORM_FIELD, null, { self: true, optional: true });
      if (this.#formField) {
        this.bindFormField(this.#formField);
      }
    }

    this.updateValidators();
    this.updateDisabled();
  }

  ngOnDestroy() {
    this.#subscription?.unsubscribe();
  }

  onChange = (value: any) => this.change.emit(value); // Emits value in EventEmitter, even when the component has no control (no [ngModel] nor formControlName)
  onTouched = () => {};

  registerOnChange(fn: any): void {
    this.onChange = (value) => {
      fn(value);
      this.change.emit(value); // Emits value in EventEmitter
    };
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  writeValue(_obj: any) {}

  isRequired(): boolean {
    return (
      this.#fieldRequired() || this.control.hasValidator(Validators.required) || this.control.hasValidator(MiscValidators.requiredNotBlank)
    );
  }

  /**
   * Reports errors computed by the component itself (e.g. by validators added to the internal control) to the signal forms field
   * bound with [formField], so that the field (and its form) is invalid. It has no effect with reactive forms or ngModel,
   * where the control is shared with the parent.
   */
  protected reportErrors(errors: ValidationErrors | null) {
    this.#reportedErrors?.set(errors);
  }

  /**
   * Mirrors the state of the signal forms field (value, errors, touched, disabled) in the internal control,
   * so that subclasses and their templates can keep using `control`.
   */
  private bindFormField(formField: FormField<unknown>) {
    // transformedValue reports its parse errors to the field bound on this element: use it to report the component errors
    this.#reportedErrors = runInInjectionContext(this.injector, () =>
      // transformedValue expects a ModelSignal, but only uses it as a WritableSignal
      transformedValue(signal(null) as unknown as ModelSignal<null>, {
        parse: (errors: ValidationErrors | null) => ({ error: fromValidationErrors(errors) }),
        format: (): ValidationErrors | null => null,
      }),
    );
    // The field state is only read in the effect below, and copied in these variables. The validator and the valueChanges subscription
    // can run outside of the effect (e.g. control.enable() in a setTimeout), when the field can be orphan (e.g. an array item that has
    // been removed or replaced): reading it would throw NG01904.
    let fieldErrors: ValidationErrors | null = null;
    // Last value written from the field to the internal control
    let fieldValue: unknown = this.control.value;
    let fieldControlValue: WritableSignal<unknown> | null = null;
    this.control.addValidators(() => fieldErrors);
    // Some subclasses only update the internal control (without calling onChange): propagate its changes to the field.
    // Values equal to the last field value are ignored (e.g. emitted by control.enable()), so that they cannot override a newer field value.
    // Arrays are compared by content: some subclasses copy them (e.g. SelectComponent), which would otherwise loop with writeValue.
    this.#subscription = this.control.valueChanges.subscribe((value) => {
      if (fieldControlValue && !isSameValue(value, fieldValue) && !isSameValue(value, untracked(fieldControlValue))) {
        fieldControlValue.set(value);
      }
    });
    effect(
      () => {
        const state = formField.state();
        const value = state.value();
        const disabled = state.disabled();
        const touched = state.touched();
        const required = state.required();
        const errors = toValidationErrors(formField.errors());
        untracked(() => {
          fieldErrors = errors;
          fieldControlValue = state.controlValue;
          this.#fieldRequired.set(required);
          if (disabled !== this.control.disabled) {
            disabled ? this.control.disable({ emitEvent: false }) : this.control.enable({ emitEvent: false });
          }
          // Also recomputes the errors of the internal control from the field errors
          fieldValue = value;
          this.control.setValue(value, { emitEvent: false });
          touched ? this.control.markAsTouched() : this.control.markAsUntouched();
          this.#changeDetectorRef.markForCheck();
        });
      },
      { injector: this.injector },
    );
  }

  private updateValidators() {
    const required = this.required();
    if (required) this.control.addValidators(Validators.required);
    else if (required === false) this.control.removeValidators(Validators.required);
  }

  private updateDisabled() {
    // We use setTimeout to prevent ExpressionChangedAfterItHasBeenCheckedError
    setTimeout(() => {
      const disabled = this.disabledState();
      if (disabled) this.control.disable();
      else if (disabled === false) this.control.enable();
      // Runs outside change detection: refresh the view (zoneless)
      this.#changeDetectorRef.markForCheck();
    });
  }
}
