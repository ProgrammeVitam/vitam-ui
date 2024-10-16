import { Component } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'design-system-spacing',
  templateUrl: './spacing.component.html',
  styleUrls: ['./spacing.component.scss'],
  imports: [TranslateModule],
  standalone: true,
})
export class SpacingComponent {
  paddings = [0, 1, 2, 3, 4, 5, 6, 7, 8];
  margins = [0, 1, 2, 3, 4, 5, 6, 7, 8, 'auto'];

  getPadding(element: HTMLElement) {
    return getComputedStyle(element).padding;
  }

  getMargin(element: HTMLElement) {
    return getComputedStyle(element).margin;
  }
}
