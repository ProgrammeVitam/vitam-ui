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
import { Component, forwardRef, Input } from '@angular/core';
import { NG_VALUE_ACCESSOR } from '@angular/forms';

export const CARD_SELECT_VALUE_ACCESSOR: any = {
  provide: NG_VALUE_ACCESSOR,
  useExisting: forwardRef(() => CardSelectComponent),
  multi: true,
};

@Component({
  selector: 'vitamui-card-select',
  templateUrl: './card-select.component.html',
  styleUrls: ['./card-select.component.scss'],
  providers: [CARD_SELECT_VALUE_ACCESSOR],
})
export class CardSelectComponent {
  @Input()
  placeholder: any;

  @Input()
  label = 'Add';

  @Input()
  disabled = false;

  @Input()
  size: number;

  values = new Set<string>();

  addElement(input: any) {
    if (this.size !== undefined && this.values.size >= this.size) {
      return;
    }
    if (input.value != null && input.value !== '') {
      this.values.add((input.value as string).trim());
      input.writeValue(null);
      this.onChange(Array.from(this.values));
    }
  }

  changeValue($event: Set<string>) {
    this.values = $event;
    this.onChange(Array.from(this.values));
  }

  onChange = (_: any) => {};
  onTouched = () => {};

  writeValue(value: Array<string>) {
    this.values = new Set(value == null ? new Array<string>() : value);
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }
}
