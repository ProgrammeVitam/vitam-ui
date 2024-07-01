import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatLegacyInputModule as MatInputModule } from '@angular/material/legacy-input';
import { VitamUICommonModule } from '../../../app/modules';

import { CardSelectComponent } from './card-select.component';

@NgModule({
  imports: [CommonModule, VitamUICommonModule, MatInputModule, CardSelectComponent],
  exports: [CardSelectComponent],
})
export class CardSelectModule {}
