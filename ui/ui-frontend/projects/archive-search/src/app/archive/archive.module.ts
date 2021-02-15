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
import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { TableFilterModule, VitamUICommonModule } from 'ui-frontend-common';

import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialogModule } from '@angular/material/dialog';
import { MatMenuModule } from '@angular/material/menu';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTreeModule } from '@angular/material/tree';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { MatButtonModule } from '@angular/material/button';
import { MatNativeDateModule } from '@angular/material/core';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { ArchiveRoutingModule } from './archive-routing.module';
import { SharedModule } from './shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { ArchiveComponent } from './archive.component';
import { FilingHoldingSchemeComponent } from './filing-holding-scheme/filing-holding-scheme.component';
import { FilingHoldingNodeComponent } from './filing-holding-scheme/tree-node/filing-holding-node.component';
import { ArchiveSearchComponent } from './archive-search/archive-search.component';
import { ArchiveApiService } from '../core/api/archive-api.service';
import { ArchiveSharedDataServiceService } from '../core/archive-shared-data-service.service';
import { AccessContractComponent } from './access-contract/access-contract.component';
import { VitamUILibraryModule } from 'projects/vitamui-library/src/public-api';
import { ArchivePreviewComponent } from './archive-preview/archive-preview.component';
import { ArchiveSearchPopupComponent } from './archive-preview/archive-search-popup.component';
import { ArchiveSearchResolverService } from './archive-search-resolver.service';
import { TranslateService } from '@ngx-translate/core';
 
@NgModule({
  imports: [
    MatTooltipModule,
    CommonModule,
    VitamUICommonModule,
    MatDialogModule,
    MatMenuModule,
    MatSidenavModule,
    MatTreeModule,
    ArchiveRoutingModule,
    SharedModule,
    ReactiveFormsModule,
    MatDatepickerModule,
    MatButtonModule,
    MatCheckboxModule,
    FormsModule,
    MatNativeDateModule,
    MatButtonToggleModule,
    MatButtonToggleModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatProgressSpinnerModule,
    TableFilterModule,
    VitamUILibraryModule,
    MatIconModule, 
    MatTabsModule,
    
  ],
  providers: [ArchiveApiService, ArchiveSharedDataServiceService, DatePipe, ArchiveSearchResolverService, TranslateService] ,
  declarations: [
    ArchiveComponent,
    FilingHoldingNodeComponent,
    FilingHoldingSchemeComponent,
    ArchiveSearchComponent,
    AccessContractComponent,
    ArchivePreviewComponent,
    ArchiveSearchPopupComponent,
    
  ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class ArchiveModule { }
