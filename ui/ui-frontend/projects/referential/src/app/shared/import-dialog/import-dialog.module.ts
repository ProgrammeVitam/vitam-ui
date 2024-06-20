import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatLegacyProgressSpinnerModule as MatProgressSpinnerModule } from '@angular/material/legacy-progress-spinner';
import { TranslateModule } from '@ngx-translate/core';
import { VitamUICommonModule } from 'vitamui-library';
import { ImportDialogComponent } from './import-dialog.component';

@NgModule({
  imports: [CommonModule, VitamUICommonModule, TranslateModule, MatProgressSpinnerModule],
  declarations: [ImportDialogComponent],
})
export class ImportDialogModule {}
