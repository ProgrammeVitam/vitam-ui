/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
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
import { Injectable } from '@angular/core';
import { MatLegacySnackBar as MatSnackBar, MatLegacySnackBarRef as MatSnackBarRef } from '@angular/material/legacy-snack-bar';
import { TranslateService } from '@ngx-translate/core';
import { Observable } from 'rxjs';
import { map, take } from 'rxjs/operators';
import { ApplicationId } from '../../application-id.enum';
import { ApplicationService } from '../../application.service';
import { Application } from '../../models/application/application.interface';
import { VitamUISnackBarComponent } from './vitamui-snack-bar.component';
import { VitamuiSnackBarData } from './vitamui-snack-bar.interface';

const DEFAULT_DURATION = 10000;

@Injectable({
  providedIn: 'root',
})
export class VitamUISnackBarService {
  constructor(
    private matSnackBar: MatSnackBar,
    private applicationService: ApplicationService,
    private translateService: TranslateService,
  ) {}

  public open(data: VitamuiSnackBarData): MatSnackBarRef<VitamUISnackBarComponent> {
    data.message = this.getTranslateValue(data.translate, data.message, data.translateParams);
    return this.openFromComponent(VitamUISnackBarComponent, data, data.duration);
  }

  public openFromComponent<T>(component: ComponentType<T>, data?: any, duration: number = DEFAULT_DURATION): MatSnackBarRef<T> {
    if (data && data.duration === undefined) {
      data.duration = DEFAULT_DURATION;
    }

    return this.matSnackBar.openFromComponent(component, { panelClass: 'vitamui-snack-bar', duration, data });
  }

  public openWithAppUrl(
    data: VitamuiSnackBarData,
    appId: ApplicationId | any,
    urlName: string,
    urlParams?: Map<string, string>,
  ): Observable<MatSnackBarRef<VitamUISnackBarComponent>> {
    return this.applicationService.getAppById(appId).pipe(
      map((application: Application) => {
        data.message = this.getTranslateValue(data.translate, data.message, data.translateParams);
        urlName = this.getTranslateValue(data.translate, urlName, urlParams);
        let url = application.url;

        if (urlParams) {
          let urlWithParams = application.url;
          for (const [key, value] of urlParams.entries()) {
            urlWithParams += '/' + key + '/' + value;
          }

          url = urlWithParams;
        }

        data.message = data.message + ' <a href="' + url + '">' + urlName + '</a>';

        return this.open(data);
      }),
      take(1),
    );
  }

  public openWithAppUrlBtn(
    data: VitamuiSnackBarData,
    appId: ApplicationId | any,
    urlName: string,
  ): Observable<MatSnackBarRef<VitamUISnackBarComponent>> {
    return this.applicationService.getAppById(appId).pipe(
      map((application: Application) => {
        data.message = this.getTranslateValue(data.translate, data.message, data.translateParams);
        urlName = this.getTranslateValue(data.translate, urlName);
        data.htmlContent =
          '<a href="' + application.url + '"><button class="btn contrast contrast-primary mr-3">' + urlName + '</button></a>';

        return this.open(data);
      }),
      take(1),
    );
  }

  public openWithStringUrl(
    data: VitamuiSnackBarData,
    url: string,
    urlName: string,
    cssClass?: string,
    closeOnClick: boolean = false,
  ): MatSnackBarRef<VitamUISnackBarComponent> {
    data.message = this.getTranslateValue(data.translate, data.message, data.translateParams);
    urlName = this.getTranslateValue(data.translate, urlName);

    const cssCl = cssClass ? 'class="' + cssClass + '"' : '';
    const onClick = closeOnClick ? '(click)="close()"' : '';

    data.message = data.message + ' <a href="' + url + '" ' + cssCl + ' ' + onClick + '">' + urlName + '</a>';
    return this.open(data);
  }

  public openWithCallback(data: VitamuiSnackBarData, callBack: Observable<any>, urlName: string): MatSnackBarRef<VitamUISnackBarComponent> {
    data.message = this.getTranslateValue(data.translate, data.message, data.translateParams);
    urlName = this.getTranslateValue(data.translate, urlName);

    data.htmlContent = '<a class="underline">' + urlName + '</a>';
    data.callBack = callBack;

    return this.open(data);
  }

  /**
   * Retreive translate key value if translate = true, else will return the raw string.
   */
  private getTranslateValue(translate: boolean, message: string, translateParams?: any): string {
    if (translate === undefined || translate) {
      return this.translateService.instant(message, translateParams);
    }

    return message;
  }
}
