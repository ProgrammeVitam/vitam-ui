import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatLegacyButtonModule as MatButtonModule } from '@angular/material/legacy-button';
import { MatLegacyCardModule as MatCardModule } from '@angular/material/legacy-card';

import { CardComponent } from './card.component';

@NgModule({
  declarations: [CardComponent],
  imports: [CommonModule, MatCardModule, MatButtonModule],
  exports: [CardComponent],
})
export class CardModule {}
