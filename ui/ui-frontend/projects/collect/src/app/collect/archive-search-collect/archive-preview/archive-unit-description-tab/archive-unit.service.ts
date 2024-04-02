/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 *
 *
 */

import { HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ArchiveUnitApiService } from './archive-unit-api.service';
import { JsonPatchDto, MultiJsonPatchDto, ArchiveUnit } from 'ui-frontend-common';

@Injectable({
  providedIn: 'root',
})
export class ArchiveUnitService {
  constructor(private archiveUnitApiService: ArchiveUnitApiService) {}

  /**
   * Updates many archive units asynchronously in one Vitam operation.
   * Can perform only add or replace operations on current archive units.
   *
   * @param transactionId transaction ID
   * @param archiveUnits archive units to update.
   * @param headers optionnal headers.
   * @returns a wrapped operation id.
   */
  asyncPartialUpdateArchiveUnits(
    transactionId: string,
    archiveUnits: ArchiveUnit[],
    headers?: HttpHeaders,
  ): Observable<{ operationId: String }> {
    return this.archiveUnitApiService.asyncPartialUpdateArchiveUnits(transactionId, archiveUnits, headers);
  }

  /**
   * Updates one archive unit asynchronously by using a jsonPatch in one Vitam operation.
   *
   * @param transactionId transaction ID
   * @param jsonPatchDto a jsonPatchDto.
   * @param headers optionnal headers.
   * @returns a wrapped operation id.
   */
  asyncPartialUpdateArchiveUnitByCommands(
    transactionId: string,
    jsonPatchDto: JsonPatchDto,
    headers?: HttpHeaders,
  ): Observable<{ operationId: String }> {
    return this.archiveUnitApiService.asyncPartialUpdateArchiveUnitByCommands(transactionId, jsonPatchDto, headers);
  }

  /**
   * Updates many archive unit asynchronously by using jsonPatches in one Vitam operation.
   *
   * @param transactionId transaction ID
   * @param multiJsonPatchDto a list of jsonPatchDto.
   * @param headers optionnal headers.
   * @returns a wrapped operation id.
   */
  asyncPartialUpdateArchiveUnitsByCommands(transactionId: string, multiJsonPatchDto: MultiJsonPatchDto, headers?: HttpHeaders) {
    return this.archiveUnitApiService.asyncPartialUpdateArchiveUnitsByCommands(transactionId, multiJsonPatchDto, headers);
  }
}
