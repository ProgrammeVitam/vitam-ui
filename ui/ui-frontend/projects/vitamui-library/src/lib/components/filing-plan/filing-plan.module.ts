import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {MatButtonModule} from '@angular/material/button';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatTreeModule} from '@angular/material/tree';

import {FilingPlanComponent} from './filing-plan.component';
import {NodeComponent} from './node.component';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    MatTreeModule,
    MatButtonModule,
    MatCheckboxModule,
    MatProgressSpinnerModule,
    MatTooltipModule
  ],
  declarations: [FilingPlanComponent, NodeComponent],
  exports: [FilingPlanComponent]
})
export class FilingPlanModule {
}
