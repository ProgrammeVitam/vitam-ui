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
import { NO_ERRORS_SCHEMA } from '@angular/compiler';
import { Pipe, PipeTransform } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { Observable, of } from 'rxjs';
import {
  CriteriaDataType, CriteriaOperator, InjectorModule, LoggerModule, SearchCriteriaEltements, SearchCriteriaHistory, SearchCriteriaTypeEnum
} from 'ui-frontend-common';
import { environment } from '../../../../environments/environment.prod';
import { ArchiveSharedDataService } from '../../../core/archive-shared-data.service';
import { VitamUISnackBar } from '../../shared/vitamui-snack-bar';
import { SearchCriteriaSaverComponent } from './search-criteria-saver.component';
import { SearchCriteriaSaverService } from './search-criteria-saver.service';

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

describe('SearchCriteriaSaverComponent', () => {
  let component: SearchCriteriaSaverComponent;
  let fixture: ComponentFixture<SearchCriteriaSaverComponent>;

  const matDialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['open']);
  matDialogRefSpy.open.and.returnValue({ afterClosed: () => of(true) });

  const matDialogSpy = jasmine.createSpyObj('MatDialog', ['open']);
  matDialogSpy.open.and.returnValue({ afterClosed: () => of(true) });

  const snackBarSpy = jasmine.createSpyObj('MatSnackBar', ['open', 'openFromComponent']);

  const SearchCriteriaSaverServiceStub = {
    getSearchCriteriaHistory: () => of([]),
    deleteSearchCriteriaHistory: () => of(),
    updateSearchCriteriaHistory: () => of(),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        MatSnackBarModule,
        InjectorModule,
        LoggerModule.forRoot(),
        TranslateModule.forRoot({
          loader: { provide: TranslateLoader, useClass: FakeLoader },
        }),
        RouterTestingModule,
      ],
      declarations: [SearchCriteriaSaverComponent, MockTruncatePipe],
      providers: [
        FormBuilder,
        HttpClientTestingModule,
        ArchiveSharedDataService,
        DatePipe,
        { provide: MatDialogRef, useValue: matDialogRefSpy },
        { provide: MatDialog, useValue: matDialogRefSpy },
        { provide: VitamUISnackBar, useValue: snackBarSpy },
        { provide: SearchCriteriaSaverService, useValue: SearchCriteriaSaverServiceStub },
        { provide: MAT_DIALOG_DATA, useValue: {} },
        {
          provide: ActivatedRoute,
          useValue: { params: of({ tenantIdentifier: 1 }), data: of({ appId: 'ARCHIVE_SEARCH_MANAGEMENT_APP' }) },
        },
        { provide: environment, useValue: environment },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SearchCriteriaSaverComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
    expect(component.nameControl).toEqual('');
  });

  it('should create new searchCriteria', () => {
    component.createNewCriteria();
    expect(component.ToUpdate).toBeFalsy();
  });

  it('savingDate of each criteria save should not be null', () => {
    // Given
    let searchCriteriaHistory: SearchCriteriaHistory;
    const criteriaList: SearchCriteriaEltements[] = [];
    searchCriteriaHistory = {
      id: 'id ',
      name: 'save name',
      savingDate: new Date().toISOString(),
      searchCriteriaList: criteriaList,
    };

    // When
    component.searchCriteriaHistory = searchCriteriaHistory;
    component.criteriaToUpdate = searchCriteriaHistory;

    component.update();

    // Then
    expect(component.criteriaToUpdate.savingDate).toBeDefined();
    expect(component.criteriaToUpdate.savingDate).not.toBeNull();
  });

  it('should showScrollFilter be false', () => {
    component.out('scroll-filters');
    expect(component.showScrollFilter).toBeFalsy();
    expect(component.noScroll).toBeFalsy();
  });

  it('should call updateSearchCriteriaHistory', () => {
    // Gievn
    const criteriaToUpdate: SearchCriteriaHistory = {
      id: 'criteriaToUpdateId',
      name: 'criteriaToUpdateName',
      userId: 'userId',
      savingDate: 'criteriaToUpdateDate',
      searchCriteriaList: [],
    };
    const searchCriteriaHistory: SearchCriteriaHistory = {
      id: 'searchCriteriaSavedId',
      name: 'searchCriteriaSaveedName',
      userId: 'userId',
      savingDate: new Date().toISOString(),
      searchCriteriaList: [],
    };
    spyOn(SearchCriteriaSaverServiceStub, 'updateSearchCriteriaHistory').and.callThrough();

    // When
    component.criteriaToUpdate = criteriaToUpdate;
    component.searchCriteriaHistory = searchCriteriaHistory;
    component.update();

    // Then
    expect(SearchCriteriaSaverServiceStub.updateSearchCriteriaHistory).toHaveBeenCalled();
  });

  it('should initialize value parameters after cancel action', () => {
    component.cancel();

    expect(component.criteriaToUpdate).toBeNull();
    expect(component.updateConfirm).toBeFalsy();
    expect(component.ToUpdate).toBeTruthy();
  });

  it('should return APPRAISAL_RULE', () => {
    const APPRAISAL_RULE = 'APPRAISAL_RULE';
    const response = component.getCategoryName(SearchCriteriaTypeEnum.APPRAISAL_RULE);
    expect(response).not.toBeNull();
    expect(response).toEqual(APPRAISAL_RULE);
  });

  describe('DOM', () => {
    it('should have 3 rows ', () => {
      // When
      const nativeElement = fixture.nativeElement;
      const elementRow = nativeElement.querySelectorAll('.row');

      // Then
      expect(elementRow.length).toBe(3);
    });

    it('should return true after over scroll-results event', () => {
      // When
      component.over('scroll-results');

      // Then
      expect(component.showScroll).toBeTruthy();
      expect(component.noScroll).toBeFalsy();
    });

    it('should return true after over scroll-filters event', () => {
      // When
      component.over('scroll-filters');

      // Then
      expect(component.showScrollFilter).toBeTruthy();
      expect(component.noScroll).toBeFalsy();
    });
  });

  describe('filters size', () => {
    let searchCriteriaHistory$: SearchCriteriaHistory[] = [];
    let criteriaList$: SearchCriteriaEltements[] = [];
    beforeEach(() => {
      // Given
      criteriaList$ = [
        {
          criteria: 'Title',
          values: [
            { value: 'vdsvdv', id: 'vdsvdv' },
            { value: 'dfbdfd', id: 'dfbdfd' },
          ],
          category: 'FIELDS',
          dataType: CriteriaDataType.STRING,
          operator: CriteriaOperator.EQ,
          keyTranslated: false,
          valueTranslated: false,
        },
        {
          criteria: 'Description',
          values: [{ value: 'dfddfgdfdgg', id: 'dfddfgdfdgg' }],
          category: 'FIELDS',
          dataType: CriteriaDataType.STRING,
          operator: CriteriaOperator.EQ,
          keyTranslated: false,
          valueTranslated: false,
        },
        {
          criteria: '#opi',
          values: [
            { value: 'dfgdfgdfgdfgdfgfdg', id: 'dfgdfgdfgdfgdfgfdg' },
            { value: 'gggggggggg', id: 'gggggggggg' },
          ],
          category: 'FIELDS',
          dataType: CriteriaDataType.STRING,
          operator: CriteriaOperator.EQ,
          keyTranslated: false,
          valueTranslated: false,
        },
        {
          criteria: 'NODE',
          values: [
            { value: 'node1', id: 'node1' },
            { value: 'node2', id: 'node2' },
            { value: 'node3', id: 'node3' },
          ],
          category: 'NODES',
          dataType: CriteriaDataType.STRING,
          operator: CriteriaOperator.EQ,
          keyTranslated: false,
          valueTranslated: false,
        },
      ];

      searchCriteriaHistory$ = [
        {
          id: 'id1',
          name: 'First Svae',
          savingDate: new Date().toISOString(),
          searchCriteriaList: criteriaList$,
        },
        {
          id: 'id2',
          name: 'Second Svae',
          savingDate: new Date().toISOString(),
          searchCriteriaList: criteriaList$,
        },
      ];
    });

    it('should get filters size searchCriteria', () => {
      const filterSize = component.getNbFilters(searchCriteriaHistory$[0]);
      expect(filterSize).toEqual(8);
    });
  });
});
