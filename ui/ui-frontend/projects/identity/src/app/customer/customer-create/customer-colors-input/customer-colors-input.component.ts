import {Component, Input, OnInit} from '@angular/core';
import {FormGroup} from '@angular/forms';
import {Color, hexToRgb, rgbToHsl, ThemeColorType, ThemeService} from 'ui-frontend-common';


@Component({
  selector: 'app-customer-colors-input',
  templateUrl: './customer-colors-input.component.html',
  styleUrls: ['./customer-colors-input.component.scss'],
})
export class CustomerColorsInputComponent implements OnInit {

  @Input() formGroup: FormGroup;

  @Input() themeOverloadSelector: string;

  @Input() disabled: boolean;

  @Input() isUpdating = false;

  public colors: { [colorId: string]: string };

  public baseColors: { [colorId in ThemeColorType]?: string };
  public displayTertiary = false;
  public backgroundColors: {id: string, label: string}[] = [];
  public THEME_COLORS = ThemeColorType;

  constructor(private themeService: ThemeService) {}

  public ngOnInit(): void {

    this.baseColors = this.themeService.getBaseColors();

    // Build dynamic formgroup and variations names array
    this.colors = this.themeService.getThemeColors();

    this.backgroundColors = this.themeService.backgroundChoice
      .map((c: Color) => ({id: c.value, label: c.class, isDefault: c.isDefault}));
  }

  public isColor500(color: string): boolean {
    const rgbValue = hexToRgb(color);
    if (rgbValue) {
      const hslValue = rgbToHsl(rgbValue);
      return hslValue && (hslValue.l <= 60 && hslValue.l >= 40);
    }
    return false;
  }
}
