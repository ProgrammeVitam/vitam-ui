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
