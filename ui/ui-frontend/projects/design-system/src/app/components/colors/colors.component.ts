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
