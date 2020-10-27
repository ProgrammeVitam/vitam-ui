import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { VitamUICommonModule } from 'ui-frontend-common';
import { MatTooltipModule, MatDialogModule, MatMenuModule, MatSidenavModule, MatDatepickerModule,
   MatNativeDateModule } from '@angular/material';
import { ArchiveRoutingModule } from './archive-routing.module';
import { SharedModule } from './shared/shared.module';
import { ReactiveFormsModule } from '@angular/forms';
import { ArchiveComponent } from './archive.component';
import { ArchiveSearchComponent } from './archive-search/archive-search.component';
import { HoldingSchemaComponent } from './holding-schema/holding-schema.component';




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
    ArchiveComponent,
    ArchiveSearchComponent,
    HoldingSchemaComponent
  ]
})
export class ArchiveModule { }
