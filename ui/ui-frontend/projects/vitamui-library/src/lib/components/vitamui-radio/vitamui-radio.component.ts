import { Component, Input, OnInit, Host } from '@angular/core';

import { VitamUIRadioGroupService } from '../vitamui-radio-group/vitamui-radio-group.service';

@Component({
  selector: 'vitamui-radio',
  templateUrl: './vitamui-radio.component.html',
  styleUrls: ['./vitamui-radio.component.scss']
})
export class VitamUIRadioComponent implements OnInit {

  @Input()
  value: string;

  @Input()
  label: string;

  @Input()
  checked: boolean;

  constructor(@Host() private radioGroupService: VitamUIRadioGroupService) { }

  ngOnInit() {
    this.checked = (this.checked == undefined) ? false : true;
  }

  changed() {
    this.radioGroupService.resetAll.emit(this);
  }

}
