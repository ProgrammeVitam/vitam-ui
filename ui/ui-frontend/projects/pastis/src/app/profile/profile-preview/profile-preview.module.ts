import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatLegacyOptionModule as MatOptionModule } from '@angular/material/legacy-core';
import { MatLegacyDialogModule as MatDialogModule } from '@angular/material/legacy-dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatLegacyMenuModule as MatMenuModule } from '@angular/material/legacy-menu';
import { MatLegacyProgressSpinnerModule as MatProgressSpinnerModule } from '@angular/material/legacy-progress-spinner';
import { MatLegacySelectModule as MatSelectModule } from '@angular/material/legacy-select';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatLegacySnackBarModule as MatSnackBarModule } from '@angular/material/legacy-snack-bar';
import { MatLegacyTabsModule as MatTabsModule } from '@angular/material/legacy-tabs';
import { RouterModule } from '@angular/router';
import { VitamUICommonModule, VitamUILibraryModule } from 'vitamui-library';
import { ProfileInformationTabComponent } from './profile-information-tab/profile-information-tab/profile-information-tab.component';
import { ProfilePreviewComponent } from './profile-preview.component';

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
    MatIconModule,
  ],

  declarations: [ProfilePreviewComponent, ProfileInformationTabComponent],
  exports: [ProfilePreviewComponent],
})
export class ProfilePreviewModule {}
