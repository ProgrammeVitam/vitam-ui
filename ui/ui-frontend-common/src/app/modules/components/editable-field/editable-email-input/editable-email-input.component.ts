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
/* tslint:disable: no-use-before-declare */

import { Component, ElementRef, forwardRef, Input, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, NG_VALUE_ACCESSOR } from '@angular/forms';
import { MatSelect } from '@angular/material/select';

import { EditableFieldComponent } from '../editable-field.component';

export const EDITABLE_EMAIL_INPUT_VALUE_ACCESSOR: any = {
  provide: NG_VALUE_ACCESSOR,
  useExisting: forwardRef(() => EditableEmailInputComponent),
  multi: true
};

@Component({
  selector: 'vitamui-common-editable-email-input',
  templateUrl: './editable-email-input.component.html',
  styleUrls: ['./editable-email-input.component.scss'],
  providers: [EDITABLE_EMAIL_INPUT_VALUE_ACCESSOR]
})

export class EditableEmailInputComponent extends EditableFieldComponent {

  @ViewChild('select') select: MatSelect;

  constructor(formBuilder: FormBuilder, elementRef: ElementRef) {
    super(elementRef);

    this.formEmail = formBuilder.group({
      emailFirstPart: null,
      domain: [null]
    });

    this.formEmail.valueChanges.subscribe((emailData) => {
      this.control.setValue(emailData.emailFirstPart + '@' + emailData.domain);
      this.control.markAsDirty();
    });

   }

  formEmail: FormGroup;

  @Input() domains: string[];

  writeValue(value: any) {
    this.control.reset(value, { emitEvent: false });
    this.originValue = value;
    if (value) {
      const splittedEmail = value.split('@');
      this.formEmail.get('emailFirstPart').setValue(splittedEmail[0], { emitEvent: false });
      this.formEmail.get('domain').setValue(splittedEmail[1], { emitEvent: false });
    }
  }

  onClick(target: HTMLElement) {
    if (!this.editMode) { return; }
    const overlayRef = this.cdkConnectedOverlay.overlayRef;
    const selectOverlayRef = this.select.overlayDir.overlayRef;
    if (
      this.isInside(target, this.elementRef.nativeElement) ||
      this.isInside(target, overlayRef.hostElement) ||
      this.isInside(target, selectOverlayRef ? selectOverlayRef.overlayElement : null) ||
      this.isInside(target, selectOverlayRef ? selectOverlayRef.backdropElement : null)
    ) {
      return;
    }
    this.cancel();
  }

}
