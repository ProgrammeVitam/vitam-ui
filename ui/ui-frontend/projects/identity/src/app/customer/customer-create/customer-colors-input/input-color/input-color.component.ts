import {Component, Input, OnInit, ViewChild} from '@angular/core';
import {FormControl} from '@angular/forms';
import {ColorPickerDirective} from 'ngx-color-picker';

@Component({
  selector: 'app-input-color',
  templateUrl: './input-color.component.html',
  styleUrls: ['./input-color.component.scss'],
})
export class InputColorComponent implements OnInit {

  @Input() placeholder: string;
  @Input() disabled: boolean;

  @Input() colorInput: FormControl;

  public color: string;

  @ViewChild('colorPickerInput', {read: ColorPickerDirective, static: false})
  private colorPicker: ColorPickerDirective;

  constructor() {}

  public ngOnInit(): void {

    this.color = this.colorInput.value;
    this.colorInput.valueChanges.subscribe((color: string) => {
      this.color = color;
    });
  }

  public onPickerOpen(): void {
    if (this.disabled) {
      this.colorPicker.closeDialog();
    }
  }

  public openPicker(): void {
    if ( ! this.disabled) {
      this.colorPicker.openDialog();
    }
  }

  public forceHex(): void {
    if (! this.colorInput.value.startsWith('#')) {
      this.colorInput.setValue('#' + this.colorInput.value);
    }
  }

  public handlePicker(pickerValue: string): void {

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
