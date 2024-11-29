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
import { Component, forwardRef, HostBinding, Injector, Input, OnInit } from '@angular/core';
import {
  AbstractControl,
  ControlValueAccessor,
  FormControl,
  FormsModule,
  NG_VALUE_ACCESSOR,
  NgControl,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { MatMenuModule } from '@angular/material/menu';
import { LowerCasePipe, NgIf } from '@angular/common';
import { FormErrorsComponent } from '../form-errors/form-errors.component';

export const SEARCH_WITH_TYPE_SELECTOR_VALUE_ACCESSOR: any = {
  provide: NG_VALUE_ACCESSOR,
  useExisting: forwardRef(() => SearchWithTypeSelectorComponent),
  multi: true,
};

export interface SearchType {
  label: string;
  value: string;
}

export interface SearchWithTypeSelectorValue {
  value: string;
  type: SearchType;
}

@Component({
  selector: 'vitamui-search-with-type-selector',
  standalone: true,
  imports: [ReactiveFormsModule, MatMenuModule, FormsModule, LowerCasePipe, NgIf, FormErrorsComponent],
  templateUrl: './search-with-type-selector.component.html',
  styleUrl: './search-with-type-selector.component.scss',
  providers: [SEARCH_WITH_TYPE_SELECTOR_VALUE_ACCESSOR],
})
export class SearchWithTypeSelectorComponent implements ControlValueAccessor, OnInit {
  @Input({ required: true }) placeholder: string;
  @Input({ required: true }) types: SearchType[];
  @Input() errorMessageMap: { [p: string]: string };

  protected control: FormControl<SearchWithTypeSelectorValue>;
  protected isRequired = false;

  @HostBinding('class.vitamui-float')
  get labelFloat() {
    return !!this.control?.value?.value;
  }

  constructor(private injector: Injector) {}

  ngOnInit() {
    const ngControl: NgControl = this.injector.get(NgControl);
    this.control = ngControl.control as FormControl<SearchWithTypeSelectorValue>;

    this.isRequired = this.control.hasValidator(Validators.required);
    this.applyValidatorsToInputValue();
  }

  // As the component contains a "type" and a "value", we wrap "required" validator to apply it on the "value" and not the object containing both the "type" and the "value"
  private applyValidatorsToInputValue() {
    if (this.control.hasValidator(Validators.required)) {
      this.control.removeValidators(Validators.required);
      this.control.addValidators(
        (control: AbstractControl<SearchWithTypeSelectorValue>): ValidationErrors =>
          Validators.required({ ...control, value: control.value?.value } as AbstractControl),
      );
    }
  }

  onChange = (_: any) => {};
  onTouched = () => {};

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  writeValue(_searchWithTypeSelectorValue?: SearchWithTypeSelectorValue): void {}

  selectType(type: SearchType) {
    this.onChange({ ...this.control.value, type: type });
  }

  inputValueChange(value: string) {
    this.onChange({ ...this.control.value, value: value });
  }
}
