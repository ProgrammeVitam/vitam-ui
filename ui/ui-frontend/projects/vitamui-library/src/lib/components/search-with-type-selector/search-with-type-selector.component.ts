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
import { Component, EventEmitter, forwardRef, HostBinding, HostListener, Input, OnInit, Output } from '@angular/core';
import { AbstractControl, FormsModule, NG_VALUE_ACCESSOR, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { MatMenuModule } from '@angular/material/menu';
import { LowerCasePipe, NgIf } from '@angular/common';
import { FormErrorsComponent } from '../form-errors/form-errors.component';
import { AbstractFormInputDirective } from '../abstract-form-input.directive';

export const SEARCH_WITH_TYPE_SELECTOR_VALUE_ACCESSOR: any = {
  provide: NG_VALUE_ACCESSOR,
  useExisting: forwardRef(() => SearchWithTypeSelectorComponent),
  multi: true,
};

export interface SearchType {
  label: string;
  value: string;
  disabled?: boolean;
}

export interface SearchWithTypeSelectorValue {
  value: string;
  type: SearchType;
}

@Component({
  selector: 'vitamui-search-with-type-selector',
  imports: [ReactiveFormsModule, MatMenuModule, FormsModule, LowerCasePipe, NgIf, FormErrorsComponent],
  templateUrl: './search-with-type-selector.component.html',
  styleUrl: './search-with-type-selector.component.scss',
  providers: [
    SEARCH_WITH_TYPE_SELECTOR_VALUE_ACCESSOR,
    {
      provide: AbstractFormInputDirective,
      useExisting: forwardRef(() => SearchWithTypeSelectorComponent),
    }, // This provider is required in order for the FormFieldValueWrapperComponent to be able to find a reference to that component
  ],
})
export class SearchWithTypeSelectorComponent extends AbstractFormInputDirective implements OnInit {
  @Input({ required: true }) placeholder: string;
  @Input({ required: true }) types: SearchType[];
  @Input() errorMessageMap: { [p: string]: string };

  _selectedType: SearchType;
  @Input() set selectedType(type: SearchType) {
    this.selectType(type);
  }

  @Output() selectedTypeChange = new EventEmitter<SearchType>();

  protected isRequired = false;

  @HostBinding('class.vitamui-float')
  get labelFloat() {
    return !!this.control?.value?.value;
  }

  @HostListener('keydown.enter', ['$event'])
  onEnter(event: KeyboardEvent) {
    if ((event.target as HTMLElement).tagName !== 'INPUT') {
      event.stopPropagation();
    }
  }

  ngOnInit() {
    super.ngOnInit();
    this.afterControlSet();
  }

  afterControlSet() {
    this.isRequired = this.control.hasValidator(Validators.required);
    if (this._selectedType) this.selectType(this._selectedType);
    this.applyValidatorsToInputValue();
    this.control.valueChanges.subscribe((value: SearchWithTypeSelectorValue) => ((this.control as any).resetValue = { type: value?.type })); // resetValue is a metadata that is used to keep the selected type when we reset the field from a FormFieldValueWrapperComponent
  }

  // As the component contains a "type" and a "value", we wrap "required" validator to apply it on the "value" and not the object containing both the "type" and the "value"
  private applyValidatorsToInputValue() {
    if (this.control.hasValidator(Validators.required)) {
      this.control.removeValidators(Validators.required);
      this.control.addValidators(
        (control: AbstractControl<SearchWithTypeSelectorValue>): ValidationErrors =>
          Validators.required({
            ...control,
            value: control.value?.value,
          } as AbstractControl),
      );
    }
  }

  selectType(selectedType: SearchType) {
    if (this.control) {
      this.control.setValue({ ...this.control.value, type: selectedType });
      this.control.markAsDirty();
      this.onChange(this.control.value);
    }
    this._selectedType = selectedType;
    this.selectedTypeChange.emit(selectedType);
  }

  inputValueChange(value: string) {
    this.control.setValue({ type: this.types[0], ...this.control.value, value: value });
    this.control.markAsDirty();
    this.onChange(this.control.value);
  }
}
