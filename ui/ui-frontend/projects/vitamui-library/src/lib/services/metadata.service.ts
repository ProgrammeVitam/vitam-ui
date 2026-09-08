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
import { Observable } from 'rxjs';

import { HttpHeaders } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';

import { MetadataApiService } from '../api/metadata-api.service';
import { Metadata } from '../models/metadata.interface';
import { VitamuiHttpHeaders } from '../../app/modules/vitamui-http-headers.enum';

@Injectable({
  providedIn: 'root',
})
export class MetadataService {
  private metadataApi = inject(MetadataApiService);

  get(tenantIdentifier: number, unitId: string): Observable<Metadata> {
    const headers = new HttpHeaders()
      .set(VitamuiHttpHeaders.X_TENANT_ID, tenantIdentifier.toString())
      .set(VitamuiHttpHeaders.X_ACCESS_CONTRACT_ID, 'hardCodedAccessContract');
    // FIXME: Use Root/Admin Access Contract ? Use Specific Value ? Let the user choose ? this.activeAccessContract.identifier

    return this.metadataApi.searchMetadata(unitId, headers);
  }

  compareVtag(tag1: { Key: string[]; Value: any[] }, tag2: { Key: string[]; Value: any[] }): number {
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

  sortVtag(vtagArray: Array<{ Key: string[]; Value: any[] }>): void {
    if (vtagArray) {
      vtagArray.sort(this.compareVtag);
    }
  }
}

function isArrayEmpty(arr: string[]) {
  return !arr || arr.length < 1;
}
