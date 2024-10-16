import { Component } from '@angular/core';
import { rgbToHsl, toHex } from '../../../../../vitamui-library/src/app/modules';
import { KeyValuePipe, NgClass, NgForOf } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  // eslint-disable-next-line @angular-eslint/component-selector
  selector: 'design-system-colors',
  templateUrl: './colors.component.html',
  styleUrls: ['./colors.component.scss'],
  standalone: true,
  imports: [NgClass, TranslateModule, KeyValuePipe, NgForOf],
})
export class ColorsComponent {
  private hueIds = [50, 100, 200, 300, 400, 500, 600, 700, 800, 900];
  colors = [
    { key: 'PRIMARY', varName: '--vitamui-primary', hueIds: this.hueIds },
    { key: 'SECONDARY', varName: '--vitamui-secondary', hueIds: this.hueIds },
    { key: 'GREY', varName: '--vitamui-grey', hueIds: this.hueIds },
    { key: 'ADDITIONAL', varName: '--vitamui-additional', hueIds: this.hueIds },
    { key: 'PRIMARY_LIGHT', varName: '--vitamui-background' },
    { key: 'RED', className: 'red' },
    { key: 'ORANGE', className: 'orange' },
    { key: 'GREEN', className: 'green' },
  ];

  private colorToRGB(color: string) {
    const [_, r, g, b] = /rgba?\((\d+), (\d+), (\d+)(, \d+)?\)/.exec(color.toString());
    return { r: Number.parseInt(r), g: Number.parseInt(g), b: Number.parseInt(b) };
  }

  colorToHex(element: HTMLElement): string {
    const color = getComputedStyle(element).backgroundColor;
    if (!color) return color;
    const { r, g, b } = this.colorToRGB(color);
    return `#${toHex(r)}${toHex(g)}${toHex(b)}`.toUpperCase();
  }

  colorToHsl(element: HTMLElement): { S?: number; H?: number; L?: number } {
    const color = getComputedStyle(element).backgroundColor;
    if (!color) return {};
    const { h, s, l } = rgbToHsl(this.colorToRGB(color));
    return { H: Math.round(360 * h), S: s, L: l };
  }
}
