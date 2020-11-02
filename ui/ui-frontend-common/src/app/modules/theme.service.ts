import { Injectable } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { AppConfiguration } from '.';
import { AuthUser, ThemeDataType } from './models';
import { Color } from './models/customer/theme/color.interface';
import { convertLighten, getColorFromMaps, hexToRgb, hexToRgbString, ThemeColors } from './utils';

export interface Theme {
  colors: {[colorId: string]: string};
  headerUrl?: SafeResourceUrl;
  footerUrl?: SafeResourceUrl;
  portalUrl?: SafeResourceUrl;
  portalMessage: string;
  portalTitle: string;
}

@Injectable({
  providedIn: 'root'
})
export class ThemeService {

  public get defaultTheme(): Theme {
    return this._defaultTheme;
  }

  public set defaultTheme(theme: Theme) { this._defaultTheme = theme; }

  constructor(
    private domSanitizer: DomSanitizer,
  ) { }

  private baseColors: {[colorId: string]: string} = {
    'vitamui-primary': 'Couleur principale',
    'vitamui-secondary': 'Couleur secondaire',
    'vitamui-tertiary': 'Couleur tertiaire',
    'vitamui-header-footer': 'Couleur header/footer',
    'vitamui-background': 'Couleur background',
  };

  // tslint:disable-next-line: variable-name
  private _defaultTheme: Theme = {
    colors: {},
    headerUrl: '',
    footerUrl: '',
    portalUrl: '',
    portalMessage: '',
    portalTitle: ''
  };

  // Default theme
  defaultMap: ThemeColors = {
    'vitamui-primary': '#604379',
    'vitamui-primary-light': '',
    'vitamui-primary-light-20': '',
    'vitamui-primary-dark': '',

    'vitamui-secondary': '#65B2E4',
    'vitamui-secondary-light': '',
    'vitamui-secondary-light-8': '',
    'vitamui-secondary-dark-5': '',

    'vitamui-tertiary': '#E7304D',
    'vitamui-header-footer': '#604379',
    'vitamui-background': '#F5F5F5',
  };

  // Theme for current app configuration
  applicationColorMap: {[colorId: string]: string};

  // tslint:disable-next-line: variable-name
  private _backgroundChoice: Color[] = [
    {class: 'Foncé', value: '#0F0D2D'},
    {class: 'Blanc', value: '#FFFFFF'},
    {class: 'Clair', value: '#F5F5F5'},
  ];

  public get backgroundChoice(): Color[] { return this._backgroundChoice; }

  public getBaseColors(): { [p: string]: string } {
    return this.baseColors;
  }

  public getVariationColorsNames(baseName: string): string[] {
    return Object.keys(this.defaultMap).filter((colorName) => colorName.startsWith(baseName));
  }

  public init(conf: AppConfiguration, customerColorMap: {[colorId: string]: string}): void {
    this.applicationColorMap = conf.THEME_COLORS;

    this.overrideTheme(customerColorMap);
    if (conf) {
      this.defaultTheme = {
        colors: conf.THEME_COLORS,
        portalMessage: conf.PORTAL_MESSAGE,
        portalTitle: conf.PORTAL_TITLE,
        headerUrl: this.domSanitizer.bypassSecurityTrustUrl('data:image/*;base64,' + conf.HEADER_LOGO),
        footerUrl: this.domSanitizer.bypassSecurityTrustUrl('data:image/*;base64,' + conf.FOOTER_LOGO),
        portalUrl: this.domSanitizer.bypassSecurityTrustUrl('data:image/*;base64,' + conf.PORTAL_LOGO),
      };

      // init default background
      const defaultBackground = this.backgroundChoice
            .find((color: Color) => color.value === conf.THEME_COLORS['vitamui-background']);
      if (defaultBackground) {
            defaultBackground.isDefault = true;
      }
    }
  }

  public overloadLocalTheme(colors: {[colorId: string]: string}, selectorToOver: string): void {
    const selector: HTMLElement = document.querySelector(selectorToOver);
    for (const key in colors) {
      if (colors.hasOwnProperty(key) && selector != null) {
        selector.style.setProperty('--' + key, colors[key]);
      }
    }
  }

