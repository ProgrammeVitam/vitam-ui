import { TranslateModule } from '@ngx-translate/core';
import { MatInputModule } from '@angular/material/input';
import { ReactiveFormsModule } from '@angular/forms';
import { VitamUICommonModule } from 'ui-frontend-common';
import { TranslationComponent } from './translation.component';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

@NgModule({
  declarations: [TranslationComponent],
  imports: [CommonModule, VitamUICommonModule, ReactiveFormsModule, MatInputModule, TranslateModule],
  exports: [TranslationComponent],
})
export class TranslationModule {}
