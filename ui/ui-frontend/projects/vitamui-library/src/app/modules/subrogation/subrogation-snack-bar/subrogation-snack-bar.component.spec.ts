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
import { ComponentFixture, TestBed } from '@angular/core/testing';
import {
  MAT_LEGACY_SNACK_BAR_DATA as MAT_SNACK_BAR_DATA,
  MatLegacySnackBarModule as MatSnackBarModule,
  MatLegacySnackBarRef as MatSnackBarRef,
} from '@angular/material/legacy-snack-bar';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { EMPTY } from 'rxjs';
import { VitamUICommonTestModule } from '../../../../../testing/src';

import { SubrogationApiService } from '../../api/subrogation-api.service';
import { VitamUISnackBarService } from '../../components/vitamui-snack-bar';
import { BASE_URL, WINDOW_LOCATION } from '../../injection-tokens';
import { SubrogationSnackBarComponent } from './subrogation-snack-bar.component';

describe('SubrogationSnackBarComponent', () => {
  let component: SubrogationSnackBarComponent;
  let fixture: ComponentFixture<SubrogationSnackBarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, MatSnackBarModule, VitamUICommonTestModule],
      declarations: [SubrogationSnackBarComponent],
      providers: [
        { provide: MAT_SNACK_BAR_DATA, useValue: {} },
        { provide: BASE_URL, useValue: '/fakeapi' },
        { provide: MatSnackBarRef, useValue: { dismiss: () => {} } },
        { provide: Router, useValue: { navigate: () => {}, navigateByUrl: () => {}, url: 'subrogations/customers/customerId' } },
        { provide: WINDOW_LOCATION, useValue: {} },
        { provide: SubrogationApiService, useValue: {} },
        { provide: VitamUISnackBarService, useValue: {} },
        { provide: TranslateService, useValue: { instant: () => EMPTY } },
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SubrogationSnackBarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
