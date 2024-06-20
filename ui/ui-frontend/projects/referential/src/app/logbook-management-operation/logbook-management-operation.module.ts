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
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatLegacyDialogModule as MatDialogModule } from '@angular/material/legacy-dialog';
import { MatLegacyFormFieldModule as MatFormFieldModule } from '@angular/material/legacy-form-field';
import { MatLegacyProgressSpinnerModule as MatProgressSpinnerModule } from '@angular/material/legacy-progress-spinner';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatLegacyTabsModule as MatTabsModule } from '@angular/material/legacy-tabs';
import { MatLegacyTooltipModule as MatTooltipModule } from '@angular/material/legacy-tooltip';
import { TableFilterModule, VitamUICommonModule } from 'vitamui-library';
import { LogbookManagementOperationListComponent } from './logbook-management-operation-list/logbook-management-operation-list.component';
// tslint:disable-next-line: max-line-length
import { LogbookManagementOperationInformationTabComponent } from './logbook-management-operation-preview/logbook-management-operation-information-tab/logbook-management-operation-information-tab.component';
// tslint:disable-next-line: max-line-length
import { LogbookManagementOperationPreviewComponent } from './logbook-management-operation-preview/logbook-management-operation-preview.component';
import { LogbookManagementOperationRoutingModule } from './logbook-management-operation-routing.module';
import { LogbookManagementOperationComponent } from './logbook-management-operation.component';

@NgModule({
  declarations: [
    LogbookManagementOperationComponent,
    LogbookManagementOperationListComponent,
    LogbookManagementOperationPreviewComponent,
    LogbookManagementOperationInformationTabComponent,
  ],
  imports: [
    CommonModule,
    LogbookManagementOperationRoutingModule,
    MatSidenavModule,
    MatDatepickerModule,
    VitamUICommonModule,
    MatTooltipModule,
    ReactiveFormsModule,
    FormsModule,
    MatFormFieldModule,
    TableFilterModule,
    MatProgressSpinnerModule,
    MatTabsModule,
    MatDialogModule,
  ],
  exports: [
    LogbookManagementOperationListComponent,
    LogbookManagementOperationPreviewComponent,
    LogbookManagementOperationInformationTabComponent,
  ],
})
export class LogbookManagementOperationModule {}
