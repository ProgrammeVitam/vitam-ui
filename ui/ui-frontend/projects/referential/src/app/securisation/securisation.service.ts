import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {download, Event} from 'projects/vitamui-library/src/public-api';
import {SearchService} from 'ui-frontend-common';

import {OperationApiService} from '../core/api/operation-api.service';

@Injectable({
  providedIn: 'root'
})
export class SecurisationService extends SearchService<Event> {

  constructor(
    private operationApiService: OperationApiService,
    http: HttpClient) {
    super(http, operationApiService, 'ALL');
  }

  download(id: string, accessContractId: string) {
    this.operationApiService.downloadOperation(
      id,
      'TRACEABILITY',
      new HttpHeaders({'X-Access-Contract-Id': accessContractId})
    ).subscribe((blob) => {
      download(blob, 'report.zip');
    });
  }

  checkTraceabilityOperation(id: string, accessContract: string) {
    return this.operationApiService.checkTraceabilityOperation(id, accessContract);
  }

  getInfoFromTimestamp(timestamp: string) {
    return this.operationApiService.getInfoFromTimestamp(timestamp);
  }

}
