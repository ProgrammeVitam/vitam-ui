import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatMenuModule } from '@angular/material/menu';
import { TranslateModule } from '@ngx-translate/core';
import { UserAlertsMenuComponent } from './user-alerts-menu.component';

@NgModule({
  declarations: [UserAlertsMenuComponent],
  imports: [CommonModule, MatMenuModule, TranslateModule],
  exports: [UserAlertsMenuComponent],
})
export class UserAlertsMenuModule {}
