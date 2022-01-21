import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterModule} from "@angular/router";
import {VitamUICommonModule} from "ui-frontend-common";
import {VitamUILibraryModule} from "../../../../../vitamui-library/src/lib/vitamui-library.module";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatMenuModule} from "@angular/material/menu";
import {MatSnackBarModule} from "@angular/material/snack-bar";
import {MatDialogModule} from "@angular/material/dialog";
import {MatSidenavModule} from "@angular/material/sidenav";
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";
import {MatSelectModule} from "@angular/material/select";
import {MatOptionModule} from "@angular/material/core";
import {MatTabsModule} from "@angular/material/tabs";
import {ProfilePreviewComponent} from "./profile-preview.component";
import {ProfileInformationTabComponent} from "./profile-information-tab/profile-information-tab/profile-information-tab.component";
import {MatIconModule} from "@angular/material/icon";


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
