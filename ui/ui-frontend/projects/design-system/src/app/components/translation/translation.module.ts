import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MatLegacyInputModule as MatInputModule } from '@angular/material/legacy-input';
import { TranslateModule } from '@ngx-translate/core';
import { VitamUICommonModule } from 'vitamui-library';
import { TranslationComponent } from './translation.component';

@NgModule({
  declarations: [TranslationComponent],
  imports: [CommonModule, VitamUICommonModule, ReactiveFormsModule, MatInputModule, TranslateModule],
  exports: [TranslationComponent],
})
export class TranslationModule {}
