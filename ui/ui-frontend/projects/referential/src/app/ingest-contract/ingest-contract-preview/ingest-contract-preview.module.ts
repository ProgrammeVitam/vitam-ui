import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatOptionModule} from '@angular/material/core';
import {MatDialogModule} from '@angular/material/dialog';
import {MatMenuModule} from '@angular/material/menu';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatSelectModule} from '@angular/material/select';
import {MatSidenavModule} from '@angular/material/sidenav';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {MatTabsModule} from '@angular/material/tabs';
import {RouterModule} from '@angular/router';
import {VitamUILibraryModule} from 'projects/vitamui-library/src/public-api';
import {VitamUICommonModule} from 'ui-frontend-common';

import {IngestContractAttachmentTabComponent} from './ingest-contract-attachment-tab/ingest-contract-attachment-tab.component';
import {
  IngestContractNodeUpdateComponent
} from './ingest-contract-attachment-tab/ingest-contract-nodes-update/ingest-contract-node-update.component';
import {IngestContractFormatTabComponent} from './ingest-contract-format-tab/ingest-contract-format-tab.component';
import {IngestContractInformationTabComponent} from './ingest-contract-information-tab/ingest-contract-information-tab.component';
import {IngestContractObjectTabComponent} from './ingest-contract-object-tab/ingest-contract-object-tab.component';
import {IngestContractPreviewComponent} from './ingest-contract-preview.component';


@NgModule({
  declarations: [
    IngestContractPreviewComponent,
    IngestContractInformationTabComponent,
    IngestContractFormatTabComponent,
    IngestContractObjectTabComponent,
    IngestContractAttachmentTabComponent,
    IngestContractNodeUpdateComponent
  ],
  entryComponents: [
    IngestContractNodeUpdateComponent
  ],
  imports: [
    CommonModule,
    RouterModule,
    VitamUICommonModule,
    VitamUILibraryModule,
    FormsModule,
    ReactiveFormsModule,
    MatMenuModule,
    MatSnackBarModule,
    MatDialogModule,
    MatSidenavModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatOptionModule,
    MatTabsModule
  ],
  exports: [
    IngestContractPreviewComponent
  ]
})
export class IngestContractPreviewModule {
}
