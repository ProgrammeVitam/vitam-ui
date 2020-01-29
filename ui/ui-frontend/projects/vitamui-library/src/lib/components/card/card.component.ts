import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'vitamui-card',
  templateUrl: './card.component.html',
  styleUrls: ['./card.component.scss']
})
export class CardComponent implements OnInit {

  @Input()
  value: string;

  @Input()
  showAction: boolean = true;

  @Output()
  buttonClick = new EventEmitter<any>();

  constructor() { }

  ngOnInit() {
  }

  buttonClicked() {
    this.buttonClick.emit();
  }

}
