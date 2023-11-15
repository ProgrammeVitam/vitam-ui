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
import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {CoreModule} from './../core/core.module';
import {FileTreeModule} from './../profile/edit-profile/file-tree/file-tree.module';
import {SharedModule} from './../shared/shared.module';


import {MatIconModule} from '@angular/material/icon';
import {MatTabsModule} from '@angular/material/tabs';


import {FormsModule} from '@angular/forms';
import {MatSlideToggleModule} from '@angular/material/slide-toggle';
import {TranslateModule} from '@ngx-translate/core';
import {FileUploadModule} from 'ng2-file-upload';
import {NgxUiLoaderModule} from 'ngx-ui-loader';
import {VitamUICommonModule} from 'ui-frontend-common';
import {PastisMaterialModule} from '../material.module';
import {UserActionsModule} from '../user-actions/user-actions.module';
import {CreateNoticeChoiceComponent} from './create-notice-choice/create-notice-choice.component';
import {CreateProfileComponent} from './create-profile/create-profile.component';
import {EditProfileComponent} from './edit-profile/edit-profile.component';
import {ListProfileComponent} from './list-profile/list-profile.component';
import {FilterByStringNamePipe} from './list-profile/pipes/filterByStringName.pipe';
import {FilterByTypePipe} from './list-profile/pipes/filterByType.pipe';
import {ProfilePreviewModule} from './profile-preview/profile-preview.module';


@NgModule({
  imports: [
    CommonModule,
    CoreModule,
    MatIconModule,
    MatTabsModule,
    SharedModule,
    FileTreeModule,
    FileUploadModule,
    PastisMaterialModule,
    UserActionsModule,
    MatSlideToggleModule,
    FormsModule,
    VitamUICommonModule,
    TranslateModule,
    NgxUiLoaderModule,
    ProfilePreviewModule
  ],
  exports: [CreateNoticeChoiceComponent, CreateProfileComponent, EditProfileComponent, ListProfileComponent,
     FilterByStringNamePipe, FilterByTypePipe],
  providers: [],
  declarations: [CreateNoticeChoiceComponent, CreateProfileComponent, EditProfileComponent,
     ListProfileComponent, FilterByStringNamePipe, FilterByTypePipe],

})
export class ProfileModule {
}
