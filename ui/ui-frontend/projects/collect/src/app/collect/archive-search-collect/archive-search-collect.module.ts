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
import { ReactiveFormsModule } from '@angular/forms';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatTabsModule } from '@angular/material/tabs';
import { ArchiveUnitRulesDetailsTabComponent } from 'projects/archive-search/src/app/archive/archive-preview/archive-unit-rules-details-tab/archive-unit-rules-details-tab.component';
import { ArchiveSearchHelperService } from 'projects/archive-search/src/app/archive/common-services/archive-search-helper.service';
import { ArchiveSharedDataService } from 'projects/archive-search/src/app/core/archive-shared-data.service';
import { VitamUICommonModule } from 'ui-frontend-common';
import { ArchivePreviewComponent } from './archive-preview/archive-preview.component';
import { ArchiveUnitInformationTabComponent } from './archive-preview/archive-unit-information-tab/archive-unit-information-tab.component';
import { ArchiveUnitRulesInformationsTabComponent } from './archive-preview/archive-unit-rules-details-tab/archive-unit-rules-informations-tab/archive-unit-rules-informations-tab.component';
import { ArchiveSearchCollectRoutingModule } from './archive-search-collect-routing.module';
import { ArchiveSearchCollectComponent } from './archive-search-collect.component';
import { TitleAndDescriptionCriteriaSearchCollectComponent } from './title-and-description-criteria-search-collect/title-and-description-criteria-search-collect.component';

@NgModule({
  imports: [
    CommonModule,
    ArchiveSearchCollectRoutingModule,
    MatSidenavModule,
    VitamUICommonModule,
    MatProgressSpinnerModule,
    MatFormFieldModule,
    MatSelectModule,
    ReactiveFormsModule,
    MatTabsModule,
    MatDatepickerModule,
    MatDialogModule,
  ],
  providers: [ArchiveSearchHelperService, ArchiveSharedDataService],
  declarations: [
    ArchiveSearchCollectComponent,
    TitleAndDescriptionCriteriaSearchCollectComponent,
    ArchivePreviewComponent,
    ArchiveUnitRulesDetailsTabComponent,
    ArchiveUnitInformationTabComponent,
    ArchiveUnitRulesInformationsTabComponent,
  ],
})
export class ArchiveSearchCollectModule {}
