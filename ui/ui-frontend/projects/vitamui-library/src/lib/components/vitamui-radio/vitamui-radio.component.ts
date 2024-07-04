/* eslint-disable @angular-eslint/component-selector */
import { Component, Input, OnInit } from '@angular/core';

import { VitamUIRadioGroupService } from '../vitamui-radio-group/vitamui-radio-group.service';
import { NgClass } from '@angular/common';

@Component({
  selector: 'vitamui-radio',
  templateUrl: './vitamui-radio.component.html',
  styleUrls: ['./vitamui-radio.component.scss'],
  standalone: true,
  imports: [NgClass],
})
export class VitamUIRadioComponent implements OnInit {
  @Input()
  value: string;

  @Input()
  label: string;

  @Input()
  checked: boolean;

  constructor(private radioGroupService: VitamUIRadioGroupService) {}

  ngOnInit() {
    this.checked = this.checked !== undefined;
  }

  changed() {
    this.radioGroupService.resetAll.emit(this);
  }
}
