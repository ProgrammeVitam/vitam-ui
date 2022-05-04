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
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { CollectRoutingModule } from './collect-routing.module';
import { ProjectsComponent } from './projects/projects.component';
import { TableFilterModule, VitamUICommonModule} from 'ui-frontend-common';
import { MatMenuModule } from '@angular/material/menu';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialogModule } from '@angular/material/dialog';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { VitamUILibraryModule } from 'vitamui-library';
import { CreateProjectComponent } from './projects/create-project/create-project.component';
import { ReactiveFormsModule } from '@angular/forms';
import { ProjectListComponent } from './projects/project-list/project-list.component';
import { NgxFilesizeModule } from 'ngx-filesize';
import {CollectConfirmUploadComponent} from "./shared/collect-confirm-upload/collect-confirm-upload.component";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatDatepickerModule} from "@angular/material/datepicker";
import {MatSelectModule} from "@angular/material/select";

@NgModule({
  declarations: [
    ProjectsComponent,
    CreateProjectComponent,
    ProjectListComponent,
    CollectConfirmUploadComponent
  ],
  imports: [
    CommonModule,
    CollectRoutingModule,
    VitamUICommonModule,
    MatMenuModule,
    MatSnackBarModule,
    MatDialogModule,
    MatSidenavModule,
    MatProgressSpinnerModule,
    TableFilterModule,
    VitamUILibraryModule,
    ReactiveFormsModule,
    NgxFilesizeModule,
    MatFormFieldModule,
    MatDatepickerModule,
    MatSelectModule
  ]
})
export class CollectModule {}
