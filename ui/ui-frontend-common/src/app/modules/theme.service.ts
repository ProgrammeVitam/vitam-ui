/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
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

import { Injectable } from '@angular/core';
import { DomSanitizer, SafeResourceUrl, SafeUrl } from '@angular/platform-browser';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { filter, map, take } from 'rxjs/operators';
import { AppConfiguration } from '.';
import { AuthUser, ThemeDataType } from './models';
import { GraphicIdentity } from './models/customer/graphic-identity.interface';
import { Color } from './models/customer/theme/color.interface';
import { convertLighten, getColorFromMaps, hexToRgb, hexToRgbString, ThemeColorType } from './utils';

export interface Theme {
  colors: { [colorId: string]: string };
  headerUrl?: SafeResourceUrl;
  footerUrl?: SafeResourceUrl;
  portalUrl?: SafeResourceUrl;
  userUrl?: SafeResourceUrl;
}

@Injectable({
  providedIn: 'root',
})
export class ThemeService {
  public get defaultTheme(): Theme {
    return this._defaultTheme.getValue();
  }

  public set defaultTheme(theme: Theme) {
    this._defaultTheme.next(theme);
  }

  constructor(private domSanitizer: DomSanitizer) {}

  private baseColors: { [colorId in ThemeColorType]?: string } = {
    [ThemeColorType.VITAMUI_PRIMARY]: 'COLOR.PRIMARY',
    [ThemeColorType.VITAMUI_SECONDARY]: 'COLOR.SECONDARY',
    [ThemeColorType.VITAMUI_TERTIARY]: 'COLOR.TERTIARY',
    [ThemeColorType.VITAMUI_HEADER_FOOTER]: 'COLOR.HEADER_FOOTER',
    [ThemeColorType.VITAMUI_BACKGROUND]: 'COLOR.BACKGROUND',
  };

  // tslint:disable-next-line: variable-name
  private _defaultTheme = new BehaviorSubject<Theme>(null);

  // Default theme
  defaultMap: { [colordId in ThemeColorType]: string } = {
    [ThemeColorType.VITAMUI_PRIMARY]: '#604379',
    [ThemeColorType.VITAMUI_GREY]: '#9E9E9E',
    [ThemeColorType.VITAMUI_ADDITIONAL]: '#9AA0FF',
    [ThemeColorType.VITAMUI_SECONDARY]: '#65B2E4',
    [ThemeColorType.VITAMUI_TERTIARY]: '#E7304D',
    [ThemeColorType.VITAMUI_HEADER_FOOTER]: '#604379',
    [ThemeColorType.VITAMUI_BACKGROUND]: '#F5F7FC',
    /* DEPRECATED colors : Use color chart with declinations var(--vitamui-primary-XXX),
    var(--vitamui-secondary-XXX) and var(--vitamui-grey-XXX) */
    [ThemeColorType.VITAMUI_PRIMARY_LIGHT]: '',
    [ThemeColorType.VITAMUI_PRIMARY_LIGHT_20]: '',
    [ThemeColorType.VITAMUI_PRIMARY_DARK]: '',
    [ThemeColorType.VITAMUI_SECONDARY_LIGHT]: '',
    [ThemeColorType.VITAMUI_SECONDARY_LIGHT_8]: '',
    [ThemeColorType.VITAMUI_SECONDARY_DARK_5]: '',
  };

  // Theme for current app configuration
  applicationColorMap: { [colorId: string]: string };

  // tslint:disable-next-line: variable-name
  private _backgroundChoice: Color[] = [
    { class: 'Foncé', value: '#0F0D2D' },
    { class: 'Blanc', value: '#FFFFFF' },
    { class: 'Clair', value: '#F5F5F5' },
    { class: 'Bleu clair', value: '#F5F7FC' },
  ];

  public get backgroundChoice(): Color[] {
    return this._backgroundChoice;
  }

  public getBaseColors(): { [colorId in ThemeColorType]?: string } {
    return this.baseColors;
  }

  public getVariationColorsNames(baseName: string): string[] {
    return Object.keys(this.defaultMap).filter((colorName) => colorName.startsWith(baseName));
  }

