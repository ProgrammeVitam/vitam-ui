import {Observable} from 'rxjs';

import {HttpHeaders} from '@angular/common/http';
import {Injectable} from '@angular/core';

import {MetadataApiService} from '../api/metadata-api.service';
import {Metadata} from '../models/metadata.interface';

@Injectable({
  providedIn: 'root'
})
export class MetadataService {

  constructor(private metadataApi: MetadataApiService) {
  }

  get(tenantIdentifier: number, unitId: string): Observable<Metadata> {
    const headers = new HttpHeaders({
      'X-Tenant-Id': tenantIdentifier.toString(),
      // FIXME: Use Root/Admin Access Contract ? Use Specific Value ? Let the user choose ?
      'X-Access-Contract-Id': 'hardCodedAccessContract'/*this.activeAccessContract.identifier*/
    });

    return this.metadataApi.searchMetadata(unitId, headers);
  }

  compareVtag(tag1: { Key: string[], Value: any[] }, tag2: { Key: string[], Value: any[] }): number {
    if (isArrayEmpty(tag1.Key) && isArrayEmpty(tag2.Key)) {
      return 0;
    } else if (isArrayEmpty(tag2.Key) && !isArrayEmpty(tag1.Key)) {
      return -1;
    } else if (isArrayEmpty(tag1.Key) && !isArrayEmpty(tag2.Key)) {
      return 1;
    } else if (tag1.Key[0] < tag2.Key[0]) {
      return -1;
    } else if (tag2.Key[0] < tag1.Key[0]) {
      return 1;
    }

    return 0;
  }

  sortVtag(vtagArray: Array<{ Key: string[], Value: any[] }>): void {
    if (vtagArray) {
      vtagArray.sort(this.compareVtag);
    }
  }

}

function isArrayEmpty(arr: string[]) {
  return !arr || arr.length < 1;
}
