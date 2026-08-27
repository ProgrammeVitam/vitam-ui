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
import { HttpTestingController } from '@angular/common/http/testing';
import { Type } from '@angular/core';
import { inject, TestBed } from '@angular/core/testing';
import { BASE_URL } from 'vitamui-library';
import { IngestReferentialService } from './ingest-referential.service';

describe('IngestReferentialService', () => {
  let httpTestingController: HttpTestingController;
  let service: IngestReferentialService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [],
      providers: [IngestReferentialService],
    });

    httpTestingController = TestBed.inject(HttpTestingController as Type<HttpTestingController>);
    service = TestBed.inject(IngestReferentialService);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should be created', inject([IngestReferentialService], (s: IngestReferentialService) => {
    expect(s).toBeTruthy();
  }));

  describe('resolveNames', () => {
    it('should make only one agency HTTP call when submissionAgency equals originatingAgency', () => {
      service.resolveNames({ originatingAgency: 'AG-001', submissionAgency: 'AG-001' }).subscribe(
        (names) => {
          expect(names.originatingAgencyName).toBe('Agency One');
          expect(names.submissionAgencyName).toBe('Agency One');
        },
        (e: unknown) => {
          throw e;
        },
      );

      httpTestingController.expectOne('/fake-api/agency/AG-001').flush({ name: 'Agency One' });
    });

    it('should return undefined for a failing HTTP call without aborting other resolutions', () => {
      service.resolveNames({ originatingAgency: 'AG-001', archivalAgreement: 'IC-001' }).subscribe(
        (names) => {
          expect(names.originatingAgencyName).toBeUndefined();
          expect(names.archivalAgreementName).toBe('Contract One');
        },
        (e: unknown) => {
          throw e;
        },
      );

      httpTestingController
        .expectOne('/fake-api/agency/AG-001')
        .flush('Server Error', { status: 500, statusText: 'Internal Server Error' });
      httpTestingController.expectOne('/fake-api/ingestcontract/IC-001').flush({ name: 'Contract One' });
    });
  });
});
