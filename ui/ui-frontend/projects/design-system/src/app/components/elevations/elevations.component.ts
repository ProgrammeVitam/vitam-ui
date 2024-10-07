import { Component } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { UpperCasePipe } from '@angular/common';

@Component({
  selector: 'design-system-elevations',
  templateUrl: './elevations.component.html',
  styleUrls: ['./elevations.component.scss'],
  imports: [TranslateModule, UpperCasePipe],
  standalone: true,
})
export class ElevationComponent {
  colors = ['dark', 'primary', 'secondary', 'tertiary'];
  elevations = Array.from({ length: 7 }, (_, i) => i + 1);
  name = (elevation: number, color: string) => `${Math.pow(2, elevation)}dp-${color}`;

  getBoxShadow(element: HTMLElement) {
    return element
      .computedStyleMap()
      .get('box-shadow')
      .toString()
      .match(/rgba([^)]+)[^,]+/g);
  }
}
