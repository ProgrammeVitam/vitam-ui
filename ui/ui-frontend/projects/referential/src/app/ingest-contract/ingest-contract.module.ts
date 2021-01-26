import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatDialogModule} from '@angular/material/dialog';
import {MatMenuModule} from '@angular/material/menu';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatSidenavModule} from '@angular/material/sidenav';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {MatTabsModule} from '@angular/material/tabs';
import {RouterModule} from '@angular/router';
import {TableFilterModule, VitamUICommonModule} from 'ui-frontend-common';

import {SharedModule} from '../shared/shared.module';
import {IngestContractCreateModule} from './ingest-contract-create/ingest-contract-create.module';
import {IngestContractListComponent} from './ingest-contract-list/ingest-contract-list.component';
import {IngestContractPreviewModule} from './ingest-contract-preview/ingest-contract-preview.module';
import {IngestContractRoutingModule} from './ingest-contract-routing.module';
import {IngestContractComponent} from './ingest-contract.component';

@NgModule({
  declarations: [IngestContractComponent, IngestContractListComponent],
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    ReactiveFormsModule,
    VitamUICommonModule,
    SharedModule,
    MatMenuModule,
    MatSnackBarModule,
    MatDialogModule,
    MatSidenavModule,
    MatProgressSpinnerModule,
    MatTabsModule,
    IngestContractRoutingModule,
    IngestContractPreviewModule,
    IngestContractCreateModule,
    TableFilterModule
  ]
})
export class IngestContractModule {
}
