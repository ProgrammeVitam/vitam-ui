import { Injectable } from '@angular/core';
import {getColorFromMaps, hexToRgbString, ThemeColors} from './utils';


@Injectable({
  providedIn: 'root'
})
export class ThemeService {

  // Default theme
  defaultMap: ThemeColors = {
    'vitamui-primary': '#702382',
    'vitamui-primary-light': '',
    'vitamui-primary-light-20': '',
    'vitamui-secondary': '#7FA1D4',
    'vitamui-secondary-light': '',
    'vitamui-secondary-light-8': '',
    'vitamui-secondary-dark-5': ''
  };

  applicationColorMap;

  constructor() {
  }

  init(appMap) {
    this.applicationColorMap = appMap;
  }

  /**
   * Gives complete color theme from current app config and any given customization.
   * Setting base colors (primary, secondary) will return updated variations (primary-light etc..)
   * @param customerColors Entries to override
   */
  getThemeColors(customerColors = null): {[colorId: string]: string} {
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
