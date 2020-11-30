import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule, MatIconModule, MatMenuModule } from '@angular/material';
import { VitamuiCommonMoreButtonComponent } from './vitamui-common-more-button.component';

@NgModule({
  imports: [
    CommonModule,
    MatMenuModule,
    MatButtonModule,
    MatIconModule
  ],
  declarations: [VitamuiCommonMoreButtonComponent],
  exports: [VitamuiCommonMoreButtonComponent]
})
export class VitamuiCommonMoreButtonModule { }
