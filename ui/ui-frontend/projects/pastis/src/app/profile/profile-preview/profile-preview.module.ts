import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatOptionModule} from '@angular/material/core';
import {MatDialogModule} from '@angular/material/dialog';
import {MatIconModule} from '@angular/material/icon';
import {MatMenuModule} from '@angular/material/menu';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatSelectModule} from '@angular/material/select';
import {MatSidenavModule} from '@angular/material/sidenav';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {MatTabsModule} from '@angular/material/tabs';
import {RouterModule} from '@angular/router';
import {VitamUICommonModule} from 'ui-frontend-common';
import {VitamUILibraryModule} from '../../../../../vitamui-library/src/lib/vitamui-library.module';
import {ProfileInformationTabComponent} from './profile-information-tab/profile-information-tab/profile-information-tab.component';
import {ProfilePreviewComponent} from './profile-preview.component';


@NgModule({
    imports: [
        CommonModule,
        RouterModule,
        VitamUICommonModule,
        VitamUILibraryModule,
        FormsModule,
        ReactiveFormsModule,
        MatMenuModule,
        MatSnackBarModule,
        MatDialogModule,
        MatSidenavModule,
        MatProgressSpinnerModule,
        MatSelectModule,
        MatOptionModule,
        MatTabsModule,
        MatIconModule
    ],

  declarations: [
    ProfilePreviewComponent,
    ProfileInformationTabComponent
  ],
  exports: [
    ProfilePreviewComponent
  ]


})
export class ProfilePreviewModule { }
