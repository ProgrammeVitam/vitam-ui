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

import { CommonModule, DatePipe } from '@angular/common';
import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatNativeDateModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatTreeModule } from '@angular/material/tree';
import { TranslateService } from '@ngx-translate/core';
import { VitamUILibraryModule } from 'projects/vitamui-library/src/public-api';
import { TableFilterModule, VitamUICommonModule } from 'ui-frontend-common';
import { ArchiveApiService } from '../core/api/archive-api.service';
import { ArchiveSharedDataService } from '../core/archive-shared-data.service';
import { ManagementRulesSharedDataService } from '../core/management-rules-shared-data.service';
import { ArchivePreviewComponent } from './archive-preview/archive-preview.component';
import { ArchiveUnitInformationTabComponent } from './archive-preview/archive-unit-information-tab/archive-unit-information-tab.component';
import { ArchiveUnitObjectsDetailsTabComponent } from './archive-preview/archive-unit-objects-details-tab/archive-unit-objects-details-tab.component';
import { ArchiveUnitRulesDetailsTabComponent } from './archive-preview/archive-unit-rules-details-tab/archive-unit-rules-details-tab.component';
import { ArchiveUnitRulesInformationsTabComponent } from './archive-preview/archive-unit-rules-details-tab/archive-unit-rules-informations-tab/archive-unit-rules-informations-tab.component';
import { ArchiveRoutingModule } from './archive-routing.module';
import { DipRequestCreateComponent } from './archive-search/additional-actions-search/dip-request-create/dip-request-create.component';
import { AddManagementRulesComponent } from './archive-search/additional-actions-search/management-rules/archive-unit-rules/add-management-rules/add-management-rules.component';
import { AddUpdatePropertyComponent } from './archive-search/additional-actions-search/management-rules/archive-unit-rules/add-update-property/add-update-property.component';
import { ArchiveUnitRulesComponent } from './archive-search/additional-actions-search/management-rules/archive-unit-rules/archive-unit-rules.component';
import { BlockCategoryInheritanceComponent } from './archive-search/additional-actions-search/management-rules/archive-unit-rules/block-category-inheritance/block-category-inheritance.component';
import { BlockRulesInheritanceComponent } from './archive-search/additional-actions-search/management-rules/archive-unit-rules/block-rules-inheritance/block-rules-inheritance.component';
import { DeleteUnitRulesComponent } from './archive-search/additional-actions-search/management-rules/archive-unit-rules/delete-unit-rules/delete-unit-rules.component';
import { UnlockCategoryInheritanceComponent } from './archive-search/additional-actions-search/management-rules/archive-unit-rules/unlock-category-inheritance/unlock-category-inheritance.component';
import { UnlockRulesInheritanceComponent } from './archive-search/additional-actions-search/management-rules/archive-unit-rules/unlock-rules-inheritance/unlock-rules-inheritance.component';
import { UpdateUnitRulesComponent } from './archive-search/additional-actions-search/management-rules/archive-unit-rules/update-unit-rules/update-unit-rules.component';
import { ManagementRulesComponent } from './archive-search/additional-actions-search/management-rules/management-rules.component';
import { ReclassificationComponent } from './archive-search/additional-actions-search/reclassification/reclassification.component';
import { TransferRequestModalComponent } from './archive-search/additional-actions-search/transfer-request-modal/transfer-request-modal.component';
import { AccessRuleSearchComponent } from './archive-search/archive-search-by-mgt-rules/access-rule-search/access-rule-search.component';
import { AppraisalRuleSearchComponent } from './archive-search/archive-search-by-mgt-rules/appraisal-rule-search/appraisal-rule-search.component';
import { DisseminationRuleSearchComponent } from './archive-search/archive-search-by-mgt-rules/dissemination-rule-search/dissemination-rule-search.component';
import { ReuseRuleSearchComponent } from './archive-search/archive-search-by-mgt-rules/reuse-rule-search/reuse-rule-search.component';
import { StorageRuleSearchComponent } from './archive-search/archive-search-by-mgt-rules/storage-rule-search/storage-rule-search.component';
import { ArchiveSearchRulesFacetsComponent } from './archive-search/archive-search-rules-facets/archive-search-rules-facets.component';
import { SearchAccessRulesFacetsComponent } from './archive-search/archive-search-rules-facets/search-access-rules-facets/search-access-rules-facets.component';
import { SearchAppraisalRulesFacetsComponent } from './archive-search/archive-search-rules-facets/search-appraisal-rules-facets/search-appraisal-rules-facets.component';
import { SearchDisseminationRulesFacetsComponent } from './archive-search/archive-search-rules-facets/search-dissemination-rules-facets/search-dissemination-rules-facets.component';
import { SearchReuseRulesFacetsComponent } from './archive-search/archive-search-rules-facets/search-reuse-rules-facets/search-reuse-rules-facets.component';
import { SearchStorageRulesFacetsComponent } from './archive-search/archive-search-rules-facets/search-storage-rules-facets/search-storage-rules-facets.component';
import { ArchiveSearchComponent } from './archive-search/archive-search.component';
import { RuleValidator } from './archive-search/rule.validator';
import { SearchCriteriaListComponent } from './archive-search/search-criteria-list/search-criteria-list.component';
import { SearchCriteriaSaverComponent } from './archive-search/search-criteria-saver/search-criteria-saver.component';
import { SearchCriteriaSaverService } from './archive-search/search-criteria-saver/search-criteria-saver.service';
import { SimpleCriteriaSearchComponent } from './archive-search/simple-criteria-search/simple-criteria-search.component';
import { TitleAndDescriptionCriteriaSearchComponent } from './archive-search/title-and-description-criteria-search/title-and-description-criteria-search.component';
import { TransferAcknowledgmentComponent } from './archive-search/transfer-acknowledgment/transfer-acknowledgment.component';
import { ArchiveComponent } from './archive.component';
import { ArchiveFacetsService } from './common-services/archive-facets.service';
import { ArchiveSearchHelperService } from './common-services/archive-search-helper.service';
import { ArchiveUnitDipService } from './common-services/archive-unit-dip.service';
import { ArchiveUnitEliminationService } from './common-services/archive-unit-elimination.service';
import { ComputeInheritedRulesService } from './common-services/compute-inherited-rules.service';
import { UpdateUnitManagementRuleService } from './common-services/update-unit-management-rule.service';
import { CriteriaSearchComponent } from './criteria-search/criteria-search.component';
import { ClassificationTreeComponent } from './filing-holding-scheme/classification-tree/classification-tree.component';
import { FilingHoldingSchemeComponent } from './filing-holding-scheme/filing-holding-scheme.component';
import { LeavesTreeComponent } from './filing-holding-scheme/leaves-tree/leaves-tree.component';
import { SharedModule } from './shared/shared.module';
import { ArchiveUnitValidatorService } from './validators/archive-unit-validator.service';
import { ManagementRulesValidatorService } from './validators/management-rules-validator.service';

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
    MatRadioModule,
    CommonModule,
  ],
  providers: [
    ArchiveApiService,
    ArchiveSharedDataService,
    DatePipe,
    TranslateService,
    SearchCriteriaSaverService,
    RuleValidator,
    ManagementRulesSharedDataService,
    ManagementRulesValidatorService,
    ArchiveUnitValidatorService,
    ArchiveSearchHelperService,
    UpdateUnitManagementRuleService,
    ComputeInheritedRulesService,
    ArchiveUnitDipService,
    ArchiveUnitEliminationService,
    ArchiveFacetsService,
  ],
  declarations: [
    ArchiveComponent,
    LeavesTreeComponent,
    FilingHoldingSchemeComponent,
    ArchiveSearchComponent,
    ArchivePreviewComponent,
    SearchCriteriaSaverComponent,
    SearchCriteriaListComponent,
    CriteriaSearchComponent,
    AppraisalRuleSearchComponent,
    StorageRuleSearchComponent,
    AccessRuleSearchComponent,
    ReuseRuleSearchComponent,
    DisseminationRuleSearchComponent,
    SearchDisseminationRulesFacetsComponent,
    SearchReuseRulesFacetsComponent,
    SimpleCriteriaSearchComponent,
    TitleAndDescriptionCriteriaSearchComponent,
    DipRequestCreateComponent,
    TransferRequestModalComponent,
    SearchAppraisalRulesFacetsComponent,
    SearchStorageRulesFacetsComponent,
    SearchAccessRulesFacetsComponent,
    ArchiveSearchRulesFacetsComponent,
    SearchReuseRulesFacetsComponent,
    ManagementRulesComponent,
    AddManagementRulesComponent,
    ArchiveUnitRulesComponent,
    AddUpdatePropertyComponent,
    UpdateUnitRulesComponent,
    ReclassificationComponent,
    DeleteUnitRulesComponent,
    ArchiveUnitInformationTabComponent,
    ArchiveUnitRulesDetailsTabComponent,
    ArchiveUnitObjectsDetailsTabComponent,
    ArchiveUnitRulesInformationsTabComponent,
    DeleteUnitRulesComponent,
    BlockCategoryInheritanceComponent,
    UnlockCategoryInheritanceComponent,
    BlockRulesInheritanceComponent,
    UnlockRulesInheritanceComponent,
    TransferAcknowledgmentComponent,
    ClassificationTreeComponent,
  ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
})
export class ArchiveModule {}
