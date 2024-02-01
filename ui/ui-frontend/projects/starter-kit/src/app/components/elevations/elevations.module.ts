import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ElevationComponent } from './elevations.component';
import { VitamUICommonModule } from 'ui-frontend-common';

@NgModule({
  imports: [CommonModule, VitamUICommonModule],
  declarations: [ElevationComponent],
})
export class ElevationModule {}
