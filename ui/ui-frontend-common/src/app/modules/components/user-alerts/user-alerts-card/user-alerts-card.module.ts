import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { UserAlertsCardComponent } from './user-alerts-card.component';

@NgModule({
  imports: [
    CommonModule,
    TranslateModule
  ],
  declarations: [UserAlertsCardComponent],
  exports: [UserAlertsCardComponent]
})
export class UserAlertCardModule { }
