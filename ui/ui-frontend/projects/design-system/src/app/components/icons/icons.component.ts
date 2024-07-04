import { Component } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { MatLegacyOptionModule } from '@angular/material/legacy-core';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { MatLegacySelectModule } from '@angular/material/legacy-select';
import { MatLegacyFormFieldModule } from '@angular/material/legacy-form-field';
import { NgFor, NgIf } from '@angular/common';

@Component({
  // eslint-disable-next-line @angular-eslint/component-selector
  selector: 'design-system-icons',
  templateUrl: './icons.component.html',
  styleUrls: ['./icons.component.scss'],
  standalone: true,
  imports: [
    NgFor,
    MatLegacyFormFieldModule,
    MatLegacySelectModule,
    ReactiveFormsModule,
    FormsModule,
    MatLegacyOptionModule,
    NgIf,
    TranslateModule,
  ],
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
