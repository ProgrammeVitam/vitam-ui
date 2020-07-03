/* tslint:disable:component-selector */
import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';

@Component({
  selector: 'vitamui-card',
  templateUrl: './card.component.html',
  styleUrls: ['./card.component.scss']
})
export class CardComponent implements OnInit {

  @Input()
  value: string;

  @Input()
  showAction = true;

  @Output()
  buttonClick = new EventEmitter<any>();

  constructor() {
  }

  ngOnInit() {
  }

  buttonClicked() {
    this.buttonClick.emit();
  }

}
