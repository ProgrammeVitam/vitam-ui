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
import { Component, Input, OnInit, ViewChild } from '@angular/core';
import { AbstractControl, FormControl, ValidationErrors, ValidatorFn } from '@angular/forms';
import { ColorPickerDirective } from 'ngx-color-picker';
import { hexToRgb, rgbToHsl, FormControlWarn } from 'vitamui-library';

@Component({
  selector: 'app-input-color',
  templateUrl: './input-color.component.html',
  styleUrls: ['./input-color.component.scss'],
  standalone: false,
})
export class InputColorComponent implements OnInit {
  @Input() placeholder: string;
  @Input() disabled: boolean;
  @Input() colorInput: FormControl;
  @Input() checkWarning: boolean;

  public color: string;

  @ViewChild('colorPickerInput', { read: ColorPickerDirective, static: false })
  private colorPicker: ColorPickerDirective;

  constructor() {}

  public ngOnInit(): void {
    this.color = this.colorInput.value;

    if (this.checkWarning) {
      this.colorInput.addValidators(this.checkColor500());
    }

    this.colorInput.valueChanges.subscribe((color: string) => {
      this.colorInput.markAsTouched();
      this.color = color;
    });
  }

  private checkColor500(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const color = control.value;

      const warnControl = control as FormControlWarn;
      warnControl.warnings = {};

      const rgbValue = hexToRgb(color);
      if (rgbValue) {
        const hslValue = rgbToHsl(rgbValue);
        if (hslValue) {
          if (hslValue.l > 60) {
            warnControl.warnings = { colorTooLight: true };
          } else if (hslValue.l < 40) {
            warnControl.warnings = { colorTooDark: true };
          }
          return null;
        }
      }
      return { colorInvalid: true };
    };
  }

  public onPickerOpen(): void {
    if (this.disabled) {
      this.colorPicker.closeDialog();
    }
  }

  public openPicker(): void {
    if (!this.disabled) {
      this.colorPicker.openDialog();
    }
  }

  public forceHex(): void {
    if (!this.colorInput.value.startsWith('#')) {
      this.colorInput.setValue('#' + this.colorInput.value);
    }
  }

  public handlePicker(pickerValue: string): void {
    let inputValue: string = this.colorInput.value.toUpperCase();
    pickerValue = pickerValue.toUpperCase();

    if (inputValue.startsWith('#')) {
      inputValue = inputValue.substring(1);
    }
    if (pickerValue.startsWith('#')) {
      pickerValue = pickerValue.substring(1);
    }

    if (inputValue.length === 3 && pickerValue.length === 6) {
      for (let i = 0; i < 3; i++) {
        if (inputValue.charAt(i) !== pickerValue.charAt(2 * i) || inputValue.charAt(i) !== pickerValue.charAt(2 * i + 1)) {
          continue;
        }
        return;
      }
    }
    this.colorInput.setValue('#' + pickerValue);
  }
}
