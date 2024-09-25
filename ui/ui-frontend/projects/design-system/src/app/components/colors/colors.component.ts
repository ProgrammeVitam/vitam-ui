import { Component } from '@angular/core';
import { rgbToHsl, toHex } from '../../../../../vitamui-library/src/app/modules';

@Component({
  // eslint-disable-next-line @angular-eslint/component-selector
  selector: 'design-system-colors',
  templateUrl: './colors.component.html',
  styleUrls: ['./colors.component.scss'],
})
export class ColorsComponent {
  colors = [
    { key: 'PRIMARY', value: '--vitamui-primary' },
    { key: 'SECONDARY', value: '--vitamui-secondary' },
    { key: 'GREY', value: '--vitamui-grey' },
    { key: 'ADDITIONAL', value: '--vitamui-additional' },
  ];
  hueIds = [50, 100, 200, 300, 400, 500, 600, 700, 800, 900];

  private colorToRGB(color: string) {
    const [_, r, g, b] = /rgb\((\d+), (\d+), (\d+)\)/.exec(color.toString());
    return { r: Number.parseInt(r), g: Number.parseInt(g), b: Number.parseInt(b) };
  }

  colorToHex(color: string): string {
    const { r, g, b } = this.colorToRGB(color);
    return `#${toHex(r)}${toHex(g)}${toHex(b)}`.toUpperCase();
  }

  colorToHsl(color: string): { S: number; H: number; L: number } {
    const { h, s, l } = rgbToHsl(this.colorToRGB(color));
    return { H: Math.round(360 * h), S: s, L: l };
  }
}
