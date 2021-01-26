import { Injectable } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { AppConfiguration } from '.';
import { AuthUser, ThemeDataType } from './models';
import { Color } from './models/customer/theme/color.interface';
import { convertLighten, getColorFromMaps, hexToRgb, hexToRgbString, ThemeColorType } from './utils';

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

  private baseColors: {[colorId in ThemeColorType]?: string} = {
    [ThemeColorType.VITAMUI_PRIMARY]: 'Couleur principale',
    [ThemeColorType.VITAMUI_SECONDARY]: 'Couleur secondaire',
    [ThemeColorType.VITAMUI_TERTIARY]: 'Couleur tertiaire',
    [ThemeColorType.VITAMUI_HEADER_FOOTER]: 'Couleur header/footer',
    [ThemeColorType.VITAMUI_BACKGROUND]: 'Couleur background'
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
  defaultMap: {[colordId in ThemeColorType]: string} = {
    [ThemeColorType.VITAMUI_PRIMARY]: '#604379',
    [ThemeColorType.VITAMUI_GREY]: '#9E9E9E',
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
  applicationColorMap: {[colorId: string]: string};

  // tslint:disable-next-line: variable-name
  private _backgroundChoice: Color[] = [
    {class: 'Foncé', value: '#0F0D2D'},
    {class: 'Blanc', value: '#FFFFFF'},
    {class: 'Clair', value: '#F5F5F5'},
    {class: 'Bleu clair', value: '#F5F7FC'},
  ];

  public get backgroundChoice(): Color[] { return this._backgroundChoice; }

  public getBaseColors(): {[colorId in ThemeColorType]?: string} {
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
            .find((color: Color) => color.value === conf.THEME_COLORS[ThemeColorType.VITAMUI_BACKGROUND]);
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

  private calculateFontColor(color: string): string {
    const rgbColor = hexToRgb(color);
    if ((rgbColor.r * 0.299 + rgbColor.g * 0.587 + rgbColor.b * 0.114) > 186) {
      return '#000000';
    } else {
      return '#ffffff';
    }
  }

  private add10Declinations(key: string, colors: {}, customerColors: {[colorId: string]: string}): void {
    // tslint:disable-next-line: variable-name
    const map = {...this.defaultMap, ...this.applicationColorMap, ...customerColors};
    const rgbValue = hexToRgb(map[key]);
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
    colors[key + '-font'] = this.calculateFontColor(map[key]); // primary/secondary/tertiary
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
  public getThemeColors(customerColors: {[colorId: string]: string} = null): {[colorId: string]: string} {
    const colors = {};
    for (const key in this.defaultMap) {
      if (this.defaultMap.hasOwnProperty(key)) {
        if (([ThemeColorType.VITAMUI_PRIMARY, ThemeColorType.VITAMUI_SECONDARY, ThemeColorType.VITAMUI_GREY] as string[]).includes(key)) {
          this.add10Declinations(key, colors, customerColors);
        } else if (key === ThemeColorType.VITAMUI_HEADER_FOOTER) {
          const map = {...this.defaultMap, ...this.applicationColorMap, ...customerColors};
          colors[key + '-font'] = this.calculateFontColor(map[key]);
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
