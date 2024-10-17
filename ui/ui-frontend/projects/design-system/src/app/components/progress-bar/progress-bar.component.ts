import { Component } from '@angular/core';
import { CommonProgressBarModule } from 'vitamui-library';
import { MatLegacyProgressSpinnerModule as MatProgressSpinnerModule } from '@angular/material/legacy-progress-spinner';
import { DecimalPipe, NgForOf } from '@angular/common';

@Component({
  selector: 'design-system-progress-bar',
  templateUrl: './progress-bar.component.html',
  styleUrls: ['./progress-bar.component.scss'],
  standalone: true,
  imports: [CommonProgressBarModule, MatProgressSpinnerModule, DecimalPipe, NgForOf],
})
export class ProgressBarComponent {
  nbSteps = [2, 3, 4, 5];
  step = 0;
  percent = 0;

  constructor() {
    setInterval(() => this.step++, 1000);
    setInterval(() => {
      this.percent += Math.round(Math.random() * 10);
      if (this.percent > 100) this.percent = 0;
    }, 400);
  }
}
