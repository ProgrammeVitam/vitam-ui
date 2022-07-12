/*Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
and the signatories of the "VITAM - Accord du Contributeur" agreement.

contact@programmevitam.fr

This software is a computer program whose purpose is to implement
implement a digital archiving front-office system for the secure and
efficient high volumetry VITAM solution.

This software is governed by the CeCILL-C license under French law and
abiding by the rules of distribution of free software.  You can  use,
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability.

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or
data to be ensured and,  more generally, to use and operate it in the
same conditions as regards security.

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
*/

import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { VitamUILibraryModule } from 'vitamui-library';
import { ReactiveFormsModule } from '@angular/forms';
import { TableFilterModule, VitamUICommonModule } from 'ui-frontend-common';

import { CollectRoutingModule } from './collect-routing.module';
import { ProjectsComponent } from './projects/projects.component';
import { ProjectListComponent } from './projects/project-list/project-list.component';
import { CreateProjectComponent } from './projects/create-project/create-project.component';
import { MatMenuModule } from '@angular/material/menu';
import { MatDialogModule } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { NgxFilesizeModule } from 'ngx-filesize';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatSelectModule } from '@angular/material/select';
import { ArchiveSearchCollectComponent } from './archive-search-collect/archive-search-collect.component';
import { TitleAndDescriptionCriteriaSearchComponent } from 'projects/archive-search/src/app/archive/archive-search/title-and-description-criteria-search/title-and-description-criteria-search.component';
import { TitleAndDescriptionCriteriaSearchCollectComponent } from './archive-search-collect/title-and-description-criteria-search-collect/title-and-description-criteria-search-collect.component';
import { ArchiveSearchHelperService } from 'projects/archive-search/src/app/archive/common-services/archive-search-helper.service';
import { ArchiveSharedDataService } from 'projects/archive-search/src/app/core/archive-shared-data.service';
import { ArchiveUnitCollectApiService } from '../core/api/archive-unit-collect-api.service';

@NgModule({
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
  ],
  providers: [
    ArchiveSearchHelperService,
    ArchiveSharedDataService,
    ArchiveUnitCollectApiService
  ],
  declarations: [ProjectsComponent, ProjectListComponent, CreateProjectComponent,
    ArchiveSearchCollectComponent, TitleAndDescriptionCriteriaSearchComponent, TitleAndDescriptionCriteriaSearchCollectComponent]
})
export class CollectModule { }
