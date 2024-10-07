import { Component, Input, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { Color, ThemeColorType, ThemeService } from 'vitamui-library';
import { Subscription } from 'rxjs';

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
  public backgroundColors: { id: string; label: string; isPrimaryLight?: boolean }[] = [];
  public THEME_COLORS = ThemeColorType;
  selectedBgColor: string;
  private primaryLightSubscription?: Subscription;

  constructor(private themeService: ThemeService) {}

  public ngOnInit(): void {
    this.baseColors = this.themeService.getBaseColors();

    this.colors = this.themeService.getThemeColors();

    this.backgroundColors = this.themeService.backgroundChoice.map((c: Color) => ({
      id: c.value,
      label: c.class,
      isDefault: c.isDefault,
      isPrimaryLight: c.isPrimaryLight,
    }));

    // We set original bg color value. If the value doesn't match available bg colors, it's probably because there's a custom primary color and the primary-light variant has been selected as bg color.
    const bgControl = this.formGroup.get(this.THEME_COLORS.VITAMUI_BACKGROUND);
    if (this.backgroundColors.map((c) => c.id).includes(bgControl.value)) {
      this.selectedBgColor = bgControl.value;
    } else {
      this.selectedBgColor = this.backgroundColors.find((c) => c.isPrimaryLight).id;
    }

    // Update value on init to make bg color automatically upgrade when changing primary color if primary-light bg color has been selected
    this.updateBackgroundColor(this.selectedBgColor);
  }

  updateBackgroundColor($event: any) {
    const find = this.backgroundColors.find((c) => c.id === $event);
    const isPrimaryLight = !find || find.isPrimaryLight;
    const bgControl = this.formGroup.get(this.THEME_COLORS.VITAMUI_BACKGROUND);
    if (isPrimaryLight) {
      // If primary-light has been selected, we must automatically upgrade bg color when primary color changes
      const primaryControl = this.formGroup.get(this.THEME_COLORS.VITAMUI_PRIMARY);
      bgControl.setValue(ThemeService.getPrimaryLight(primaryControl.value));
      this.primaryLightSubscription = primaryControl.valueChanges.subscribe((primaryColor) =>
        bgControl.setValue(ThemeService.getPrimaryLight(primaryColor)),
      );
    } else {
      this.primaryLightSubscription?.unsubscribe();
      bgControl.setValue($event);
    }
  }
}
