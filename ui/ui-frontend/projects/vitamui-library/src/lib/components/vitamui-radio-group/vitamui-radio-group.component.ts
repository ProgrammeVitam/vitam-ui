/* tslint:disable:no-use-before-declare component-selector */
import {AfterContentInit, Component, ContentChildren, forwardRef, Input, OnInit, QueryList, Self} from '@angular/core';
import {NG_VALUE_ACCESSOR} from '@angular/forms';

import {VitamUIRadioComponent} from '../vitamui-radio/vitamui-radio.component';
import {VitamUIRadioGroupService} from './vitamui-radio-group.service';

export const RADIO_GROUP_VALUE_ACCESSOR: any = {
  provide: NG_VALUE_ACCESSOR,
  useExisting: forwardRef(() => VitamUIRadioGroupComponent),
  multi: true
};

@Component({
  selector: 'vitamui-radio-group',
  templateUrl: './vitamui-radio-group.component.html',
  styleUrls: ['./vitamui-radio-group.component.scss'],
  providers: [
    RADIO_GROUP_VALUE_ACCESSOR,
    VitamUIRadioGroupService
  ]
})
export class VitamUIRadioGroupComponent implements OnInit, AfterContentInit {

  constructor(@Self() private radioGroupService: VitamUIRadioGroupService) {
  }

  @ContentChildren(VitamUIRadioComponent) private radios: QueryList<VitamUIRadioComponent>;

  @Input()
  label: string;

  @Input()
  required: boolean;

  value: any;

  ngOnDestroy: () => void;

  onChange = (_: any) => {
  }
  onTouched = () => {
  }

  ngOnInit(): void {
    this.required = (this.required === undefined) ? false : true;
    const subscription = this.radioGroupService.resetAll.subscribe((elem: VitamUIRadioComponent) => {
      this.radios.forEach((radioButton: VitamUIRadioComponent) => {
        radioButton.checked = false;
      });
      elem.checked = true;
      this.value = elem.value;
      this.onChange(this.value);
    });

    this.ngOnDestroy = () => {
      subscription.unsubscribe();
    };
  }

  ngAfterContentInit(): void {
    this.radios.filter(radio => radio.checked).forEach(radio => {
      this.value = radio.value;
      this.onChange(this.value);
    });

    this.radios.filter(radio => radio.value === this.value).forEach(radio => radio.checked = true);
  }

  writeValue(value: any) {
    this.value = value;
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }


}
