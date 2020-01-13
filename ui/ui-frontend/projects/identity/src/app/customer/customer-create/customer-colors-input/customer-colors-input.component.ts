import { ENTER } from '@angular/cdk/keycodes';
import { Component, forwardRef, Input } from '@angular/core';
import { ControlValueAccessor, FormBuilder, FormGroup, NG_VALUE_ACCESSOR, Validators } from '@angular/forms';

class Color {
  colorName: string;
  colorValue: string;
}

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

  colors: Color[] = [];
  colorForm: FormGroup;
  separatorKeysCodes = [ENTER];

  onChange: (_: any) => void;
  onTouched: () => void;

  constructor(private formBuilder: FormBuilder) {
    this.colorForm = this.formBuilder.group({
      colorName: [null, Validators.required],
      colorValue: [null, Validators.required]
    });
  }

  writeValue(colors: Color[]) {
    this.colors = (colors || []).slice();
  }

  registerOnChange(fn: (_: any) => void) {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void) {
    this.onTouched = fn;
  }

  add(): void {
    if (this.colorForm.invalid || this.colorForm.pending) { return; }
    const color = this.colorForm.value;
    if (this.colors.includes(color)) { return; }
    this.colors.push(color);
    this.onChange(this.colors);
    this.colorForm.reset();
  }

  remove(color: Color): void {
    const index = this.colors.indexOf(color);

    if (index >= 0) {
      this.colors.splice(index, 1);
      this.onChange(this.colors);
    }
  }

  buttonAddDisabled(): boolean {
    return this.colorForm.pending || this.colorForm.invalid || this.colorExists;
  }

  get colorExists(): boolean {
    return this.colors.includes((this.colorForm.value || ''));
  }

}
