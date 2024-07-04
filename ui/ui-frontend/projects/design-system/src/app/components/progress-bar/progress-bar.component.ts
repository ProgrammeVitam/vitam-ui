import { Component } from '@angular/core';
import { MatLegacyProgressSpinnerModule } from '@angular/material/legacy-progress-spinner';
import { CommonProgressBarComponent } from 'vitamui-library';

@Component({
  selector: 'design-system-progress-bar',
  templateUrl: './progress-bar.component.html',
  styleUrls: ['./progress-bar.component.scss'],
  standalone: true,
  imports: [CommonProgressBarComponent, MatLegacyProgressSpinnerModule],
})
export class ProgressBarComponent {
  public progressValue: number;
}
