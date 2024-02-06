import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { AlertOption, UserAlerts } from '../../../models/user/user-alerts.interface';

@Component({
  selector: 'vitamui-common-user-alerts-menu',
  templateUrl: './user-alerts-menu.component.html',
  styleUrls: ['./user-alerts-menu.component.scss'],
})
export class UserAlertsMenuComponent {
  @Input() userAlerts: UserAlerts;
  @Input() hasMoreAlerts: boolean;

  @Output() openAlert = new EventEmitter<AlertOption>();
  @Output() closeAlert = new EventEmitter<AlertOption>();
  @Output() seeMoreAlerts = new EventEmitter();

  constructor() {}
}
