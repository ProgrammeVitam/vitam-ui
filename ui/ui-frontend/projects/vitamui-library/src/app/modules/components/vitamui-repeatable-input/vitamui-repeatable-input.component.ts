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
import { Component, ElementRef, forwardRef, HostBinding, HostListener, Input } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

export const REPEATABLE_INPUT_VALUE_ACCESSOR: any = {
  provide: NG_VALUE_ACCESSOR,
  useExisting: forwardRef(() => VitamuiRepeatableInputComponent),
  multi: true,
};

type InternalValue = { id: number; value: string | number | boolean };

@Component({
  selector: 'vitamui-common-repeatable-input',
  templateUrl: './vitamui-repeatable-input.component.html',
  styleUrls: ['./vitamui-repeatable-input.component.scss'],
  providers: [REPEATABLE_INPUT_VALUE_ACCESSOR],
})
export class VitamuiRepeatableInputComponent implements ControlValueAccessor {
  @Input() placeholder: string;
  @Input() autofocus: boolean;
  @HostBinding('class.textarea')
  @Input()
  textarea = false;
  @Input() addTooltipKey = 'REPEATABLE_INPUT.ADD_TOOLTIP';
  @Input() removeTooltipKey = 'REPEATABLE_INPUT.REMOVE_TOOLTIP';

  items: InternalValue[];
  focused: number;
  @HostBinding('class.disabled') disabled: boolean;

  @HostBinding('class.vitamui-float')
  get labelFloat() {
    return !!this.items && !this.isEmpty(this.items[0]?.value);
  }

  onChange = (_: any) => {};
  onTouched = () => {};

  @HostListener('click', ['$event.target'])
  onClick(target: Element) {
    if (!['INPUT', 'TEXTAREA', 'BUTTON', 'I'].includes(target.tagName)) {
      const input = target.querySelector('input, textarea') as HTMLElement;
      if (input) {
        input.focus();
      } else {
        this.elRef.nativeElement.querySelector('input:first-of-type, textarea:first-of-type').focus();
      }
    }
  }

  constructor(private elRef: ElementRef) {}

  writeValue(values: InternalValue['value'][]) {
    this.items = (values && values.length ? values : ['']).map((v, i) => ({ id: i, value: v.toString() }));
  }

  addInput() {
    const lastIndex = this.items.length - 1;
    this.items.push({ id: this.items[lastIndex].id + 1, value: '' });
    setTimeout(() => this.elRef.nativeElement.querySelectorAll('input, textarea')[lastIndex + 1].focus());
  }

  removeInput(i: number) {
    this.items.splice(i, 1);
    this.onChange(this.items.map((v) => v.value).filter((v) => !this.isEmpty(v)));
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  onValueChange(value: string, i: number) {
    this.items[i].value = value;
    this.onChange(this.items.map((v) => v.value).filter((v) => !!v));
  }

  onFocus(i: number) {
    this.focused = i;
    this.onTouched();
  }

  onBlur(i: number) {
    this.focused = null;
    const hasMoreThanOneLine = this.items.length > 1;
    if (this.isEmpty(this.items[i].value) && hasMoreThanOneLine) {
      this.removeInput(i);
    }
    this.onTouched();
  }

  isEmpty(s: InternalValue['value']): boolean {
    return !s?.toString().replace(/\s/g, '');
  }

  trackBy(_: number, item: InternalValue) {
    return item.id;
  }

  setDisabledState(isDisabled: boolean) {
    this.disabled = isDisabled;
  }
}
