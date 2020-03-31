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

  colorForm: FormGroup;

  colors: {[colorId: string]: string} = {
    'vitamui-primary': '',
    'vitamui-secondary': ''
  };

  onTouched: () => void;

  validator: ValidatorFn = Validators.pattern(/#([0-9A-Fa-f]{6})/);

  constructor(private formBuilder: FormBuilder, private themeService: ThemeService) {
    this.colorForm = this.formBuilder.group({
      primary: ['', this.validator],
      secondary: ['', this.validator]
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


  handleValueChanges() {
    this.colorForm.valueChanges.subscribe((colors) => {

      // Force color hex to start with '#'
      if (!colors.primary.startsWith('#')) {
        const newPrimary: string = '#' + colors.primary;
        const oldSecondary: string = colors.secondary;
        this.colorForm.setValue({primary: newPrimary, secondary: oldSecondary});
      }

      if (!this.colorForm.value.secondary.startsWith('#')) {
        const newSecondary: string = '#' + colors.secondary;
        const oldPrimary: string = colors.primary;
        this.colorForm.setValue({primary: oldPrimary, secondary: newSecondary});
      }

      if (this.colorForm.invalid || this.colorForm.pending) {
        return;
      }

      this.colors = {
        'vitamui-primary': this.colorForm.value.primary,
        'vitamui-secondary': this.colorForm.value.secondary
      };

      // If form is valid, overload local theme for preview
      this.overloadLocalTheme();
    });

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
    this.handleValueChanges();
  }
}
