import { Component } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { MatLegacyDialogModule } from '@angular/material/legacy-dialog';

@Component({
  selector: 'app-customer-alerting',
  templateUrl: './customer-alerting.component.html',
  styleUrls: ['./customer-alerting.component.scss'],
  standalone: true,
  imports: [MatLegacyDialogModule, TranslateModule],
})
export class CustomerAlertingComponent {}
