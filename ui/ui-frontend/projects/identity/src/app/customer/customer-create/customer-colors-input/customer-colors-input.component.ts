import { Component, Input, OnInit } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { Color, ThemeColorType, ThemeService, VitamuiCommonSelectComponent } from 'vitamui-library';
import { TranslateModule } from '@ngx-translate/core';
import { InputColorComponent } from './input-color/input-color.component';
import { NgFor, NgIf } from '@angular/common';

@Component({
  selector: 'app-customer-colors-input',
  templateUrl: './customer-colors-input.component.html',
  styleUrls: ['./customer-colors-input.component.scss'],
  standalone: true,
  imports: [ReactiveFormsModule, NgFor, InputColorComponent, NgIf, VitamuiCommonSelectComponent, TranslateModule],
})
export class CustomerColorsInputComponent implements OnInit {
  @Input() formGroup: FormGroup;

  @Input() themeOverloadSelector: string;

  @Input() disabled: boolean;

  @Input() isUpdating = false;

  public colors: { [colorId: string]: string };

  public baseColors: { [colorId in ThemeColorType]?: string };
  public displayTertiary = false;
  public backgroundColors: { id: string; label: string }[] = [];
  public THEME_COLORS = ThemeColorType;

  constructor(private themeService: ThemeService) {}

  public ngOnInit(): void {
    this.baseColors = this.themeService.getBaseColors();

    this.colors = this.themeService.getThemeColors();

    this.backgroundColors = this.themeService.backgroundChoice.map((c: Color) => ({ id: c.value, label: c.class, isDefault: c.isDefault }));
  }
}
