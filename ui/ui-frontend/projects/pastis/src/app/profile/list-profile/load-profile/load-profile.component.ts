import { Component, Inject } from '@angular/core';
import { MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA, MatLegacyDialogModule } from '@angular/material/legacy-dialog';
import { FileSelectorComponent, PipesModule } from 'vitamui-library';
import { AsyncPipe, NgForOf, NgIf } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';

export interface LoadProfileConfig {
  title: string;
  subTitle: string;
  okLabel?: string;
  cancelLabel?: string;
  /**
   * Allowed extensions. Ex: ['.json', '.rng']
   */
  extensions?: string[];
  multipleFiles?: boolean;
}

@Component({
  selector: 'vitamui-load-profile',
  templateUrl: './load-profile.component.html',
  styleUrl: './load-profile.component.scss',
  standalone: true,
  imports: [FileSelectorComponent, NgIf, TranslateModule, NgForOf, PipesModule, MatLegacyDialogModule, AsyncPipe],
})
export class LoadProfileComponent {
  constructor(
    @Inject(MAT_DIALOG_DATA)
    public config: LoadProfileConfig,
  ) {}
}
