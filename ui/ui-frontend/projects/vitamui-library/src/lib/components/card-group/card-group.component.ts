/* tslint:disable:component-selector */
import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';

@Component({
  selector: 'vitamui-card-group',
  templateUrl: './card-group.component.html',
  styleUrls: ['./card-group.component.scss']
})
export class CardGroupComponent implements OnInit {

  constructor() {
  }

  @Input()
  values: Set<string>;

  @Input()
  showAction: boolean;

  @Output()
  valuesChange = new EventEmitter<Set<string>>();

  ngOnInit() {
  }

  remove($event: string) {
    this.values.delete($event);
    this.valuesChange.emit(this.values);
  }

}
