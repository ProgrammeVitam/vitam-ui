/*
Copyright © CINES - Centre Informatique National pour l'Enseignement Supérieur (2020)

[dad@cines.fr]

This software is a computer program whose purpose is to provide
a web application to create, edit, import and export archive
profiles based on the french SEDA standard
(https://redirect.francearchives.fr/seda/).


This software is governed by the CeCILL-C  license under French law and
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
import { PortalModule } from '@angular/cdk/portal';
import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TranslateModule } from '@ngx-translate/core';
import { VitamUICommonModule } from 'vitamui-library';
import { PastisMaterialModule } from '../material.module';
import { CenterMatmenuDirective } from '../profile/edit-profile/file-tree-metadata/center-matmenu.directive';
import { ModifyTextButtonComponent } from './modify-text-button/modify-text-button.component';
import { PastisBreadcrumbComponent } from './pastis-breadcrumb-components/pastis-breadcrumb/pastis-breadcrumb.component';
import { PastisTitleBreadcrumbComponent } from './pastis-breadcrumb-components/pastis-title-breadcrumb/pastis-title-breadcrumb.component';
import { PastisDialogConfirmComponent } from './pastis-dialog/pastis-dialog-confirm/pastis-dialog-confirm.component';
import { PastisGenericPopupComponent } from './pastis-generic-popup/pastis-generic-popup.component';
import { PastisPopupMetadataLanguageComponent } from './pastis-popup-metadata-language/pastis-popup-metadata-language.component';
import { PastisPopupOptionComponent } from './pastis-popup-option/pastis-popup-option.component';
import { PastisUnderConstructionComponent } from './pastis-under-construction/pastis-under-construction.component';

@NgModule({
  declarations: [
    PastisUnderConstructionComponent,
    CenterMatmenuDirective,
    PastisPopupMetadataLanguageComponent,
    PastisDialogConfirmComponent,
    PastisBreadcrumbComponent,
    PastisTitleBreadcrumbComponent,
    PastisGenericPopupComponent,
    PastisPopupOptionComponent,
    ModifyTextButtonComponent,
  ],
  imports: [
    CommonModule,
    FormsModule,
    MatTooltipModule,
    PastisMaterialModule,
    MatSlideToggleModule,
    PortalModule,
    TranslateModule,
    VitamUICommonModule,
  ],
  exports: [
    PastisUnderConstructionComponent,
    MatTooltipModule,
    PastisMaterialModule,
    PastisDialogConfirmComponent,
    CenterMatmenuDirective,
    PastisPopupMetadataLanguageComponent,
    PastisBreadcrumbComponent,
    PastisTitleBreadcrumbComponent,
    PastisGenericPopupComponent,
    PastisPopupOptionComponent,
    ModifyTextButtonComponent,
  ],
})
export class SharedModule {}
