import {Component, forwardRef, Input, OnInit} from '@angular/core';

import {
  ControlValueAccessor,
  FormBuilder,
  FormGroup,
  NG_VALUE_ACCESSOR,
  ValidatorFn,
  Validators
} from '@angular/forms';
import {ThemeService} from 'ui-frontend-common';

export const COLORS_INPUT_ACCESSOR: any = {
  provide: NG_VALUE_ACCESSOR,
  useExisting: forwardRef(() => CustomerColorsInputComponent),
  multi: true
};

@Component({
  selector: 'app-customer-colors-input',
  templateUrl: './customer-colors-input.component.html',
  styleUrls: ['./customer-colors-input.component.scss'],
  providers: [COLORS_INPUT_ACCESSOR]
})
export class CustomerColorsInputComponent implements ControlValueAccessor, OnInit {


  @Input() placeholder: string;
  @Input() spinnerDiameter = 25;

  // css selector to overload color theme for preview (default = only color circles around inputs)
  @Input() overloadSelector = '.field-color-preview';

  @Input() disabled = false;

  colorForm: FormGroup;

  colors: {[colorId: string]: string} = {
    'vitamui-primary': '',
    'vitamui-secondary': ''
  };

  onTouched: () => void;

  hexValidator: ValidatorFn = Validators.pattern(/#([0-9A-Fa-f]{6})/);

  constructor(private formBuilder: FormBuilder, private themeService: ThemeService) {
    this.colorForm = this.formBuilder.group({
      primary: ['', this.hexValidator],
      secondary: ['', this.hexValidator]
    });
  }

  get value(): {[key: string]: string} {
    return this.colors;
  }

  writeValue(colors: {primary: string, secondary: string}) {
    this.colorForm.setValue(colors);
  }

  registerOnChange(fn: (colors: {[key: string]: string}) => void) {
    this.colorForm.valueChanges.subscribe(fn);
  }

  registerOnTouched(fn: () => void) {
    this.onTouched = fn;
  }

  handleValueChanges(colors: {primary: string, secondary: string}) {

    if (this.colorForm.invalid || this.colorForm.pending) {
      return;
    }

    this.colors = {
      'vitamui-primary': colors.primary,
      'vitamui-secondary': colors.secondary
    };

    // If form is valid, overload local theme for preview
    this.overloadLocalTheme();
  }

  overloadLocalTheme() {
    const newTheme = this.themeService.getThemeColors(this.colors);
    const selector: HTMLElement = document.querySelector(this.overloadSelector);
    for (const key in newTheme) {
      if (newTheme.hasOwnProperty(key)) {
        selector.style.setProperty('--' + key, newTheme[key]);
      }
    }
  }

  ngOnInit(): void {
    this.colorForm.valueChanges.subscribe((colors) => {
      this.handleValueChanges(colors);
    });
  }


  handlePicker(key: string, pickerValue: string) {

    // Avoir 3 chars hex to become 6 chars (ex. #123 becoming instantly #112233...)
    let inputValue: string = this.colorForm.get(key).value;
    inputValue = inputValue.toUpperCase();
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

    if (key === 'primary') {
      this.colorForm.setValue({
        primary: '#' + pickerValue,
        secondary: this.colorForm.value.secondary
      });

    } else if (key === 'secondary') {
      this.colorForm.setValue({
        primary: this.colorForm.value.primary,
        secondary: '#' + pickerValue
      });
    }

  }

  forceHex() {
    if (! this.colorForm.value.primary.startsWith('#')) {
      this.colorForm.setValue({
        primary: '#' + this.colorForm.value.primary,
        secondary: this.colorForm.value.secondary
      });

    } else if (! this.colorForm.value.secondary.startsWith('#')) {
      this.colorForm.setValue({
        primary: this.colorForm.value.primary,
        secondary: '#' + this.colorForm.value.secondary
      });
    }
  }
}
