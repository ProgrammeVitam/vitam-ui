import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatLegacyButtonModule as MatButtonModule } from '@angular/material/legacy-button';
import { MatLegacyCheckboxModule as MatCheckboxModule } from '@angular/material/legacy-checkbox';
import { MatLegacyProgressSpinnerModule as MatProgressSpinnerModule } from '@angular/material/legacy-progress-spinner';
import { MatLegacyTooltipModule as MatTooltipModule } from '@angular/material/legacy-tooltip';
import { MatTreeModule } from '@angular/material/tree';

import { FilingPlanComponent } from './filing-plan.component';
import { NodeComponent } from './node.component';

@NgModule({
  imports: [CommonModule, FormsModule, MatTreeModule, MatButtonModule, MatCheckboxModule, MatProgressSpinnerModule, MatTooltipModule],
  declarations: [FilingPlanComponent, NodeComponent],
  exports: [FilingPlanComponent],
})
export class FilingPlanModule {}
