/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */

import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatNativeDateModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialogModule } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatRadioModule } from '@angular/material/radio';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatTooltipModule } from '@angular/material/tooltip';
import { VitamUICommonModule } from 'ui-frontend-common';
import { CreateGetorixDepositComponent } from './create-getorix-deposit/create-getorix-deposit.component';
import { GetorixDepositAdvisePreviewComponent } from './getorix-deposit-advise-preview/getorix-deposit-advise-preview.component';
import { GetorixDepositRoutingModule } from './getorix-deposit-routing.module';
import { GetorixUploadObjectModule } from './getorix-deposit-upload-object/getorix-upload-object.module';
import { GetorixDepositComponent } from './getorix-deposit.component';
import { GetorixDepositService } from './getorix-deposit.service';
import { GetorixDepositSharedDataService } from './services/getorix-deposit-shared-data.service';

@NgModule({
  declarations: [GetorixDepositComponent, CreateGetorixDepositComponent, GetorixDepositAdvisePreviewComponent],
  imports: [
    CommonModule,
    MatSidenavModule,
    GetorixDepositRoutingModule,
    VitamUICommonModule,
    ReactiveFormsModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatRadioModule,
    MatButtonModule,
    MatCheckboxModule,
    MatTooltipModule,
    FormsModule,
    MatInputModule,
    MatDialogModule,
    MatProgressSpinnerModule,
    GetorixUploadObjectModule,
  ],
  providers: [GetorixDepositService, GetorixDepositSharedDataService],
  exports: [GetorixDepositComponent],
})
export class GetorixDepositModule {}
