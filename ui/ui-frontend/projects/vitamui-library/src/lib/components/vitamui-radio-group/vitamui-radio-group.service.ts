import {EventEmitter, Injectable} from '@angular/core';
import {VitamUIRadioComponent} from '../vitamui-radio/vitamui-radio.component';

@Injectable()
export class VitamUIRadioGroupService {

  constructor() {
  }

  resetAll = new EventEmitter<VitamUIRadioComponent>();

  random = Math.random();
}
