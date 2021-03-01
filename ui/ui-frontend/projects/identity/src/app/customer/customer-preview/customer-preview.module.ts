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
import { MatDialogModule } from '@angular/material/dialog';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterModule } from '@angular/router';
import { VitamUICommonModule } from 'ui-frontend-common';

import { SharedModule } from '../../shared/shared.module';
import {CustomerColorsInputModule} from '../customer-create/customer-colors-input/customer-colors-input.module';
import { CustomerPreviewComponent } from './customer-preview.component';
import { GraphicIdentityTabComponent } from './graphic-identity-tab/graphic-identity-tab.component';
import { GraphicIdentityUpdateComponent } from './graphic-identity-tab/graphic-identity-update/graphic-identity-update.component';
import { GraphicIdentityFormComponent } from './graphic-identity-tab/graphic-identity/graphic-identity-form/graphic-identity-form.component';
import { GraphicIdentityComponent } from './graphic-identity-tab/graphic-identity/graphic-identity.component';
import { InformationTabComponent } from './information-tab/information-tab.component';
import { IdentityProviderCreateComponent } from './sso-tab/identity-provider-create/identity-provider-create.component';
import { IdentityProviderDetailsComponent } from './sso-tab/identity-provider-details/identity-provider-details.component';
import { IdentityProviderService } from './sso-tab/identity-provider.service';
import { SsoTabComponent } from './sso-tab/sso-tab.component';

@NgModule({
  imports: [
    CommonModule,
    CustomerColorsInputModule,
    SharedModule,
    RouterModule,
    MatDialogModule,
    MatMenuModule,
    MatTabsModule,
    MatButtonToggleModule,
    ReactiveFormsModule,
    MatProgressBarModule,
    MatTooltipModule,
    MatProgressSpinnerModule,
    VitamUICommonModule,
  ],
  declarations: [
    CustomerPreviewComponent,
    SsoTabComponent,
    IdentityProviderCreateComponent,
    IdentityProviderDetailsComponent,
    InformationTabComponent,
    GraphicIdentityTabComponent,
    GraphicIdentityUpdateComponent,
    GraphicIdentityComponent,
    GraphicIdentityFormComponent,
  ],
  exports: [ CustomerPreviewComponent, GraphicIdentityComponent ],
  entryComponents: [IdentityProviderCreateComponent, GraphicIdentityUpdateComponent],
  providers: [IdentityProviderService]
})
export class CustomerPreviewModule { }