  public getData(authUser: AuthUser, type: string): string | SafeResourceUrl {
    const userAuthGraphicIdentity = authUser && authUser.basicCustomer && authUser.basicCustomer.graphicIdentity
      ? authUser.basicCustomer.graphicIdentity
      : null;
    switch (type) {
      case ThemeDataType.PORTAL_MESSAGE: return userAuthGraphicIdentity
        && userAuthGraphicIdentity.portalMessage && userAuthGraphicIdentity.hasCustomGraphicIdentity
        ? userAuthGraphicIdentity.portalMessage
        : this.defaultTheme.portalMessage;
                                         break;
      case ThemeDataType.PORTAL_TITLE: return userAuthGraphicIdentity
        && userAuthGraphicIdentity.portalTitle && userAuthGraphicIdentity.hasCustomGraphicIdentity
        ? userAuthGraphicIdentity.portalTitle
        : this.defaultTheme.portalTitle;
                                       break;
      case ThemeDataType.PORTAL_LOGO: return userAuthGraphicIdentity
        && userAuthGraphicIdentity.portalDataBase64 && userAuthGraphicIdentity.hasCustomGraphicIdentity
        ? this.domSanitizer.bypassSecurityTrustUrl('data:image/*;base64,' + userAuthGraphicIdentity.portalDataBase64)
        : this.defaultTheme.portalUrl;
                                      break;
      case ThemeDataType.HEADER_LOGO: return userAuthGraphicIdentity
        && userAuthGraphicIdentity.headerDataBase64 && userAuthGraphicIdentity.hasCustomGraphicIdentity
        ? this.domSanitizer.bypassSecurityTrustUrl('data:image/*;base64,' + userAuthGraphicIdentity.headerDataBase64)
        : this.defaultTheme.headerUrl;
                                      break;
      case ThemeDataType.FOOTER_LOGO: return userAuthGraphicIdentity
        && userAuthGraphicIdentity.footerDataBase64 && userAuthGraphicIdentity.hasCustomGraphicIdentity
        ? this.domSanitizer.bypassSecurityTrustUrl('data:image/*;base64,' + userAuthGraphicIdentity.footerDataBase64)
        : this.defaultTheme.footerUrl;
                                      break;
        default: return;
    }
  }

  private add10Declinations(key: string, colors: {}, customerColors: {[colorId: string]: string}): void {
    const map = customerColors ? customerColors : this.applicationColorMap;
    const rgbValue = hexToRgb(map[key]);
    // consider hs-L from color key as 500
    colors[key + '-900'] = convertLighten(rgbValue, -32);
    colors[key + '-800'] = convertLighten(rgbValue, -24);
    colors[key + '-700'] = convertLighten(rgbValue, -16);
    colors[key + '-600'] = convertLighten(rgbValue, -8);
    colors[key + '-400'] = convertLighten(rgbValue, 8);
    colors[key + '-300'] = convertLighten(rgbValue, 16);
    colors[key + '-200'] = convertLighten(rgbValue, 24);
    colors[key + '-100'] = convertLighten(rgbValue, 32);
    colors[key + '-50'] = convertLighten(rgbValue, 40);
  }

  /**
   * Gives complete color theme from current app config and any given customization.
   * Setting base colors (primary, secondary) will return updated variations (primary-light etc..)
   * @param customerColors Entries to override
   */
  public getThemeColors(customerColors: {[colorId: string]: string} = null): {[colorId: string]: string} {
    const colors = {};
    for (const key in this.defaultMap) {
      if (this.defaultMap.hasOwnProperty(key)) {
        if (key === 'vitamui-primary' || key === 'vitamui-secondary') {
          this.add10Declinations(key, colors, customerColors);
        }
        colors[key] = getColorFromMaps(key, this.defaultMap, this.applicationColorMap, customerColors);
      }
    }
    return colors;
  }

  public overrideTheme(customerThemeMap, selector= 'body'): void {
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
