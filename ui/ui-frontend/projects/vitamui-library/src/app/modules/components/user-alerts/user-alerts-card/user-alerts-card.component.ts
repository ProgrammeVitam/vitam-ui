import { Component, EventEmitter, Input, Output } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'vitamui-common-user-alerts-card',
  templateUrl: './user-alerts-card.component.html',
  styleUrls: ['./user-alerts-card.component.scss'],
  standalone: true,
  imports: [TranslateModule],
})
export class UserAlertsCardComponent {
  @Input() applicationName: string;
  @Input() details: string;
  @Input() date: string;
  @Input() time: string;

  @Output() openAlert = new EventEmitter();
  @Output() removeAlert = new EventEmitter();
}
