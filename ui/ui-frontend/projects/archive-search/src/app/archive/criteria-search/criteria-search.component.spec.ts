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
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { environment } from 'projects/archive-search/src/environments/environment';
import {
  BASE_URL,
  InjectorModule,
  LoggerModule,
  SearchCriteriaTypeEnum,
  TranslateWithOptionalTypeSuffixPipe,
  WINDOW_LOCATION,
} from 'vitamui-library';
import { VitamUICommonTestModule } from 'vitamui-library/testing';
import { CriteriaSearchComponent } from './criteria-search.component';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

describe('CriteriaSearchComponent', () => {
  let component: CriteriaSearchComponent;
  let fixture: ComponentFixture<CriteriaSearchComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      schemas: [NO_ERRORS_SCHEMA],
      imports: [
        InjectorModule,
        RouterTestingModule,
        VitamUICommonTestModule,
        BrowserAnimationsModule,
        LoggerModule.forRoot(),
        RouterTestingModule,
        CriteriaSearchComponent,
      ],
      providers: [
        TranslateWithOptionalTypeSuffixPipe,
        { provide: WINDOW_LOCATION, useValue: window.location },
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: environment, useValue: environment },
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
      ],
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
