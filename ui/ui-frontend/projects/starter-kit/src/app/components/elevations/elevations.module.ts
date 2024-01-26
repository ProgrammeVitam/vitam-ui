import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { VitamUICommonModule } from 'ui-frontend-common';
import { ElevationComponent } from './elevations.component';

@NgModule({
  imports: [CommonModule, VitamUICommonModule],
  declarations: [ElevationComponent],
})
export class ElevationModule {}
