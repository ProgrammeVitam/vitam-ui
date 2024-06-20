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
import { MatLegacySlideToggleModule as MatSlideToggleModule } from '@angular/material/legacy-slide-toggle';
import { MatLegacyTabsModule as MatTabsModule } from '@angular/material/legacy-tabs';
import { MatLegacyTooltipModule as MatTooltipModule } from '@angular/material/legacy-tooltip';
import { TranslateModule } from '@ngx-translate/core';

import { EditableFieldModule } from '../components/editable-field/editable-field.module';
import { UserPhotoModule } from '../components/header/user-photo/user-photo.module';
import { NavbarModule } from '../components/navbar/navbar.module';
import { SlideToggleModule } from '../components/slide-toggle/slide-toggle.module';
import { VitamuiContentBreadcrumbModule } from '../components/vitamui-content-breadcrumb/vitamui-content-breadcrumb.module';
import { VitamUIFieldErrorModule } from '../components/vitamui-field-error/vitamui-field-error.module';
import { AccountApplicationTabComponent } from './account-application-tab/account-application-tab.component';
import { AccountInformationTabComponent } from './account-information-tab/account-information-tab.component';
import { AccountComponent } from './account.component';

@NgModule({
  imports: [
    CommonModule,
    NavbarModule,
    ReactiveFormsModule,
    EditableFieldModule,
    MatSlideToggleModule,
    MatTooltipModule,
    SlideToggleModule,
    MatTabsModule,
    VitamUIFieldErrorModule,
    VitamuiContentBreadcrumbModule,
    UserPhotoModule,
    TranslateModule,
  ],
  declarations: [AccountComponent, AccountInformationTabComponent, AccountApplicationTabComponent],
})
export class AccountModule {}
