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

import { Component, ElementRef, EventEmitter, forwardRef, Input, Output } from '@angular/core';
import { NG_VALUE_ACCESSOR } from '@angular/forms';

import { EditableFieldComponent } from 'ui-frontend-common';

export const EDITABLE_DOMAIN_INPUT_VALUE_ACCESSOR: any = {
  provide: NG_VALUE_ACCESSOR,
  useExisting: forwardRef(() => EditableDomainInputComponent),
  multi: true
};

@Component({
  selector: 'app-editable-domain-input',
  templateUrl: './editable-domain-input.component.html',
  providers: [EDITABLE_DOMAIN_INPUT_VALUE_ACCESSOR]
})
export class EditableDomainInputComponent extends EditableFieldComponent {

  @Input()
  set defaultDomain(defaultDomain: string) {
    this._defaultDomain = defaultDomain;
    this.selected = defaultDomain;
  }
  get defaultDomain(): string { return this._defaultDomain; }
  private _defaultDomain: string;

  @Output() defaultDomainChange = new EventEmitter<string>();

  selected: string;

  private domainInputClicked = false;

  get canConfirm(): boolean {
    return this.editMode && !this.control.pending && this.control.valid && (this.control.dirty || (this.selected !== this.defaultDomain));
  }

  constructor(elementRef: ElementRef) {
    super(elementRef);
  }

  confirm() {
    if (!this.canConfirm) { return; }
    super.confirm();
    this.defaultDomain = this.selected;
    this.defaultDomainChange.emit(this.defaultDomain);
  }

  cancel() {
    super.cancel();
    this.selected = this.defaultDomain;
  }

  onClick(target: HTMLElement) {
    if (!this.editMode) { return; }
    if (this.domainInputClicked) {
      this.domainInputClicked = false;

      return;
    }
    const overlayRef = this.cdkConnectedOverlay.overlayRef;
    if (this.isInside(target, this.elementRef.nativeElement) || this.isInside(target, overlayRef.hostElement)) {
      return;
    }
    this.cancel();
  }

  onDomainInputClick() {
    this.domainInputClicked = true;
  }

}
