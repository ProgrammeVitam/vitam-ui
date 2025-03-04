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
import { Component, Input, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { Color, Option, ThemeColorType, ThemeService } from 'vitamui-library';
import { Subscription } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-customer-colors-input',
  templateUrl: './customer-colors-input.component.html',
  styleUrls: ['./customer-colors-input.component.scss'],
  standalone: false,
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

  backgroundColorsOptions: Option[];

  constructor(
    private themeService: ThemeService,
    private translateService: TranslateService,
  ) {}

  public ngOnInit(): void {
    this.baseColors = this.themeService.getBaseColors();

    this.colors = this.themeService.getThemeColors();

    this.backgroundColors = this.themeService.backgroundChoice.map((c: Color) => ({
      id: c.value,
      label: c.class,
      isDefault: c.isDefault,
      isPrimaryLight: c.isPrimaryLight,
    }));
    this.backgroundColorsOptions = this.backgroundColors.map((color) => ({
      key: color.id,
      label: ((color as any).isDefault ? `${this.translateService.instant('COMMON_SELECT.DEFAULT_LABEL')} ` : '') + color.label,
      isPrimaryLight: color.isPrimaryLight,
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
