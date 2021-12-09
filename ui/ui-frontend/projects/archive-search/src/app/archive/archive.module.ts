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
import { ArchiveSharedDataServiceService } from '../core/archive-shared-data-service.service';
import { ManagementRulesSharedDataService } from '../core/management-rules-shared-data.service';
import { ArchivePreviewComponent } from './archive-preview/archive-preview.component';
import { ArchiveSearchPopupComponent } from './archive-preview/archive-search-popup.component';
import { ArchiveRoutingModule } from './archive-routing.module';
import { ArchiveSearchResolverService } from './archive-search-resolver.service';
import { AppraisalRuleSearchComponent } from './archive-search/appraisal-rule-search/appraisal-rule-search.component';
import { RuleValidator } from './archive-search/appraisal-rule-search/rule.validator';
import { ArchiveSearchRulesFacetsComponent } from './archive-search/archive-search-rules-facets/archive-search-rules-facets.component';
import { SearchAppraisalRulesFacetsComponent } from './archive-search/archive-search-rules-facets/search-appraisal-rules-facets/search-appraisal-rules-facets.component';
import { ArchiveSearchComponent } from './archive-search/archive-search.component';
import { DipRequestCreateComponent } from './archive-search/dip-request-create/dip-request-create.component';
import { SearchCriteriaListComponent } from './archive-search/search-criteria-list/search-criteria-list.component';
import { SearchCriteriaSaverComponent } from './archive-search/search-criteria-saver/search-criteria-saver.component';
import { SearchCriteriaSaverService } from './archive-search/search-criteria-saver/search-criteria-saver.service';
import { SimpleCriteriaSearchComponent } from './archive-search/simple-criteria-search/simple-criteria-search.component';
import { TitleAndDescriptionCriteriaSearchComponent } from './archive-search/title-and-description-criteria-search/title-and-description-criteria-search.component';
import { ArchiveComponent } from './archive.component';
import { CriteriaSearchComponent } from './criteria-search/criteria-search.component';
import { FilingHoldingSchemeComponent } from './filing-holding-scheme/filing-holding-scheme.component';
import { FilingHoldingNodeComponent } from './filing-holding-scheme/tree-node/filing-holding-node.component';
import { AddManagementRulesComponent } from './management-rules/add-management-rules/add-management-rules.component';
import { AddUpdatePropertyComponent } from './management-rules/add-update-property/add-update-property.component';
import { DuaManagementRulesComponent } from './management-rules/dua-management-rules/dua-management-rules.component';
import { ManagementRulesComponent } from './management-rules/management-rules.component';
import { SharedModule } from './shared/shared.module';
import { SearchAppraisalRulesFacetsComponent } from './archive-search/archive-search-rules-facets/search-appraisal-rules-facets/search-appraisal-rules-facets.component';
import { ArchiveSearchRulesFacetsComponent } from './archive-search/archive-search-rules-facets/archive-search-rules-facets.component';
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
  ],
  providers: [
    ArchiveApiService,
    ArchiveSharedDataServiceService,
    DatePipe,
    ArchiveSearchResolverService,
    TranslateService,
    SearchCriteriaSaverService,
    RuleValidator,
    ManagementRulesSharedDataService,
    ManagementRulesValidatorService,
  ],
  declarations: [
    ArchiveComponent,
    FilingHoldingNodeComponent,
    FilingHoldingSchemeComponent,
    ArchiveSearchComponent,
    ArchivePreviewComponent,
    ArchiveSearchPopupComponent,
    SearchCriteriaSaverComponent,
    SearchCriteriaListComponent,
    CriteriaSearchComponent,
    AppraisalRuleSearchComponent,
    SimpleCriteriaSearchComponent,
    TitleAndDescriptionCriteriaSearchComponent,
    DipRequestCreateComponent,
    SearchAppraisalRulesFacetsComponent,
    ArchiveSearchRulesFacetsComponent
    ManagementRulesComponent,
    AddManagementRulesComponent,
    DuaManagementRulesComponent,
    AddUpdatePropertyComponent,
  ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class ArchiveModule {}
