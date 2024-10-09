import { Component } from '@angular/core';
import { rgbToHsl, toHex } from '../../../../../vitamui-library/src/app/modules';

@Component({
  // eslint-disable-next-line @angular-eslint/component-selector
  selector: 'design-system-colors',
  templateUrl: './colors.component.html',
  styleUrls: ['./colors.component.scss'],
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

  colorToHex(color?: string): string {
    if (!color) return color;
    const { r, g, b } = this.colorToRGB(color);
    return `#${toHex(r)}${toHex(g)}${toHex(b)}`.toUpperCase();
  }

  colorToHsl(color?: string): { S?: number; H?: number; L?: number } {
    if (!color) return {};
    const { h, s, l } = rgbToHsl(this.colorToRGB(color));
    return { H: Math.round(360 * h), S: s, L: l };
  }
}
