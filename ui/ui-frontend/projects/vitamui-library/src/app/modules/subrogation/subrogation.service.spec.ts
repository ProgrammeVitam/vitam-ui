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
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { inject, TestBed } from '@angular/core/testing';
import { MatLegacySnackBarModule as MatSnackBarModule } from '@angular/material/legacy-snack-bar';
import { Router } from '@angular/router';

import { TranslateService } from '@ngx-translate/core';

import { EMPTY } from 'rxjs';
import { VitamUICommonTestModule } from '../../../../testing/src';

import { AuthService } from '../auth.service';
import { VitamUISnackBarService } from '../components/vitamui-snack-bar/vitamui-snack-bar.service';
import { BASE_URL } from '../injection-tokens';
import { LoggerModule } from '../logger';
import { environment } from './../../../environments/environment';
import { ENVIRONMENT, SUBROGRATION_REFRESH_RATE_MS, WINDOW_LOCATION } from './../injection-tokens';
import { SubrogationService } from './subrogation.service';

describe('SubrogationService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, MatSnackBarModule, LoggerModule.forRoot(), VitamUICommonTestModule],
      providers: [
        SubrogationService,
        { provide: WINDOW_LOCATION, useValue: {} },
        { provide: Router, useValue: { navigate: () => {}, navigateByUrl: () => {}, url: 'subrogations/customers/customerId' } },
        { provide: BASE_URL, useValue: 'fake-api' },
        { provide: SUBROGRATION_REFRESH_RATE_MS, useValue: 100 },
        { provide: AuthService, useValue: {} },
        { provide: ENVIRONMENT, useValue: environment },
        { provide: TranslateService, useValue: { instant: () => EMPTY } },
        { provide: VitamUISnackBarService, useValue: { instant: () => EMPTY } },
      ],
    });
  });

  it('should be created', inject([SubrogationService], (service: SubrogationService) => {
    expect(service).toBeTruthy();
  }));
});
