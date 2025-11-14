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
import { Directive, EventEmitter, Injector, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges } from '@angular/core';
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
  ValidatorFn,
  Validators,
} from '@angular/forms';
import { Subscription } from 'rxjs';
import { coerceBooleanProperty } from '@angular/cdk/coercion';
import { MiscValidators } from '../validators/misc.validators';

@Directive()
export class AbstractFormInputDirective implements ControlValueAccessor, OnInit, OnDestroy, OnChanges {
  @Input() errorMessageMap: { [p: string]: string };
  @Input({ transform: coerceBooleanProperty }) required: boolean;
  @Input({ transform: coerceBooleanProperty }) disabled: boolean;

  // eslint-disable-next-line @angular-eslint/no-output-native
  @Output() change = new EventEmitter(); // To be able to use (change)="..." on components

  protected control: FormControl;

  #subscription?: Subscription;

  constructor(private injector: Injector) {}

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
      this.control.hasValidator(Validators.required) ||
      this.control.hasValidator(MiscValidators.requiredNotBlank) ||
      this.control.hasValidator(MiscValidators.requiredIdentifier)
    );
  }

  private updateValidators() {
    if (this.required) this.control.addValidators(Validators.required);
    else if (this.required === false) this.control.removeValidators(Validators.required);
  }

  private updateDisabled() {
    // We use setTimeout to prevent ExpressionChangedAfterItHasBeenCheckedError
    setTimeout(() => {
      if (this.disabled) this.control.disable();
      else if (this.disabled === false) this.control.enable();
    });
  }
}
