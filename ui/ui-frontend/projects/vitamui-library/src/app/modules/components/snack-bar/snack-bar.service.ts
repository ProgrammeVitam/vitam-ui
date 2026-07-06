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
import { ComponentType } from '@angular/cdk/portal';
import { Injectable, inject } from '@angular/core';
import { MatSnackBar, MatSnackBarRef } from '@angular/material/snack-bar';
import { TranslateService } from '@ngx-translate/core';
import { firstValueFrom } from 'rxjs';
import { ApplicationService } from '../../application.service';
import { Application } from '../../models/application/application.interface';
import { SnackBarComponent } from './snack-bar.component';
import { SnackBarAppButton, SnackBarUrlButton, SnackBarData } from './snack-bar.interface';

const DEFAULT_DURATION = 10_000;
const DOWNLOAD_STARTED_MESSAGE = 'DOWNLOAD.STARTED_MESSAGE';

@Injectable({
  providedIn: 'root',
})
export class SnackBarService {
  private matSnackBar = inject(MatSnackBar);
  private applicationService = inject(ApplicationService);
  private translateService = inject(TranslateService);

  public async open(data: SnackBarData<SnackBarUrlButton | SnackBarAppButton>): Promise<MatSnackBarRef<SnackBarComponent>> {
    data.message = this.getTranslateValue(data.translate, data.message, data.translateParams);
    data.buttons = await Promise.all(
      (data.buttons || [])?.map(async (button) => {
        const url = (button as SnackBarAppButton).appId
          ? await (async () => {
              const appButton = button as SnackBarAppButton;
              const application: Application = await firstValueFrom(this.applicationService.getAppById(appButton.appId));
              return appButton.path ? `${application.url}/${appButton.path.replace(/^\//, '')}` : application.url;
            })()
          : (button as SnackBarUrlButton).url;

        return {
          ...button,
          label: this.getTranslateValue(button.translate, button.label, button.translateParams),
          url: url,
        };
      }),
    );
    return this.openFromComponent(SnackBarComponent, data, data.duration);
  }

  public openFromComponent<T>(component: ComponentType<T>, data?: any, duration: number = DEFAULT_DURATION): MatSnackBarRef<T> {
    if (data && data.duration === undefined) {
      data.duration = DEFAULT_DURATION;
    }

    return this.matSnackBar.openFromComponent(component, { duration, data });
  }

  public notifyDownloadStarted(): void {
    this.open({
      message: DOWNLOAD_STARTED_MESSAGE,
      icon: 'vitamui-icon vitamui-icon-telecharger',
    });
  }

  public startDownload(url: string): void {
    this.notifyDownloadStarted();
    window.location.href = url;
  }

  /**
   * Retrieve translate key value if translate = true, else will return the raw string.
   */
  private getTranslateValue(translate: boolean, message: string, translateParams?: any): string {
    if (translate === undefined || translate) {
      return this.translateService.instant(message, translateParams);
    }

    return message;
  }
}
