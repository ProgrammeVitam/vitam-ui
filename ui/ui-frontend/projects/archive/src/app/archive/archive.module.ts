import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { VitamUICommonModule } from 'ui-frontend-common';
import { MatTooltipModule, MatDialogModule, MatMenuModule, MatSidenavModule, MatDatepickerModule,
   MatNativeDateModule } from '@angular/material';
import { ArchiveRoutingModule } from './archive-routing.module';
import { SharedModule } from './shared/shared.module';
import { ReactiveFormsModule } from '@angular/forms';
import { ArchiveComponent } from './archive.component';



@NgModule({
  imports: [
    MatTooltipModule,
    CommonModule,
    VitamUICommonModule,
    MatDialogModule,
    MatMenuModule,
    MatSidenavModule,
    ArchiveRoutingModule,
    SharedModule,
    ReactiveFormsModule,
    MatDatepickerModule,
    MatNativeDateModule
  ],
  declarations: [
    ArchiveComponent
  ]
})
export class ArchiveModule { }
