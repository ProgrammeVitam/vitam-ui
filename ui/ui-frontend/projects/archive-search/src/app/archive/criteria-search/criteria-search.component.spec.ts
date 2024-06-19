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

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatLegacySnackBarModule as MatSnackBarModule } from '@angular/material/legacy-snack-bar';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateModule } from '@ngx-translate/core';
import { environment } from 'projects/archive-search/src/environments/environment';
import { BASE_URL, InjectorModule, LoggerModule, SearchCriteriaTypeEnum, WINDOW_LOCATION } from 'vitamui-library';
import { VitamUICommonTestModule } from 'vitamui-library/testing';
import { CriteriaSearchComponent } from './criteria-search.component';

describe('CriteriaSearchComponent', () => {
  let component: CriteriaSearchComponent;
  let fixture: ComponentFixture<CriteriaSearchComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        InjectorModule,
        RouterTestingModule,
        HttpClientTestingModule,
        VitamUICommonTestModule,
        BrowserAnimationsModule,
        LoggerModule.forRoot(),
        RouterTestingModule,
        TranslateModule.forRoot(),
        MatSnackBarModule,
      ],
      declarations: [CriteriaSearchComponent],
      providers: [
        { provide: WINDOW_LOCATION, useValue: window.location },
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: environment, useValue: environment },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CriteriaSearchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('component should be created', () => {
    expect(component).toBeTruthy();
  });

  it('should return ACCESS_RULE as result', () => {
    const response = 'ACCESS_RULE';
    expect(component.getCategoryName(SearchCriteriaTypeEnum.ACCESS_RULE)).not.toBeNull();
    expect(component.getCategoryName(SearchCriteriaTypeEnum.ACCESS_RULE)).toEqual(response);
  });
});
