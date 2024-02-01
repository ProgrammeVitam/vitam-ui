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

import { Component, ElementRef, forwardRef, HostListener, Input, OnChanges, OnDestroy, SimpleChanges, ViewChild } from '@angular/core';
import { EditableFieldComponent } from '../editable-field/editable-field.component';
import { NG_VALUE_ACCESSOR } from '@angular/forms';
import { Subscription } from 'rxjs';

export const MULTIPLE_INPUT_VALUE_ACCESSOR: any = {
  provide: NG_VALUE_ACCESSOR,
  useExisting: forwardRef(() => VitamuiMultiInputsComponent),
  multi: true,
};

@Component({
  selector: 'vitamui-multi-inputs',
  templateUrl: './vitamui-multi-inputs.component.html',
  styleUrls: ['./vitamui-multi-inputs.component.scss'],
  providers: [MULTIPLE_INPUT_VALUE_ACCESSOR],
})
export class VitamuiMultiInputsComponent extends EditableFieldComponent implements OnDestroy, OnChanges {
  values: string[] = [];

  @Input() maxlength: number;
  @Input() truncateLimit: number = 20;
  @Input() type = 'text';
  @Input() searchActivated: boolean = false;
  @Input() reset: boolean = false;
  @ViewChild('input') private input: ElementRef;
  valSub: Subscription;

  constructor(elementRef: ElementRef) {
    super(elementRef);
  }

  enterEditMode() {
    super.enterEditMode();
    setTimeout(() => this.input.nativeElement.focus(), 0);
  }

  confirm() {
    if (this.control.invalid || this.control.pending) {
      return;
    }
    const val = this.control.value;
    if (this.values.includes(val)) {
      return;
    }
    this.values.push(val);
    this.onChange(this.values);
    this.originValue = this.values;
    this.editMode = false;
    this.control.reset();
  }

  remove(val: string): void {
    const index = this.values.indexOf(val);

    if (index >= 0) {
      this.values.splice(index, 1);
      this.onChange(this.values);
    }
  }

  ngOnDestroy(): void {
    this.valSub.unsubscribe();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['reset']?.currentValue) {
      this.values = [];
      this.originValue = this.values;
      this.onChange(this.values);
      this.control.reset(this.values);
    }
  }
}
