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
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { TranslateModule } from '@ngx-translate/core';
import { BehaviorSubject, of } from 'rxjs';
import {
  AgenciesModule,
  BASE_URL,
  InjectorModule,
  ItemNode,
  LoggerModule,
  SchemaElement,
  SchemaService,
  SearchCriteriaAddAction,
  VitamUISnackBarService,
} from 'vitamui-library';
import { ArchiveSharedDataService } from '../../../core/archive-shared-data.service';
import { ManagementRulesSharedDataService } from '../../../core/management-rules-shared-data.service';
import { SimpleCriteriaSearchComponent } from './simple-criteria-search.component';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { ArchiveService } from '../../archive.service';
import { ActivatedRoute } from '@angular/router';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

describe('SimpleCriteriaSearchComponent', () => {
  let component: SimpleCriteriaSearchComponent;
  let fixture: ComponentFixture<SimpleCriteriaSearchComponent>;

  const archiveExchangeDataServiceMock = {
    addSimpleSearchCriteriaSubject: () => of(),
    receiveRemoveFromChildSearchCriteriaSubject: () => of(),
    searchCriteria$: of(),
  };

  const archiveServiceStub = {
    loadFilingHoldingSchemeTree: () => of([]),
    hasArchiveSearchRole: () => of(true),
    getAccessContractById: () => of({}),
  };

  const managementRulesSharedDataServiceMock = {
    getCriteriaSearchListToSave: () => of([]),
  };

  let schema: BehaviorSubject<ItemNode<SchemaElement>[]>;

  beforeEach(async () => {
    const matDialogSpy = jasmine.createSpyObj('MatDialog', ['open']);
    matDialogSpy.open.and.returnValue({ afterClosed: () => of(true) });

    schema = new BehaviorSubject<ItemNode<SchemaElement>[]>([]);
    const schemaServiceMock = {
      getDescriptiveSchemaTree: () => schema,
    };

    await TestBed.configureTestingModule({
      declarations: [SimpleCriteriaSearchComponent],
      imports: [InjectorModule, TranslateModule.forRoot(), AgenciesModule, MatSnackBarModule, LoggerModule.forRoot()],
      providers: [
        FormBuilder,
        { provide: ArchiveService, useValue: archiveServiceStub },
        { provide: ArchiveSharedDataService, useValue: archiveExchangeDataServiceMock },
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: ManagementRulesSharedDataService, useValue: managementRulesSharedDataServiceMock },
        { provide: SchemaService, useValue: schemaServiceMock },
        { provide: BASE_URL, useValue: '/fake-api' },
        VitamUISnackBarService,
        {
          provide: ActivatedRoute,
          useValue: {
            queryParamMap: of(),
          },
        },
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SimpleCriteriaSearchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('component should create', () => {
    expect(component).toBeTruthy();
  });

  it('should not call addSimpleSearchCriteriaSubject when keyElt is null', () => {
    // Given
    const criteria: Partial<SearchCriteriaAddAction> = {
      keyElt: null,
    };

    spyOn(archiveExchangeDataServiceMock, 'addSimpleSearchCriteriaSubject').and.callThrough();

    // When
    component.addCriteria(criteria as SearchCriteriaAddAction);

    // Then
    expect(archiveExchangeDataServiceMock.addSimpleSearchCriteriaSubject).not.toHaveBeenCalled();
  });

  it('should return false', () => {
    expect(component.isValueTranslated('test')).toBeFalsy();
  });
  it('should return true', () => {
    // Given
    const FINAL_ACTION_TYPE = 'FINAL_ACTION_TYPE';
    const ALL_ARCHIVE_UNIT_TYPES = 'ALL_ARCHIVE_UNIT_TYPES';

    // When
    const firstResult = component.isValueTranslated(FINAL_ACTION_TYPE);
    const secondResult = component.isValueTranslated(ALL_ARCHIVE_UNIT_TYPES);

    // Then
    expect(firstResult).toBeTruthy();
    expect(secondResult).toBeTruthy();
  });

  it('should call addSimpleSearchCriteriaSubject when keyElt and CriteriaValue are not null', () => {
    // Given
    const criteria: Partial<SearchCriteriaAddAction> = {
      keyElt: 'keyElt',
      valueElt: {
        id: '',
        value: '',
      },
    };

    spyOn(archiveExchangeDataServiceMock, 'addSimpleSearchCriteriaSubject').and.callThrough();

    // When
    component.addCriteria(criteria as SearchCriteriaAddAction);

    // Then
    expect(archiveExchangeDataServiceMock.addSimpleSearchCriteriaSubject).toHaveBeenCalled();
  });

  describe('DOM', () => {
    it('should have 4 vitamui editables inputs and no formFieldValueWrapper when exact search on Title is disabled', () => {
      // Given
      schema.next([
        {
          item: {
            Path: 'Title',
            ApiPath: 'Title',
          } as SchemaElement,
          children: [],
        },
      ]);
      fixture.detectChanges();

      // When
      const nativeElement = fixture.nativeElement;
      const editableInputs = nativeElement.querySelectorAll('vitamui-common-editable-input');
      const formFieldValueWrapper = nativeElement.querySelectorAll('vitamui-form-field-value-wrapper');

      // Then
      expect(editableInputs.length).toBe(4);
      expect(formFieldValueWrapper.length).toBe(0);
    });
    it('should have 3 vitamui editables inputs and 1 formFieldValueWrapper when exact search on Title is enabled', () => {
      // Given
      schema.next([
        {
          item: {
            Path: 'Title',
            ApiPath: 'Title',
            CustomSearchTypes: ['Strict'],
          } as SchemaElement,
          children: [],
        },
      ]);
      fixture.detectChanges();

      // When
      const nativeElement = fixture.nativeElement;
      const editableInputs = nativeElement.querySelectorAll('vitamui-common-editable-input');
      const formFieldValueWrapper = nativeElement.querySelectorAll('vitamui-form-field-value-wrapper');

      // Then
      expect(editableInputs.length).toBe(3);
      expect(formFieldValueWrapper.length).toBe(1);
    });
  });
});
