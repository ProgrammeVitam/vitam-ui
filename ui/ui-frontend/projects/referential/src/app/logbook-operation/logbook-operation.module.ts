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
import {TableFilterModule, VitamUICommonModule} from 'ui-frontend-common';

import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {MatDialogModule} from '@angular/material/dialog';
import {MatNativeDateModule} from '@angular/material/core';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MatMenuModule} from '@angular/material/menu';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatSidenavModule} from '@angular/material/sidenav';
import {MatTabsModule} from '@angular/material/tabs';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {MatSelectModule} from '@angular/material/select';

import {
  LogbookOperationDetailComponent
} from './logbook-operation-detail/logbook-operation-detail.component';
import {
  LogbookOperationPopupComponent
} from './logbook-operation-detail/logbook-operation-popup.component';
import {EventTypeBadgeClassPipe} from './logbook-operation-list/event-type-badge-class.pipe';
import {EventTypeColorClassPipe} from './logbook-operation-list/event-type-color-class.pipe';
import {LastEventPipe} from './logbook-operation-list/last-event.pipe';
import {LogbookOperationListComponent} from './logbook-operation-list/logbook-operation-list.component';
import {LogbookOperationRoutingModule} from './logbook-operation-routing.module';
import {LogbookOperationComponent} from './logbook-operation.component';

@NgModule({
  declarations: [
    LogbookOperationComponent,
    LogbookOperationListComponent,
    LogbookOperationDetailComponent,
    LogbookOperationPopupComponent,
    LastEventPipe,
    EventTypeColorClassPipe,
    EventTypeBadgeClassPipe,
  ],
  imports: [
    CommonModule,
    MatSidenavModule,
    MatMenuModule,
    MatDatepickerModule,
    MatDialogModule,
    MatProgressSpinnerModule,
    MatTabsModule,
    MatSnackBarModule,
    ReactiveFormsModule,
    VitamUICommonModule,
    LogbookOperationRoutingModule,
    MatNativeDateModule,
    MatSelectModule,
    TableFilterModule
  ]
})
export class LogbookOperationModule { }
