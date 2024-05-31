import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { VitamUICommonModule } from 'vitamui-library';
import { ElevationComponent } from './elevations.component';

@NgModule({
  imports: [CommonModule, VitamUICommonModule],
  declarations: [ElevationComponent],
})
export class ElevationModule {}
