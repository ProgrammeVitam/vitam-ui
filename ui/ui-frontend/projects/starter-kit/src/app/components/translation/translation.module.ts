import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { TranslateModule } from '@ngx-translate/core';
import { VitamUICommonModule } from 'ui-frontend-common';
import { TranslationComponent } from './translation.component';



@NgModule({
  declarations: [
    TranslationComponent
  ],
  imports: [
    CommonModule,
    VitamUICommonModule,
    ReactiveFormsModule,
    MatInputModule,
    TranslateModule
  ],
  exports: [
    TranslationComponent
  ]
})
export class TranslationModule { }
