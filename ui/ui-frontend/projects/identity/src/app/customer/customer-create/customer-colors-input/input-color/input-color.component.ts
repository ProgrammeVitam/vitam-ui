import { Component, Input, OnInit, ViewChild } from '@angular/core';
import { FormControl } from '@angular/forms';
import { ColorPickerDirective } from 'ngx-color-picker';
import { hexToRgb, rgbToHsl } from 'ui-frontend-common';

import { ColorErrorEnum } from './color-error.enum';

@Component({
  selector: 'app-input-color',
  templateUrl: './input-color.component.html',
  styleUrls: ['./input-color.component.scss'],
})
export class InputColorComponent implements OnInit {
  @Input() placeholder: string;
  @Input() disabled: boolean;
  @Input() colorInput: FormControl;
  @Input() checkWarning: boolean;

  public color: string;

  @ViewChild('colorPickerInput', { read: ColorPickerDirective, static: false })
  private colorPicker: ColorPickerDirective;
  public colorErrorEnum: typeof ColorErrorEnum = ColorErrorEnum;
  public colorError: ColorErrorEnum = ColorErrorEnum.NONE;

  constructor() { }

  public ngOnInit(): void {
    this.color = this.colorInput.value;
    this.colorInput.valueChanges.subscribe((color: string) => {
      this.color = color;
      if (this.checkWarning) {
        this.checkColor500(color);
      }
    });
  }

  checkColor500(color: string) {
    this.colorError = ColorErrorEnum.NONE;
    const rgbValue = hexToRgb(color);
    if (rgbValue) {
      const hslValue = rgbToHsl(rgbValue);
      if (hslValue) {
        if (hslValue.l > 60) {
          this.colorError = ColorErrorEnum.COLOR_TOO_LIGHT;
        } else if (hslValue.l < 40) {
          this.colorError = ColorErrorEnum.COLOR_TOO_DARK;
        }
      } else {
        this.colorError = ColorErrorEnum.COLOR_INVALID;
      }
    } else {
      this.colorError = ColorErrorEnum.COLOR_INVALID;
    }
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
        if (
          inputValue.charAt(i) !== pickerValue.charAt(2 * i) ||
          inputValue.charAt(i) !== pickerValue.charAt(2 * i + 1)
        ) {
          continue;
        }
        return;
      }
    }
    this.colorInput.setValue('#' + pickerValue);
  }
}
