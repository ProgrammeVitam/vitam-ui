import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

@Component({
  selector: 'vitamui-common-user-alerts-card',
  templateUrl: './user-alerts-card.component.html',
  styleUrls: ['./user-alerts-card.component.scss']
})
export class UserAlertsCardComponent implements OnInit {

  @Input() applicationName: string;
  @Input() details: string;
  @Input() date: string;
  @Input() time: string;

  @Output() openAlert = new EventEmitter();
  @Output() removeAlert = new EventEmitter();

  constructor() { }

  ngOnInit() {
  }

}
