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
  AfterContentInit,
  Component,
  ContentChild,
  ElementRef,
  forwardRef,
  HostListener,
  Injector,
  ViewChild,
  inject,
} from '@angular/core';
import { CdkConnectedOverlay, CdkOverlayOrigin } from '@angular/cdk/overlay';
import { FormControl, NG_VALUE_ACCESSOR } from '@angular/forms';
import { AbstractFormInputDirective } from '../abstract-form-input.directive';

export const FORM_FIELD_VALUE_WRAPPER_VALUE_ACCESSOR: any = {
  provide: NG_VALUE_ACCESSOR,
  useExisting: forwardRef(() => FormFieldValueWrapperComponent),
  multi: true,
};

@Component({
  selector: 'vitamui-form-field-value-wrapper',
  imports: [CdkConnectedOverlay, CdkOverlayOrigin],
  templateUrl: './form-field-value-wrapper.component.html',
  styleUrl: './form-field-value-wrapper.component.scss',
  providers: [FORM_FIELD_VALUE_WRAPPER_VALUE_ACCESSOR],
})
export class FormFieldValueWrapperComponent extends AbstractFormInputDirective implements AfterContentInit {
  editMode: boolean;
  private innerControl: FormControl;

  #cancelTimeout: number;
  #componentRef: Element;

  get canConfirm(): boolean {
    return (
      this.editMode &&
      this.innerControl &&
      !this.innerControl.pending &&
      this.innerControl.valid &&
      this.innerControl.dirty &&
      this.ref.canConfirmInWrapper()
    );
  }

  @ContentChild(AbstractFormInputDirective) ref!: AbstractFormInputDirective;
  @ViewChild(CdkConnectedOverlay) cdkConnectedOverlay: CdkConnectedOverlay;

  @HostListener('focusin')
  focusIn() {
    this.editMode = true;
  }

  @HostListener('document:click', ['$event.target'])
  onClick(target: HTMLElement) {
    const clickInside =
      this.isInside(target, this.#componentRef) || this.isInside(target, this.cdkConnectedOverlay.overlayRef?.hostElement);
    const activeElementInside = this.isInside(document.activeElement, this.#componentRef);
    if (clickInside || activeElementInside) {
      clearTimeout(this.#cancelTimeout);
    } else if (this.editMode) {
      this.cancel();
    }
  }

  @HostListener('focusout', ['$event.relatedTarget'])
  focusOut(target: HTMLElement) {
    const overlayRef = this.cdkConnectedOverlay.overlayRef;
    if (this.isInside(target, this.#componentRef) || this.isInside(target, overlayRef.hostElement)) {
      return;
    }
    this.#cancelTimeout = window.setTimeout(() => this.cancel(), 100);
  }

  @HostListener('document:keydown.escape', ['$event'])
  onEscape(event: KeyboardEvent) {
    event.preventDefault();
    this.cancel();
  }

  @HostListener('keydown.enter')
  onEnter() {
    this.confirm();
  }

  constructor() {
    const injector = inject(Injector);
    const elementRef = inject(ElementRef);

    super(injector);
    this.#componentRef = elementRef.nativeElement;
  }

  ngAfterContentInit() {
    this.innerControl = this.ref?.setControl(this.control);
  }

  writeValue(value: any) {
    this.ref?.writeValue(value);
  }

  confirm() {
    if (!this.canConfirm) return;
    (this.innerControl as any).resetValue = this.innerControl.value;
    this.onChange(this.innerControl.value);
    this.cancel();
  }

  cancel() {
    const resetValue = (this.innerControl as any)?.resetValue || undefined;
    this.innerControl.reset(resetValue, { emitEvent: false });
    const isInside = this.isInside(document.activeElement, this.#componentRef);
    if (isInside) {
      (document.activeElement as HTMLElement)?.blur();
    }
    this.editMode = false;
  }

  protected isInside(target: Element, element: Element): boolean {
    return element && (target === element || element.contains(target));
  }

  positions = [
    {
      originX: 'end',
      originY: 'top',
      overlayX: 'start',
      overlayY: 'top',
    },
    {
      originX: 'end',
      originY: 'bottom',
      overlayX: 'end',
      overlayY: 'top',
    },
    {
      originX: 'end',
      originY: 'top',
      overlayX: 'end',
      overlayY: 'bottom',
    },
  ];
}
