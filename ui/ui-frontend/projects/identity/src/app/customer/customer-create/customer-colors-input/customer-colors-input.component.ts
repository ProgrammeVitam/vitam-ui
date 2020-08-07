import {Component, forwardRef, Input, OnInit} from '@angular/core';
import {ControlValueAccessor, FormControl, FormGroup, NG_VALUE_ACCESSOR, ValidatorFn, Validators} from '@angular/forms';
import {ThemeService} from 'ui-frontend-common';

export const THEME_COLORS_INPUT_ACCESSOR = {
  provide: NG_VALUE_ACCESSOR,
  useExisting: forwardRef(() => CustomerColorsInputComponent),
  multi: true
};

@Component({
  selector: 'app-customer-colors-input',
  templateUrl: './customer-colors-input.component.html',
  styleUrls: ['./customer-colors-input.component.scss'],
  providers: [THEME_COLORS_INPUT_ACCESSOR]
})
export class CustomerColorsInputComponent implements ControlValueAccessor, OnInit {

  @Input() themeOverloadSelector: string;

  @Input() disabled: boolean;

  public colors: { [colorId: string]: string };

  public colorForm: FormGroup;

  public onTouched: () => void;

  private hexValidator: ValidatorFn = Validators.pattern(/#([0-9A-Fa-f]{6})/);

  public baseColors: {[colorId: string]: string};
  public variations: { [colorId: string]: string[] } = {};
  public baseColorsNames: string[];

  constructor(private themeService: ThemeService) {
    this.baseColors = this.themeService.getBaseColors();
    this.baseColorsNames = Object.keys(this.baseColors);

    // Build dynamic formgroup and variations names array
    this.colors = this.themeService.getThemeColors();

    const group: {[k: string]: FormControl} = { };
    for (const name of Object.keys(this.baseColors)) {

      group[name] = new FormControl(
        {value: this.colors[name], disabled: this.disabled},
        [this.hexValidator, Validators.required]
      );

      group[name].valueChanges.subscribe((color: string) => {
        this.handleChange(name, color);
      });

      this.variations[name] = this.themeService.getVariationColorsNames(name);
    }
    this.colorForm = new FormGroup(group);
  }


  public ngOnInit(): void {

  }

  public handleChange(name: string, color: string): void {
    const input = this.colorForm.get(name);

    if (input.invalid || input.pending) {
      return;
    }

    const newColors: {[colorId: string]: string} = {};
    for (const key of Object.keys(this.baseColors)) {
      if (key === name) {
        newColors[key] = color;
      } else {
        newColors[key] = this.colors[key];
      }
    }
    this.colors = newColors;
    this.themeService.overrideTheme(this.colors, this.themeOverloadSelector);
  }

  public registerOnChange(fn: any): void {
    this.colorForm.valueChanges.subscribe(fn);
  }

  public registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  public writeValue(value: {[colorId: string]: string}): void {
      this.colorForm.setValue(value, {emitEvent: true});
  }

}
