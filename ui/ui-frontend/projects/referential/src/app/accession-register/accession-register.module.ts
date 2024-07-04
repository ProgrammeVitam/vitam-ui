/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
import { CommonModule } from '@angular/common';
import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatLegacyCardModule as MatCardModule } from '@angular/material/legacy-card';
import { MatLegacyCheckboxModule as MatCheckboxModule } from '@angular/material/legacy-checkbox';
import { MatPseudoCheckboxModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatLegacyDialogModule as MatDialogModule } from '@angular/material/legacy-dialog';
import { MatLegacyMenuModule as MatMenuModule } from '@angular/material/legacy-menu';
import { MatLegacyProgressBarModule as MatProgressBarModule } from '@angular/material/legacy-progress-bar';
import { MatLegacyProgressSpinnerModule as MatProgressSpinnerModule } from '@angular/material/legacy-progress-spinner';
import { MatLegacyRadioModule as MatRadioModule } from '@angular/material/legacy-radio';
import { MatLegacySelectModule as MatSelectModule } from '@angular/material/legacy-select';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatLegacySnackBarModule as MatSnackBarModule } from '@angular/material/legacy-snack-bar';
import { MatLegacyTabsModule as MatTabsModule } from '@angular/material/legacy-tabs';
import { RouterModule } from '@angular/router';
import { RoleToggleComponent, TableFilterComponent, VitamUICommonModule, VitamUILibraryModule } from 'vitamui-library';
// eslint-disable-next-line max-len
import { AccessionRegisterAdvancedSearchComponent } from './accession-register-advanced-search/accession-register-advanced-search.component';
import { AccessionRegisterFacetsComponent } from './accession-register-facets/accession-register-facets.component';
import { AccessionRegisterListComponent } from './accession-register-list/accession-register-list.component';
import { AccessionRegisterDetailComponent } from './accession-register-preview/accession-register-detail/accession-register-detail.component';
import { AccessionRegisterOperationComponent } from './accession-register-preview/accession-register-operations-list/accession-register-operation/accession-register-operation.component';
import { AccessionRegisterOperationsListComponent } from './accession-register-preview/accession-register-operations-list/accession-register-operations-list.component';
import { AccessionRegisterPreviewComponent } from './accession-register-preview/accession-register-preview.component';
import { AccessionRegisterRoutingModule } from './accession-register-routing.module';
import { AccessionRegisterComponent } from './accession-register.component';

@NgModule({
  imports: [
    CommonModule,
    RouterModule,
    VitamUICommonModule,
    MatMenuModule,
    MatSnackBarModule,
    MatDialogModule,
    MatSidenavModule,
    MatProgressSpinnerModule,
    TableFilterComponent,
    VitamUILibraryModule,
    AccessionRegisterRoutingModule,
    MatButtonToggleModule,
    MatSelectModule,
    ReactiveFormsModule,
    MatProgressBarModule,
    MatTabsModule,
    RoleToggleComponent,
    MatCheckboxModule,
    MatCardModule,
    MatPseudoCheckboxModule,
    MatDatepickerModule,
    VitamUICommonModule,
    MatRadioModule,
    AccessionRegisterComponent,
    AccessionRegisterListComponent,
    AccessionRegisterFacetsComponent,
    AccessionRegisterAdvancedSearchComponent,
    AccessionRegisterPreviewComponent,
    AccessionRegisterDetailComponent,
    AccessionRegisterOperationsListComponent,
    AccessionRegisterOperationComponent,
  ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
})
export class AccessionRegisterModule {}
