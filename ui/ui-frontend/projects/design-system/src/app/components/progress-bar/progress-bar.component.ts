import { Component } from '@angular/core';

@Component({
  selector: 'design-system-progress-bar',
  templateUrl: './progress-bar.component.html',
  styleUrls: ['./progress-bar.component.scss'],
})
export class ProgressBarComponent {
  public progressValue: number;
}
