import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatOptionModule } from '@angular/material/core';
import { MatSelectModule } from '@angular/material/select';
import { VitamUICommonModule } from 'vitamui-library';
import { IconsComponent } from './icons.component';

@NgModule({
  declarations: [IconsComponent],
  imports: [CommonModule, VitamUICommonModule, FormsModule, MatOptionModule, MatSelectModule],
  exports: [IconsComponent],
})
export class IconsModule {}
