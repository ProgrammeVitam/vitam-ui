/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
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
import { CdkConnectedOverlay } from '@angular/cdk/overlay';
import { AfterContentInit, ContentChildren, Directive, ElementRef, EventEmitter,
   HostBinding, HostListener, Input, Output, QueryList, ViewChild } from '@angular/core';
import { AsyncValidatorFn, ControlValueAccessor, FormControl, ValidatorFn } from '@angular/forms';

import { VitamUIFieldErrorComponent } from '../vitamui-field-error/vitamui-field-error.component';

@Directive()
// tslint:disable-next-line:directive-class-suffix
export class EditableFieldComponent implements AfterContentInit, ControlValueAccessor {

  @Input() label: string;
  @Input()
  set disabled(disabled: boolean) {
    this._disabled = disabled;
    this.readonly = this._disabled;
  }
  get disabled(): boolean { return this._disabled; }
  // tslint:disable-next-line:variable-name
  private _disabled: boolean;

  @Input()
  set validator(validator: ValidatorFn) {
    this.control.setValidators(validator);
    this.control.setErrors(null);
  }

  @Input()
  set asyncValidator(asyncValidator: AsyncValidatorFn) {
    this.control.setAsyncValidators(asyncValidator);
    this.control.setErrors(null);
  }

  @Output() editOpen = new EventEmitter<void>();
  @Output() editClose = new EventEmitter<boolean>();

  @ViewChild(CdkConnectedOverlay) cdkConnectedOverlay: CdkConnectedOverlay;
  @ContentChildren(VitamUIFieldErrorComponent) errors: QueryList<VitamUIFieldErrorComponent>;

  @HostBinding('class.readonly') readonly = false;

  control = new FormControl();
  originValue: any;
  editMode = false;
  positions = [
    {
      originX: 'end',
      originY: 'center',
      overlayX: 'start',
      overlayY: 'center'
    },
    {
      originX: 'end',
      originY: 'bottom',
      overlayX: 'end',
      overlayY: 'top'
    },
    {
      originX: 'end',
      originY: 'top',
      overlayX: 'end',
      overlayY: 'bottom'
    },
  ];

  get showSpinner(): boolean { return this.control.pending && this.control.dirty; }
  get canConfirm() { return this.editMode && !this.control.pending && this.control.valid && this.control.dirty; }

  constructor(protected elementRef: ElementRef) {}

  ngAfterContentInit() {
    this.control.statusChanges.subscribe(() => {
      this.errors.forEach((error: VitamUIFieldErrorComponent) => {
        error.show = this.control.errors ? !!this.control.errors[error.errorKey] : false;
      });
    });
  }

  onChange = (_: any) => {};
  onTouched = () => {};

  writeValue(value: any) {
    this.control.reset(value, { emitEvent: false });
    this.originValue = value;
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  setDisabledState(disabled: boolean) {
    this.disabled = disabled;
  }

  enterEditMode() {
    this.editMode = true;
  }

  @HostListener('document:keydown.escape', ['$event'])
  onEscape(event: KeyboardEvent) {
    event.preventDefault();
    this.cancel();
  }

  @HostListener('document:keydown.enter', ['$event'])
  onEnter(event: KeyboardEvent) {
    event.preventDefault();
    this.confirm();
  }

  @HostListener('document:click', ['$event.target'])
  onClick(target: HTMLElement) {
    if (!this.editMode) { return; }
    const overlayRef = this.cdkConnectedOverlay.overlayRef;
    if (this.isInside(target, this.elementRef.nativeElement) || this.isInside(target, overlayRef.hostElement)) {
      return;
    }
    this.cancel();
  }

  confirm() {
    if (!this.canConfirm) { return; }
    this.editMode = false;
    this.onChange(this.control.value);
    this.originValue = this.control.value;
    this.control.reset(this.originValue);
  }

  cancel() {
    if (!this.editMode) { return; }
    this.editMode = false;
    this.control.reset(this.originValue);
  }

  protected isInside(target: HTMLElement, element: HTMLElement): boolean {
    return element && (target === element || element.contains(target));
  }

}
