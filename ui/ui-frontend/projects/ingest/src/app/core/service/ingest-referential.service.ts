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
import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, forkJoin, of } from 'rxjs';
import { catchError, map, shareReplay } from 'rxjs/operators';
import { BASE_URL } from 'vitamui-library';
import { IngestReferentialNames } from '../../models/logbook-event.interface';

@Injectable({ providedIn: 'root' })
export class IngestReferentialService {
  private readonly baseUrl = inject(BASE_URL);
  private readonly http = inject(HttpClient);

  resolveNames(params: {
    originatingAgency?: string;
    submissionAgency?: string;
    archivalAgreement?: string;
    archivalProfile?: string;
  }): Observable<IngestReferentialNames> {
    const { originatingAgency, submissionAgency, archivalAgreement, archivalProfile } = params;

    const orig$ = originatingAgency ? this.getAgencyName(originatingAgency).pipe(shareReplay(1)) : of(undefined);
    const sub$ = submissionAgency ? (submissionAgency === originatingAgency ? orig$ : this.getAgencyName(submissionAgency)) : of(undefined);
    const contract$ = archivalAgreement ? this.getIngestContractName(archivalAgreement) : of(undefined);
    const profile$ = archivalProfile ? this.getProfileName(archivalProfile) : of(undefined);

    return forkJoin([orig$, sub$, contract$, profile$]).pipe(
      map(([originatingAgencyName, submissionAgencyName, archivalAgreementName, archivalProfileName]) => ({
        originatingAgencyName: originatingAgencyName ?? undefined,
        submissionAgencyName: submissionAgencyName ?? undefined,
        archivalAgreementName: archivalAgreementName ?? undefined,
        archivalProfileName: archivalProfileName ?? undefined,
      })),
    );
  }

  private getAgencyName(identifier: string): Observable<string | null> {
    return this.http.get<{ name: string }>(`${this.baseUrl}/agency/${identifier}`).pipe(
      map((agency) => agency?.name ?? null),
      catchError(() => of(null)),
    );
  }

  private getIngestContractName(identifier: string): Observable<string | null> {
    return this.http.get<{ name: string }>(`${this.baseUrl}/ingestcontract/${identifier}`).pipe(
      map((contract) => contract?.name ?? null),
      catchError(() => of(null)),
    );
  }

  private getProfileName(identifier: string): Observable<string | null> {
    const params = new HttpParams().set('embedded', 'ALL').set('criteria', JSON.stringify({ identifier }));
    return this.http.get<{ name: string }[]>(`${this.baseUrl}/profile`, { params }).pipe(
      map((profiles) => profiles?.[0]?.name ?? null),
      catchError(() => of(null)),
    );
  }
}
