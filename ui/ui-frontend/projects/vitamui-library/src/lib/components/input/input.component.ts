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
import { Component, ElementRef, forwardRef, HostBinding, HostListener, inject, Injector, Input } from '@angular/core';
import { FormsModule, NG_VALUE_ACCESSOR, Validators } from '@angular/forms';
import { AbstractFormInputDirective } from '../abstract-form-input.directive';

import { FormErrorsComponent } from '../form-errors/form-errors.component';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { TranslatePipe } from '@ngx-translate/core';
import { TooltipDirective } from '../../../app/modules/components/common-tooltip/tooltip.directive';

export const INPUT_VALUE_ACCESSOR: any = {
  provide: NG_VALUE_ACCESSOR,
  useExisting: forwardRef(() => InputComponent),
  multi: true,
};

type InternalValue = { id: number; value: string | number | boolean };

@Component({
  selector: 'vitamui-input',
  templateUrl: './input.component.html',
  styleUrls: ['./input.component.scss'],
  providers: [INPUT_VALUE_ACCESSOR],
  imports: [FormsModule, TooltipDirective, FormErrorsComponent, MatProgressSpinner, TranslatePipe],
})
export class InputComponent extends AbstractFormInputDirective {
  private elRef = inject(ElementRef);

  @Input() placeholder: string;
  @Input() autofocus: boolean;
  @Input()
  multiple = false;
  @HostBinding('class.textarea')
  @Input()
  textarea = false;
  @Input() addTooltipKey = 'INPUT.ADD_TOOLTIP';
  @Input() removeTooltipKey = 'INPUT.REMOVE_TOOLTIP';
  @Input() type: 'text' | 'number' | 'password' = 'text';

  items: InternalValue[] = [{ id: 0, value: '' }];
  focused: number;
  @HostBinding('class.disabled') get isDisabled() {
    return this.control.disabled;
  }

  @HostBinding('class.vitamui-float')
  get labelFloat() {
    return !!this.items && !this.isEmpty(this.items[0]?.value);
  }

  @HostListener('click', ['$event.target'])
  onClick(target: EventTarget) {
    const el = target as Element;
    if (!['INPUT', 'TEXTAREA', 'BUTTON', 'I'].includes(el.tagName)) {
      const input = el.querySelector('input, textarea') as HTMLElement;
      if (input) {
        input.focus();
      } else {
        this.elRef.nativeElement.querySelector('input:first-of-type, textarea:first-of-type').focus();
      }
    }
  }

  constructor() {
    const injector = inject(Injector);

    super(injector);
  }

  override writeValue(values?: InternalValue['value'] | InternalValue['value'][]) {
    this.items = (Array.isArray(values) ? (values && values.length ? values : ['']) : [values || '']).map((v, i) => ({
      id: i,
      value: v.toString(),
    }));
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

  onValueChange(value: string, i: number) {
    this.items[i].value = value;
    this.onChange(this.multiple ? this.items.map((v) => v.value).filter((v) => !!v) : this.items[0].value);
  }

  onFocus(i: number) {
    this.focused = i;
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

  protected readonly Validators = Validators;
}
