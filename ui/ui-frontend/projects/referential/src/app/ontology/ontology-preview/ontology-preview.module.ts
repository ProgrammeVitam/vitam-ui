import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {
  MatDialogModule,
  MatMenuModule,
  MatOptionModule,
  MatProgressSpinnerModule,
  MatSelectModule,
  MatSidenavModule,
  MatSnackBarModule,
  MatTabsModule,
} from '@angular/material';
import {RouterModule} from '@angular/router';
import {VitamUILibraryModule} from 'projects/vitamui-library/src/public-api';
import {VitamUICommonModule} from 'ui-frontend-common';

import {OntologyInformationTabComponent} from './ontology-information-tab/ontology-information-tab.component';
import {OntologyPreviewComponent} from './ontology-preview.component';

@NgModule({
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
  declarations: [
    OntologyPreviewComponent,
    OntologyInformationTabComponent
  ],
  exports: [
    OntologyPreviewComponent
  ]

})
export class OntologyPreviewModule {
}
