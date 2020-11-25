import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { VitamUICommonModule } from 'ui-frontend-common';
import { MatTooltipModule, MatDialogModule, MatMenuModule, MatSidenavModule, MatDatepickerModule,
   MatNativeDateModule } from '@angular/material';
import { CollectRoutingModule } from './collect-routing.module';
import { SharedModule } from './shared/shared.module';
import { ReactiveFormsModule } from '@angular/forms';
import { CollectComponent } from './collect.component';



@NgModule({
  imports: [
    MatTooltipModule,
    CommonModule,
    VitamUICommonModule,
    MatDialogModule,
    MatMenuModule,
    MatSidenavModule,
    CollectRoutingModule,
    SharedModule,
    ReactiveFormsModule,
    MatDatepickerModule,
    MatNativeDateModule
  ],
  declarations: [
    CollectComponent
  ]
})
export class CollectModule { }
