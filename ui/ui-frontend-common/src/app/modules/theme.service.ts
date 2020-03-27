import { Injectable } from '@angular/core';
import {getColorFromMaps, ThemeColors} from './utils';

@Injectable({
  providedIn: 'root'
})
export class ThemeService {

  applicationColorMap;

  customerColorMap;

  themeColors: ThemeColors = {
    'vitamui-primary': '#fe4f02',
    'vitamui-primary-light': '#ff8559',
    'vitamui-primary-light-20': '#ffa789',
    'vitamui-secondary': '#5cbaa9',
    'vitamui-secondary-light': '#81cabd',
    'vitamui-secondary-light-8': '#7ac7b9',
    'vitamui-secondary-dark-5': '#52aa9a',

  };

  constructor() {
  }

  init(appMap, customerMap) {
    this.applicationColorMap = appMap;
    this.customerColorMap = customerMap;
    this.themeColors = this.getThemeColors(customerMap);
  }

  getThemeColors(customerColorMap: any) {

    const applicationColorMap = this.applicationColorMap;
    const defaultPrimary = this.themeColors['vitamui-primary'];
    const defaultSecondary = this.themeColors['vitamui-secondary'];

    return {
      'vitamui-primary': getColorFromMaps('vitamui-primary', defaultPrimary, applicationColorMap, customerColorMap),
      'vitamui-primary-light': getColorFromMaps('vitamui-primary-light', null, applicationColorMap, customerColorMap),
      'vitamui-primary-light-20': getColorFromMaps('vitamui-primary-light-20', null, applicationColorMap, customerColorMap),
      'vitamui-secondary': getColorFromMaps('vitamui-secondary', defaultSecondary, applicationColorMap, customerColorMap),
      'vitamui-secondary-light': getColorFromMaps('vitamui-secondary-light', null, applicationColorMap, customerColorMap),
      'vitamui-secondary-light-8': getColorFromMaps('vitamui-secondary-light-8', null, applicationColorMap, customerColorMap),
      'vitamui-secondary-dark-5': getColorFromMaps('vitamui-secondary-dark-5', null, applicationColorMap, customerColorMap)
    };

  }

  refresh(primary: string, secondary: string): void {
    this.themeColors = {
      'vitamui-primary': getColorFromMaps('vitamui-primary', primary, null, null),
      'vitamui-primary-light': getColorFromMaps('vitamui-primary-light', null, null, null),
      'vitamui-primary-light-20': getColorFromMaps('vitamui-primary-light-20', null, null, null),
      'vitamui-secondary': getColorFromMaps('vitamui-secondary', secondary, null, null),
      'vitamui-secondary-light': getColorFromMaps('vitamui-secondary-light', null, null, null),
      'vitamui-secondary-light-8': getColorFromMaps('vitamui-secondary-light-8', null, null, null),
      'vitamui-secondary-dark-5': getColorFromMaps('vitamui-secondary-dark-5', null, null, null)
    };
  }

}
