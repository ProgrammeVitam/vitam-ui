import { Injectable } from '@angular/core';
import {getColorFromMaps, ThemeColors} from './utils';


@Injectable({
  providedIn: 'root'
})
export class ThemeService {

  defaultMap: ThemeColors = {
    'vitamui-primary': '#fe4f02',
    'vitamui-primary-light': '#ff8559',
    'vitamui-primary-light-20': '#ffa789',
    'vitamui-secondary': '#5cbaa9',
    'vitamui-secondary-light': '#81cabd',
    'vitamui-secondary-light-8': '#7ac7b9',
    'vitamui-secondary-dark-5': '#52aa9a',

  };

  applicationColorMap;

  constructor() {
  }

  init(appMap) {
    this.applicationColorMap = appMap;
  }

  getThemeColors(customerColors = null): {[key: string]: string} {
    const colors = {};
    for (const key in this.defaultMap) {
      if (this.defaultMap.hasOwnProperty(key)) {
        colors[key] = getColorFromMaps(key, this.defaultMap, this.applicationColorMap, customerColors);
      }
    }
    return colors;
  }

}
