import {Component, forwardRef, Input} from '@angular/core';
import {ControlValueAccessor, FormBuilder, FormGroup, NG_VALUE_ACCESSOR, ValidatorFn, Validators} from '@angular/forms';
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
export class CustomerColorsInputComponent implements ControlValueAccessor {


  @Input() placeholder: string;
  @Input() spinnerDiameter = 25;

  colorForm: FormGroup;

  colors: {[key: string]: string};

  onChange: (colors: {[key: string]: string}) => void;
  onTouched: () => void;

  validator: ValidatorFn;

  constructor(private formBuilder: FormBuilder, private themeService: ThemeService) {

    this.validator = Validators.pattern(/#([0-9A-Fa-f]{6})/);
    this.colorForm = this.formBuilder.group({
      primary: [themeService.themeColors['vitamui-primary'], this.validator],
      secondary: [themeService.themeColors['vitamui-secondary'], this.validator]
    });

  }

  writeValue(colors: {[key: string]: string}) {
    this.colors = {
      primary: colors['vitamui-primary'],
      secondary: colors['vitamui-secondary']
    };
  }

  registerOnChange(fn: (colors: {[key: string]: string}) => void) {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void) {
    this.onTouched = fn;
  }


  handleValueChange() {
    // Force color hex to start with '#'
    if ( ! this.colorForm.value.primary.startsWith('#') ) {
      const newPrimary: string = '#' + this.colorForm.value.primary;
      const oldSecondary: string = this.colorForm.value.secondary;
      this.colorForm.setValue({primary: newPrimary, secondary: oldSecondary});
    }

    if ( ! this.colorForm.value.secondary.startsWith('#') ) {
      const newSecondary: string = '#' + this.colorForm.value.secondary;
      const oldPrimary: string = this.colorForm.value.primary;
      this.colorForm.setValue({primary: oldPrimary, secondary: newSecondary});
    }

    if (this.colorForm.invalid || this.colorForm.pending) {
      return;
    }

    this.colors = {
      'vitamui-primary': this.colorForm.value.primary,
      'vitamui-secondary': this.colorForm.value.secondary
    };

    // propagate changes
    this.onChange(this.colors);

    // If form is valid, overload local theme for preview
    this.overloadLocalTheme();

  }

  overloadLocalTheme() {

    this.themeService.refresh(this.colorForm.value.primary, this.colorForm.value.secondary);

    const selector: HTMLElement = document.querySelector('div.customer-colors-input');
    for (const key in this.themeService.themeColors) {
      if (this.themeService.themeColors.hasOwnProperty(key)) {
        selector.style.setProperty('--' + key, this.themeService.themeColors[key]);
      }
    }
  }
}
