import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatLegacyOptionModule as MatOptionModule } from '@angular/material/legacy-core';
import { MatLegacySelectModule as MatSelectModule } from '@angular/material/legacy-select';
import { VitamUICommonModule } from 'vitamui-library';
import { IconsComponent } from './icons.component';

@NgModule({
  declarations: [IconsComponent],
  imports: [CommonModule, VitamUICommonModule, FormsModule, MatOptionModule, MatSelectModule],
  exports: [IconsComponent],
})
export class IconsModule {}
