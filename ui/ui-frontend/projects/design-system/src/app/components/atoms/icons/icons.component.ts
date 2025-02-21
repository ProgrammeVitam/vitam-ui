/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SelectComponent } from 'vitamui-library';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  templateUrl: './icons.component.html',
  styleUrls: ['./icons.component.scss'],
  standalone: true,
  imports: [CommonModule, TranslateModule, FormsModule, SelectComponent],
})
export class IconsComponent {
  icons: (string | string[])[];
  coloredIcons: { [key: string]: string[] };
  colors = ['primary', 'secondary', 'danger', 'success', 'warning', 'light'];
  selectedColor: string = '';

  colorOptions = { options: this.colors.map((color) => ({ key: color, label: color })) };

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
