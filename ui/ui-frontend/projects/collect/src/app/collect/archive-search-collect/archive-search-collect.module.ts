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
import { ArchiveSharedDataService } from 'projects/archive-search/src/app/core/archive-shared-data.service';
import { ArchiveSearchCollectComponent } from './archive-search-collect.component';
import { ArchiveSearchCollectRoutingModule } from './archive-search-collect-routing.module';
import { ArchivePreviewComponent } from './archive-preview/archive-preview.component';
import { ArchiveUnitRulesDetailsTabComponent } from './archive-preview/archive-unit-rules-details-tab/archive-unit-rules-details-tab.component';
import { ArchiveUnitInformationTabComponent } from './archive-preview/archive-unit-information-tab/archive-unit-information-tab.component';
import { ArchiveUnitRulesInformationsTabComponent } from './archive-preview/archive-unit-rules-details-tab/archive-unit-rules-informations-tab/archive-unit-rules-informations-tab.component';
import { MatSidenavModule } from '@angular/material/sidenav';
import { VitamUICommonModule } from 'ui-frontend-common';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ReactiveFormsModule } from '@angular/forms';
import { MatTabsModule } from '@angular/material/tabs';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialogModule } from '@angular/material/dialog';
import { TitleAndDescriptionCriteriaSearchCollectComponent } from './archive-search-criteria/components/title-and-description-criteria-search-collect/title-and-description-criteria-search-collect.component';
import { CriteriaSearchComponent } from './archive-search-criteria/components/criteria-search/criteria-search.component';
import { SimpleCriteriaSearchComponent } from './archive-search-criteria/components/simple-criteria-search/simple-criteria-search.component';
import { ArchiveSearchHelperService } from './archive-search-criteria/services/archive-search-helper.service';
import { StorageRuleSearchComponent } from './archive-search-criteria/components/storage-rule-search/storage-rule-search.component';
import { ReuseRuleSearchComponent } from './archive-search-criteria/components/reuse-rule-search/reuse-rule-search.component';
import { DisseminationRuleSearchComponent } from './archive-search-criteria/components/dissemination-rule-search/dissemination-rule-search.component';
import { AppraisalRuleSearchComponent } from './archive-search-criteria/components/appraisal-rule-search/appraisal-rule-search.component';
import { AccessRuleSearchComponent } from './archive-search-criteria/components/access-rule-search/access-rule-search.component';
import { RuleValidator } from './archive-search-criteria/services/rule.validator';
import { ArchiveSearchRulesFacetsComponent } from './archive-search-criteria/components/archive-search-rules-facets/archive-search-rules-facets.component';
import { SearchStorageRulesFacetsComponent } from './archive-search-criteria/components/archive-search-rules-facets/search-storage-rules-facets/search-storage-rules-facets.component';
import { SearchReuseRulesFacetsComponent } from './archive-search-criteria/components/archive-search-rules-facets/search-reuse-rules-facets/search-reuse-rules-facets.component';
import { SearchDisseminationRulesFacetsComponent } from './archive-search-criteria/components/archive-search-rules-facets/search-dissemination-rules-facets/search-dissemination-rules-facets.component';
import { SearchAppraisalRulesFacetsComponent } from './archive-search-criteria/components/archive-search-rules-facets/search-appraisal-rules-facets/search-appraisal-rules-facets.component';
import { SearchAccessRulesFacetsComponent } from './archive-search-criteria/components/archive-search-rules-facets/search-access-rules-facets/search-access-rules-facets.component';
import { SearchCriteriaSaverComponent } from './archive-search-criteria/components/search-criteria-saver/search-criteria-saver.component';
import { SearchCriteriaSaverService } from './archive-search-criteria/services/search-criteria-saver.service';
import { SearchCriteriaListComponent } from './archive-search-criteria/components/search-criteria-list/search-criteria-list.component';
import { ConfirmActionModule } from './archive-search-criteria/components/search-criteria-list/confirm-action/confirm-action.module';
import { FilingHoldingSchemeComponent } from './archive-search-criteria/components/filing-holding-scheme/filing-holding-scheme.component';
import { ClassificationTreeComponent } from './archive-search-criteria/components/filing-holding-scheme/classification-tree/classification-tree.component';
import { MatTreeModule } from '@angular/material/tree';
import { LeavesTreeComponent } from './archive-search-criteria/components/filing-holding-scheme/leaves-tree/leaves-tree.component';

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
    ConfirmActionModule,
    MatTreeModule
  ],
  providers: [
    ArchiveSearchHelperService,
    ArchiveSharedDataService,
    RuleValidator,
    SearchCriteriaSaverService
  ],
  declarations: [
    ArchiveSearchCollectComponent,
    TitleAndDescriptionCriteriaSearchCollectComponent,
    ArchivePreviewComponent,
    ArchiveUnitRulesDetailsTabComponent,
    ArchiveUnitInformationTabComponent,
    ArchiveUnitRulesInformationsTabComponent,
    CriteriaSearchComponent,
    SimpleCriteriaSearchComponent,
    StorageRuleSearchComponent,
    ReuseRuleSearchComponent,
    DisseminationRuleSearchComponent,
    AppraisalRuleSearchComponent,
    AccessRuleSearchComponent,
    ArchiveSearchRulesFacetsComponent,
    SearchStorageRulesFacetsComponent,
    SearchReuseRulesFacetsComponent,
    SearchDisseminationRulesFacetsComponent,
    SearchAppraisalRulesFacetsComponent,
    SearchAccessRulesFacetsComponent,
    SearchCriteriaSaverComponent,
    SearchCriteriaListComponent,
    FilingHoldingSchemeComponent,
    ClassificationTreeComponent,
    LeavesTreeComponent
  ],
})
export class ArchiveSearchCollectModule { }
