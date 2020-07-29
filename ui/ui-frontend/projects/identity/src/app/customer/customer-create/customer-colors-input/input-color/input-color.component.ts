import {Component, forwardRef, Input, OnInit, ViewChild} from '@angular/core';
import {ControlValueAccessor, FormControl, NG_VALUE_ACCESSOR, ValidatorFn, Validators} from '@angular/forms';
import {ColorPickerDirective} from 'ngx-color-picker';

export const COLOR_INPUT_ACCESSOR: any = {
  provide: NG_VALUE_ACCESSOR,
  useExisting: forwardRef(() => InputColorComponent),
  multi: true
};
@Component({
  selector: 'app-input-color',
  templateUrl: './input-color.component.html',
  styleUrls: ['./input-color.component.scss'],
  providers: [COLOR_INPUT_ACCESSOR]
})
export class InputColorComponent implements ControlValueAccessor, OnInit {

  @Input() placeholder: string;
  @Input() disabled: boolean;

  @Input() colorVariationsClassNames: string[];

  hexValidator: ValidatorFn = Validators.pattern(/#([0-9A-Fa-f]{6})/);

  color: string;

  colorInput: FormControl;

  @ViewChild('colorPickerInput', {read: ColorPickerDirective, static: false})
  colorPicker: ColorPickerDirective;


  onTouched: () => void;

  constructor() {
    this.colorInput = new FormControl('', [this.hexValidator, Validators.required]);
    this.colorInput.valueChanges.subscribe((color: string) => {
      this.color = color;
    });
  }

  ngOnInit() {

  }

  registerOnChange(fn: any): void {
    this.colorInput.valueChanges.subscribe(fn);
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  writeValue(color: string): void {
    this.colorInput.setValue(color);
  }


  onPickerOpen() {
    if (this.disabled) {
      this.colorPicker.closeDialog();
    }
  }

  openPicker() {
    if ( ! this.disabled) {
      this.colorPicker.openDialog();
    }
  }

  forceHex() {
    if (! this.colorInput.value.startsWith('#')) {
      this.colorInput.setValue('#' + this.colorInput.value);
    }
  }

  handlePicker(pickerValue: string) {

    // Avoid 3 chars hex to become 6 chars (ex. #123 becoming instantly #112233...)
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
