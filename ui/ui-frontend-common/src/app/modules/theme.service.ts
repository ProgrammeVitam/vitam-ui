import { Injectable } from '@angular/core';
import {getColorFromMaps, hexToRgbString, ThemeColors} from './utils';


@Injectable({
  providedIn: 'root'
})
export class ThemeService {

  private baseColors: {[colorId: string]: string} = {
    'vitamui-primary': 'Couleur principale',
    'vitamui-secondary': 'Couleur secondaire'
  };


  constructor() { }

  // Default theme
  defaultMap: ThemeColors = {
    'vitamui-primary': '#702382',
    'vitamui-primary-light': '',
    'vitamui-primary-light-20': '',
    'vitamui-primary-dark': '',

    'vitamui-secondary': '#7FA1D4',
    'vitamui-secondary-light': '',
    'vitamui-secondary-light-8': '',
    'vitamui-secondary-dark-5': ''
  };

  // Theme for current app configuration
  applicationColorMap;

  public getBaseColors() {
    return this.baseColors;
  }

  public getVariationColorsNames(baseName: string): string[] {
    return Object.keys(this.defaultMap).filter((colorName) => colorName.startsWith(baseName));
  }

  init(appMap) {
    this.applicationColorMap = appMap;
  }

  /**
   * Gives complete color theme from current app config and any given customization.
   * Setting base colors (primary, secondary) will return updated variations (primary-light etc..)
   * @param customerColors Entries to override
   */
  getThemeColors(customerColors: {[colorId: string]: string} = null): {[colorId: string]: string} {

    const colors = {};
    for (const key in this.defaultMap) {
      if (this.defaultMap.hasOwnProperty(key)) {
        colors[key] = getColorFromMaps(key, this.defaultMap, this.applicationColorMap, customerColors);
      }
    }
    return colors;
  }

  overrideTheme(customerThemeMap, selector= 'body') {
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
