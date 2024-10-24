import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatLegacyInputModule as MatInputModule } from '@angular/material/legacy-input';
import { VitamUICommonModule } from '../../../app/modules';

import { CardGroupModule } from '../card-group/card-group.module';
import { VitamUIInputModule } from '../vitamui-input/vitamui-input.module';
import { CardSelectComponent } from './card-select.component';

@NgModule({
  declarations: [CardSelectComponent],
  imports: [CommonModule, VitamUICommonModule, VitamUIInputModule, CardGroupModule, MatInputModule],
  exports: [CardSelectComponent],
})
export class CardSelectModule {}
