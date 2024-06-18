/* eslint-disable @angular-eslint/component-selector */
import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'vitamui-card',
  templateUrl: './card.component.html',
  styleUrls: ['./card.component.scss'],
})
export class CardComponent {
  @Input()
  value: string;

  @Input()
  showAction = true;

  @Output()
  buttonClick = new EventEmitter<any>();

  buttonClicked() {
    this.buttonClick.emit();
  }
}