  public init(conf: AppConfiguration, customerColorMap: { [colorId: string]: string }): void {
    this.applicationColorMap = conf.THEME_COLORS;

    this.overrideTheme(customerColorMap);
    if (conf) {
      this.defaultTheme = {
        colors: conf.THEME_COLORS,
        headerUrl: this.domSanitizer.bypassSecurityTrustUrl('data:image/*;base64,' + conf.HEADER_LOGO),
        footerUrl: this.domSanitizer.bypassSecurityTrustUrl('data:image/*;base64,' + conf.FOOTER_LOGO),
        portalUrl: this.domSanitizer.bypassSecurityTrustUrl('data:image/*;base64,' + conf.PORTAL_LOGO),
        userUrl: this.domSanitizer.bypassSecurityTrustUrl('data:image/*;base64,' + conf.USER_LOGO),
      };

      // init default background
      const defaultBackground = this.backgroundChoice.find(
        (color: Color) => color.value === conf.THEME_COLORS[ThemeColorType.VITAMUI_BACKGROUND],
      );
      if (defaultBackground) {
        defaultBackground.isDefault = true;
      }
    }
  }

  public overloadLocalTheme(colors: { [colorId: string]: string }, selectorToOver: string): void {
    const selector: HTMLElement = document.querySelector(selectorToOver);
    for (const key in colors) {
      if (colors.hasOwnProperty(key) && selector != null) {
        selector.style.setProperty('--' + key, colors[key]);
      }
    }
  }

  public getData$(authUser: AuthUser, type: string): Observable<string | SafeResourceUrl> {
    const userGraphicIdentity: GraphicIdentity = authUser?.basicCustomer?.graphicIdentity;
    const hasCustomGraphicIdentity = userGraphicIdentity?.hasCustomGraphicIdentity;

    return this._defaultTheme.pipe(
      filter((theme: Theme) => !!theme),
      map((theme: Theme) => {
        let value: string | SafeResourceUrl;
        switch (type) {
          case ThemeDataType.PORTAL_LOGO:
            const portal64 = hasCustomGraphicIdentity && userGraphicIdentity.portalDataBase64;
            value = portal64 ? this.domSanitize(userGraphicIdentity.portalDataBase64) : theme.portalUrl;
            break;
          case ThemeDataType.HEADER_LOGO:
            const header64 = hasCustomGraphicIdentity && userGraphicIdentity.headerDataBase64;
            value = header64 ? this.domSanitize(userGraphicIdentity.headerDataBase64) : theme.headerUrl;
            break;
          case ThemeDataType.FOOTER_LOGO:
            const footer64 = hasCustomGraphicIdentity && userGraphicIdentity.footerDataBase64;
            value = footer64 ? this.domSanitize(userGraphicIdentity.footerDataBase64) : theme.footerUrl;
            break;
          case ThemeDataType.USER_LOGO:
            const userData64 = hasCustomGraphicIdentity && userGraphicIdentity.userDataBase64;
            value = userData64 ? this.domSanitize(userGraphicIdentity.userDataBase64) : theme.userUrl;
            break;
          default:
            return;
        }

        return value;
      }, take(1)),
    );
  }

  private domSanitize(base64: string): SafeUrl {
    return this.domSanitizer.bypassSecurityTrustUrl('data:image/*;base64,' + base64);
  }

  private calculateFontColor(color: string): string {
    const rgbColor = hexToRgb(color);
    if (rgbColor.r * 0.299 + rgbColor.g * 0.587 + rgbColor.b * 0.114 > 186) {
      return '#000000';
    } else {
      return '#ffffff';
    }
  }

