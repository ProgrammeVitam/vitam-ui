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

import { DatePipe } from '@angular/common';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NO_ERRORS_SCHEMA, Pipe, PipeTransform } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTreeModule } from '@angular/material/tree';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { environment } from 'projects/archive-search/src/environments/environment';
import { Observable, of } from 'rxjs';
import { BASE_URL, CriteriaDataType, CriteriaOperator, InjectorModule, LoggerModule, WINDOW_LOCATION } from 'ui-frontend-common';
import { ArchiveSharedDataServiceService } from '../../core/archive-shared-data-service.service';
import { ArchiveService } from '../archive.service';
import {
  PagedResult,
  SearchCriteria,
  SearchCriteriaStatusEnum,
  SearchCriteriaTypeEnum,
  SearchCriteriaValue
} from '../models/search.criteria';
import { VitamUISnackBar } from '../shared/vitamui-snack-bar';
import { ArchiveSearchComponent } from './archive-search.component';

@Pipe({ name: 'truncate' })
class MockTruncatePipe implements PipeTransform {
  transform(value: number): number {
    return value;
  }
}

const translations: any = { TEST: 'Mock translate test' };

class FakeLoader implements TranslateLoader {
  getTranslation(): Observable<any> {
    return of(translations);
  }
}

describe('ArchiveSearchComponent', () => {
  let component: ArchiveSearchComponent;
  let fixture: ComponentFixture<ArchiveSearchComponent>;
  const pagedResult: PagedResult = { pageNumbers: 1, facets: [], results: [], totalResults: 1 };

  const matDialogSpy = jasmine.createSpyObj('MatDialog', ['open']);
  matDialogSpy.open.and.returnValue({ afterClosed: () => of(true) });

  const snackBarSpy = jasmine.createSpyObj('MatSnackBar', ['open', 'openFromComponent']);

  const archiveServiceStub = {
    loadFilingHoldingSchemeTree: () => of([]),

    getOntologiesFromJson: () => of([]),

    searchArchiveUnitsByCriteria: () => of(pagedResult),

    hasArchiveSearchRole: () => of(true),

    getAccessContractById: () => of({}),
  };
  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        MatMenuModule,
        MatTreeModule,
        MatProgressSpinnerModule,
        MatSidenavModule,
        InjectorModule,
        LoggerModule.forRoot(),
        TranslateModule.forRoot({
          loader: { provide: TranslateLoader, useClass: FakeLoader },
        }),
        MatSnackBarModule,
        HttpClientTestingModule,
        RouterTestingModule,
      ],
      declarations: [ArchiveSearchComponent, MockTruncatePipe],
      providers: [
        FormBuilder,
        ArchiveSharedDataServiceService,
        DatePipe,
        { provide: ArchiveService, useValue: archiveServiceStub },
        {
          provide: ActivatedRoute,
          useValue: { params: of({ tenantIdentifier: 1 }), data: of({ appId: 'ARCHIVE_SEARCH_MANAGEMENT_APP' }) },
        },
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: VitamUISnackBar, useValue: snackBarSpy },
        { provide: WINDOW_LOCATION, useValue: window.location },
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: environment, useValue: environment },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ArchiveSearchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Submit-Click', () => {
    const searchCriteria: SearchCriteria = {
      key: 'Title',
      values: [
        {
          value: { value: 'Titre 1', id: 'Titre 1' },
          label: 'Titre 1',
          status: SearchCriteriaStatusEnum.NOT_INCLUDED,
          valueShown: true,
          valueTranslated: false,
          keyTranslated: false,
        },
      ],
      category: SearchCriteriaTypeEnum.FIELDS,
      operator: CriteriaOperator.EQ,
      dataType: CriteriaDataType.STRING,
      valueTranslated: false,
      keyTranslated: false,
    };

    const searchCriteriaValues: SearchCriteriaValue[] = [
      {
        value: { value: 'Titre 1', id: 'Titre 1' },
        label: 'Titre 1',
        status: SearchCriteriaStatusEnum.NOT_INCLUDED,
        valueTranslated: false,
        keyTranslated: false,
      },
      {
        value: { value: 'Titre2', id: 'Titre2' },
        label: 'Titre 2',
        status: SearchCriteriaStatusEnum.NOT_INCLUDED,
        valueShown: true,
        valueTranslated: false,
        keyTranslated: false,
      },
    ];

    searchCriteriaValues.sort((a: any, b: any) => {
      const valueA = a.value;
      const valueB = b.value;
      return valueA < valueB ? -1 : valueA > valueB ? 1 : 0;
    });

    beforeEach(() => {
      // Given
      const currentCriteria: Map<string, SearchCriteria> = new Map<string, SearchCriteria>();
      currentCriteria.set('Title', {
        key: 'Title',
        dataType: CriteriaDataType.STRING,
        keyTranslated: true,
        valueTranslated: false,
        values: [
          {
            value: { value: 'Titre2', id: 'Titre2' },
            label: 'Titre 2',
            status: SearchCriteriaStatusEnum.NOT_INCLUDED,
            valueShown: true,
            valueTranslated: false,
            keyTranslated: false,
          },
        ],
        category: SearchCriteriaTypeEnum.FIELDS,
        operator: CriteriaOperator.EQ,
      });

      component.searchCriterias = currentCriteria;
    });

    describe('addCriteria', () => {
      it('should add the new criteria to the criteria list having same key', () => {
        // When: add a new criteria value
        component.addCriteria(
          searchCriteria.key,
          searchCriteria.values[0].value,
          searchCriteria.values[0].value.value,
          false,
          CriteriaOperator.EQ,
          SearchCriteriaTypeEnum.FIELDS,
          searchCriteria.valueTranslated,
          CriteriaDataType.STRING,
          false
        );

        // Then: the new criteria should be added to the criteria list
        expect(component.searchCriterias.size).toEqual(1);
        const newCriteria = component.searchCriterias.get('Title').values;

        newCriteria.sort((a: any, b: any) => {
          const valueA = a.value;
          const valueB = b.value;
          return valueA < valueB ? -1 : valueA > valueB ? 1 : 0;
        });
      });
    });

    describe('submit', () => {
      it('should check all criteria as included when submit', () => {
        component.submit();
        component.searchCriterias.forEach((criteria) => {
          criteria.values.forEach((criteriaValue) => {
            expect(criteriaValue.status).toEqual(SearchCriteriaStatusEnum.IN_PROGRESS);
          });
        });
      });
    });
  });
});
