import { Component, EventEmitter, Input, Output } from '@angular/core';
import { AlertOption, UserAlerts } from '../../../models/user/user-alerts.interface';
import { TranslateModule } from '@ngx-translate/core';
import { NgFor, NgIf } from '@angular/common';
import { MatLegacyMenuModule } from '@angular/material/legacy-menu';

@Component({
  selector: 'vitamui-common-user-alerts-menu',
  templateUrl: './user-alerts-menu.component.html',
  styleUrls: ['./user-alerts-menu.component.scss'],
  standalone: true,
  imports: [MatLegacyMenuModule, NgFor, NgIf, TranslateModule],
})
export class UserAlertsMenuComponent {
  @Input() userAlerts: UserAlerts;
  @Input() hasMoreAlerts: boolean;

  @Output() openAlert = new EventEmitter<AlertOption>();
  @Output() closeAlert = new EventEmitter<AlertOption>();
  @Output() seeMoreAlerts = new EventEmitter();

  constructor() {}
}