  private add10Declinations(key: string, colors: {}, customerColors: { [colorId: string]: string }): void {
    // tslint:disable-next-line: variable-name
    const mergedMap = { ...this.defaultMap, ...this.applicationColorMap, ...customerColors };
    const rgbValue = hexToRgb(mergedMap[key]);
    // consider hs-L from color key as 500

    if (key === ThemeColorType.VITAMUI_GREY) {
      colors[key + '-900'] = '#212121';
      colors[key + '-800'] = '#424242';
      colors[key + '-700'] = '#616161';
      colors[key + '-600'] = '#757575';
      colors[key + '-400'] = '#BDBDBD';
      colors[key + '-300'] = '#E0E0E0';
      colors[key + '-200'] = '#EEEEEE';
      colors[key + '-100'] = '#F5F5F5';
      colors[key + '-50'] = '#FAFAFA';
    } else if (key === ThemeColorType.VITAMUI_ADDITIONAL) {
      colors[key + '-900'] = '#000AA3';
      colors[key + '-800'] = '#000DE0';
      colors[key + '-700'] = '#1F2CFF';
      colors[key + '-600'] = '#5C65FF';
      colors[key + '-400'] = '#A8ADFF';
      colors[key + '-300'] = '#B8BCFF';
      colors[key + '-200'] = '#C7CAFF';
      colors[key + '-100'] = '#D6D9FF';
      colors[key + '-50'] = '#E5E7FF';
    } else {
      colors[key + '-900'] = convertLighten(rgbValue, -32);
      colors[key + '-800'] = convertLighten(rgbValue, -24);
      colors[key + '-700'] = convertLighten(rgbValue, -16);
      colors[key + '-600'] = convertLighten(rgbValue, -8);
      // The color declination 500 is the base version (we use var(--vitamui-primary) instead of var(--vitamui-primary-500))
      colors[key + '-400'] = convertLighten(rgbValue, 8);
      colors[key + '-300'] = convertLighten(rgbValue, 16);
      colors[key + '-200'] = convertLighten(rgbValue, 24);
      colors[key + '-100'] = convertLighten(rgbValue, 32);
      colors[key + '-50'] = convertLighten(rgbValue, 40);
    }

    colors[key + '-900-font'] = this.calculateFontColor(colors[key + '-900']);
    colors[key + '-800-font'] = this.calculateFontColor(colors[key + '-800']);
    colors[key + '-700-font'] = this.calculateFontColor(colors[key + '-700']);
    colors[key + '-600-font'] = this.calculateFontColor(colors[key + '-600']);
    colors[key + '-font'] = this.calculateFontColor(mergedMap[key]); // primary/secondary/tertiary
    colors[key + '-400-font'] = this.calculateFontColor(colors[key + '-400']);
    colors[key + '-300-font'] = this.calculateFontColor(colors[key + '-300']);
    colors[key + '-200-font'] = this.calculateFontColor(colors[key + '-200']);
    colors[key + '-100-font'] = this.calculateFontColor(colors[key + '-100']);
    colors[key + '-50-font'] = this.calculateFontColor(colors[key + '-50']);
  }

  /**
   * Gives complete color theme from current app config and any given customization.
   * Setting base colors (primary, secondary) will return updated variations (primary-light etc..)
   * @param customerColors Entries to override
   */
  public getThemeColors(customerColors: { [colorId: string]: string } = null): { [colorId: string]: string } {
    const colors = {};
    for (const key in this.defaultMap) {
      if (this.defaultMap.hasOwnProperty(key)) {
        if (
          (
            [
              ThemeColorType.VITAMUI_PRIMARY,
              ThemeColorType.VITAMUI_SECONDARY,
              ThemeColorType.VITAMUI_GREY,
              ThemeColorType.VITAMUI_ADDITIONAL,
            ] as string[]
          ).includes(key)
        ) {
          this.add10Declinations(key, colors, customerColors);
        } else if (key === ThemeColorType.VITAMUI_HEADER_FOOTER) {
          const mergedMap = { ...this.defaultMap, ...this.applicationColorMap, ...customerColors };
          colors[key + '-font'] = this.calculateFontColor(mergedMap[key]);
        }
        colors[key] = getColorFromMaps(key, this.defaultMap, this.applicationColorMap, customerColors);
      }
    }
    return colors;
  }

  public overrideTheme(customerThemeMap, selector = 'body'): void {
    const element: HTMLElement = document.querySelector(selector);
    const themeColors = this.getThemeColors(customerThemeMap);
    for (const key in themeColors) {
      if (themeColors.hasOwnProperty(key)) {
        element.style.setProperty('--' + key, themeColors[key]);
        element.style.setProperty('--' + key + '-rgb', hexToRgbString(themeColors[key]));
      }
    }
  }
}
