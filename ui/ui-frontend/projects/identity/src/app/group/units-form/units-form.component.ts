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
/* eslint-disable no-magic-numbers, max-lines, max-classes-per-file */
/* eslint-disable @typescript-eslint/no-use-before-define */

import { Component, forwardRef, Input, OnInit } from '@angular/core';
import { AbstractControl, ControlValueAccessor, FormControl, NG_VALUE_ACCESSOR, ValidationErrors } from '@angular/forms';
import { GroupValidators } from '../group.validators';

export const UNITS_FORM_VALUE_ACCESSOR: any = {
  provide: NG_VALUE_ACCESSOR,
  useExisting: forwardRef(() => UnitsFormComponent),
  multi: true,
};

/**
 * Wrapper component for units form fields
 */
@Component({
  selector: 'app-units-form',
  templateUrl: './units-form.component.html',
  styleUrls: ['./units-form.component.scss'],
  providers: [UNITS_FORM_VALUE_ACCESSOR],
})
export class UnitsFormComponent implements ControlValueAccessor, OnInit {
  units: string[] = [];

  removedUnits: string[] = [];

  unitControl: FormControl;

  @Input() customer: string;

  constructor(private groupValidators: GroupValidators) {}

  onChange = (_: any) => {};
  onTouched = () => {};

  writeValue(value: string[]) {
    this.units = value || [];
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  ngOnInit(): void {
    this.unitControl = new FormControl(
      '',
      [this.unitAlreadyAdd.bind(this)],
      this.groupValidators.unitExists(this.customer, this.removedUnits),
    );
  }

  /**
   * Add units
   */
  add() {
    let val = this.unitControl.value;
    if (!val || this.unitControl.pending || this.unitControl.invalid) {
      return;
    }
    this.unitControl.reset();
    val = val.trim();
    const elementToRemoveIndex = this.removedUnits.indexOf(val);
    if (elementToRemoveIndex > -1) {
      this.removedUnits.splice(elementToRemoveIndex, 1);
    }
    this.units.push(val);
    this.sortUnits();
    this.onChange(this.units);
  }

  /**
   * Remove unit
   */
  remove(unitToRemove: string) {
    this.removedUnits.push(unitToRemove);
    this.units = this.units.filter((unit) => unit !== unitToRemove);
    this.onChange(this.units);
  }

  /**
   * Sort by alphebatic order
   */
  sortUnits() {
    this.units.sort((a: any, b: any) => a.localeCompare(b));
  }

  unitAlreadyAdd(control: AbstractControl): ValidationErrors {
    const unit: string = control.value;
    if (unit && this.units.map((u) => u.toLocaleLowerCase()).includes(unit.trim().toLocaleLowerCase())) {
      return {
        unitAlreadyAdd: true,
      };
    }

    return null;
  }
}
