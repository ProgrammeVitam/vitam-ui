import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Event} from 'projects/vitamui-library/src/public-api';
import {tap} from 'rxjs/operators';
import {SearchService} from 'ui-frontend-common';

import {OperationApiService} from '../core/api/operation-api.service';
import {VitamUISnackBar, VitamUISnackBarComponent} from '../shared/vitamui-snack-bar';

@Injectable({
  providedIn: 'root'
})
export class ProbativeValueService extends SearchService<Event> {

  constructor(
    private operationApiService: OperationApiService,
    private snackBar: VitamUISnackBar,
    http: HttpClient) {
    super(http, operationApiService, 'ALL');
  }

  create(probativeValueRequest: any, headers: HttpHeaders) {
    for (const header in this.headers) {
      if (this.headers.hasOwnProperty(header)) {
        headers.set(header, this.headers.get(header));
      }
    }

    return this.operationApiService.runProbativeValue(probativeValueRequest, headers)
      .pipe(
        tap(
          () => {
            this.snackBar.openFromComponent(VitamUISnackBarComponent, {
              panelClass: 'vitamui-snack-bar',
              data: {type: 'probativeValueRun'},
              duration: 10000
            });
          },
          (error: any) => {
            console.log('error: ', error);
            if (!error || !error.error) {
              return;
            }
            this.snackBar.open(error.error.message, null, {
              panelClass: 'vitamui-snack-bar',
              duration: 10000
            });
          }
        )
      );
  }

  export(id: string, accessContractId: string) {
    this.operationApiService.downloadProbativeValue(id, new HttpHeaders({'X-Access-Contract-Id': accessContractId})).subscribe((blob) => {
      const element = document.createElement('a');
      element.href = window.URL.createObjectURL(blob);
      element.download = id + '.zip';
      element.style.visibility = 'hidden';
      document.body.appendChild(element);
      element.click();
      document.body.removeChild(element);
    });
  }
}
