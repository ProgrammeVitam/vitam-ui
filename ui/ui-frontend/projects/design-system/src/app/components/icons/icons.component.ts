import { Component } from '@angular/core';

@Component({
  // tslint:disable-next-line:component-selector
  selector: 'design-system-icons',
  templateUrl: './icons.component.html',
  styleUrls: ['./icons.component.scss'],
})
export class IconsComponent {
  icons: (string | string[])[];
  coloredIcons: { [key: string]: string[] };
  colors = ['primary', 'secondary', 'danger', 'success', 'warning', 'light'];
  selectedColor: string = '';

  constructor() {
    const cssRules = Array.from(document.styleSheets)
      .reduce((acc, v) => {
        try {
          return acc.concat(Array.from(v.cssRules));
        } catch (e) {
          return acc;
        }
      }, [])
      .filter((css) => css.cssText?.startsWith('.vitamui-icon-'))
      .map((css) => css.cssText.split(':')[0].split('vitamui-icon-')[1])
      .sort((a, b) => a.localeCompare(b));

    const obj = cssRules.reduce((acc, cssRule) => {
      const [icon, path] = cssRule.split(' .');
      acc[icon] = acc[icon] || [];
      if (path) {
        acc[icon] = [...new Set([...acc[icon], path])];
      }
      return acc;
    }, {});

    this.icons = Object.keys(obj);
    this.coloredIcons = Object.entries(obj)
      .filter(([_, value]) => value)
      .reduce(
        (acc, [key, value]) => {
          acc[key] = value as string[];
          return acc;
        },
        {} as { [key: string]: string[] },
      );
  }
}
