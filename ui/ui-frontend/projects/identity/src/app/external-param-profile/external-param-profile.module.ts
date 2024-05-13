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
import { ReactiveFormsModule } from '@angular/forms';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatTabsModule } from '@angular/material/tabs';
import { LevelInputModule, RoleToggleModule, VitamUICommonModule } from 'vitamui-library';
import { SharedModule } from '../shared/shared.module';
import { GroupAttributionModule } from '../user/group-attribution/group-attribution.module';
import { ExternalParamProfileCreateComponent } from './external-param-profile-create/external-param-profile-create.component';
import { ExternalParamProfileDetailComponent } from './external-param-profile-detail/external-param-profile-detail.component';
import { InformationTabComponent } from './external-param-profile-detail/information-tab/information-tab.component';
import { ThresholdsTabComponent } from './external-param-profile-detail/thresholds-tab/thresholds-tab.component';
import { ExternalParamProfileListComponent } from './external-param-profile-list/external-param-profile-list.component';
import { ExternalParamProfileRoutingModule } from './external-param-profile-routing.module';
import { ExternalParamProfileComponent } from './external-param-profile.component';

@NgModule({
  imports: [
    CommonModule,
    VitamUICommonModule,
    SharedModule,
    MatButtonToggleModule,
    MatMenuModule,
    MatSelectModule,
    ReactiveFormsModule,
    GroupAttributionModule,
    LevelInputModule,
    MatSidenavModule,
    MatProgressSpinnerModule,
    ExternalParamProfileRoutingModule,
    MatProgressBarModule,
    MatTabsModule,
    RoleToggleModule,
  ],
  declarations: [
    ExternalParamProfileComponent,
    ExternalParamProfileListComponent,
    ExternalParamProfileCreateComponent,
    ExternalParamProfileDetailComponent,
    InformationTabComponent,
    ThresholdsTabComponent,
  ],
})
export class ExternalParamProfileModule {}
