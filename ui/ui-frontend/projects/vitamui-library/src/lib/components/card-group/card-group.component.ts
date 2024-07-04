/* eslint-disable @angular-eslint/component-selector */
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CardComponent } from '../card/card.component';
import { NgFor } from '@angular/common';

@Component({
  selector: 'vitamui-card-group',
  templateUrl: './card-group.component.html',
  styleUrls: ['./card-group.component.scss'],
  standalone: true,
  imports: [NgFor, CardComponent],
})
export class CardGroupComponent {
  constructor() {}

  @Input()
  values: Set<string>;

  @Input()
  showAction: boolean;

  @Output()
  valuesChange = new EventEmitter<Set<string>>();

  remove($event: string) {
    this.values.delete($event);
    this.valuesChange.emit(this.values);
  }
}
