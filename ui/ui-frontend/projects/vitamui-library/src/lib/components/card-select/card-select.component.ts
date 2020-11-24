/* tslint:disable:no-use-before-declare component-selector */
import {Component, forwardRef, Input} from '@angular/core';
import {NG_VALUE_ACCESSOR} from '@angular/forms';

export const CARD_SELECT_VALUE_ACCESSOR: any = {
  provide: NG_VALUE_ACCESSOR,
  useExisting: forwardRef(() => CardSelectComponent),
  multi: true
};

@Component({
  selector: 'vitamui-card-select',
  templateUrl: './card-select.component.html',
  styleUrls: ['./card-select.component.scss'],
  providers: [CARD_SELECT_VALUE_ACCESSOR]
})
export class CardSelectComponent {

  @Input()
  placeholder: any;

  @Input()
  label = 'Add';

  @Input()
  disabled = false;

  @Input()
  size: number;

  values = new Set<string>();

  addElement(input: any) {
    if (this.size !== undefined && this.values.size >= this.size) {
      return;
    }
    if (input.value != null && input.value !== '') {
      this.values.add((input.value as string).trim());
      input.writeValue(null);
      this.onChange(Array.from(this.values));
    }
  }

  changeValue($event: Set<string>) {
    this.values = $event;
    this.onChange(Array.from(this.values));
  }

  onChange = (_: any) => {
  }
  onTouched = () => {
  }

  writeValue(value: Array<string>) {
    this.values = new Set((value == null) ? new Array<string>() : value);
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }


}
