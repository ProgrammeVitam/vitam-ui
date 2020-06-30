import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {
  MatDialogModule,
  MatMenuModule,
  MatProgressSpinnerModule,
  MatSidenavModule,
  MatSnackBarModule,
} from '@angular/material';
import {RouterModule} from '@angular/router';
import {VitamUILibraryModule} from 'projects/vitamui-library/src/public-api';
import {TableFilterModule, VitamUICommonModule} from 'ui-frontend-common';

import {SharedModule} from '../shared/shared.module';
import {OntologyCreateModule} from './ontology-create/ontology-create.module';
import {OntologyListComponent} from './ontology-list/ontology-list.component';
import {OntologyPreviewModule} from './ontology-preview/ontology-preview.module';
import {OntologyRoutingModule} from './ontology-routing.module';
import {OntologyComponent} from './ontology.component';

@NgModule({
  imports: [
    CommonModule,
    RouterModule,
    VitamUICommonModule,
    VitamUILibraryModule,
    OntologyRoutingModule,
    OntologyCreateModule,
    OntologyPreviewModule,
    MatMenuModule,
    MatSnackBarModule,
    MatDialogModule,
    MatSidenavModule,
    MatProgressSpinnerModule,
    SharedModule,
    TableFilterModule
  ],
  declarations: [
    OntologyComponent,
    OntologyListComponent
  ]

})
export class OntologyModule {
}
