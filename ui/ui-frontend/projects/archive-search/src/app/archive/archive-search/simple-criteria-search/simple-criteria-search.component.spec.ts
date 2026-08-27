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
import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
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
  SnackBarService,
} from 'vitamui-library';
import { ArchiveSharedDataService } from '../../../core/archive-shared-data.service';
import { ManagementRulesSharedDataService } from '../../../core/management-rules-shared-data.service';
import { SimpleCriteriaSearchComponent } from './simple-criteria-search.component';
import { ArchiveService } from '../../archive.service';
import { ActivatedRoute } from '@angular/router';

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
    const matDialogSpy = {
      open: vi.fn().mockName('MatDialog.open'),
    };
    matDialogSpy.open.mockReturnValue({ afterClosed: () => of(true) });

    schema = new BehaviorSubject<ItemNode<SchemaElement>[]>([]);
    const schemaServiceMock = {
      getDescriptiveSchemaTree: () => schema,
      getSchema: () => of(),
    };

    await TestBed.configureTestingModule({
      imports: [InjectorModule, AgenciesModule, LoggerModule.forRoot(), SimpleCriteriaSearchComponent],
      providers: [
        FormBuilder,
        { provide: ArchiveService, useValue: archiveServiceStub },
        { provide: ArchiveSharedDataService, useValue: archiveExchangeDataServiceMock },
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: ManagementRulesSharedDataService, useValue: managementRulesSharedDataServiceMock },
        { provide: SchemaService, useValue: schemaServiceMock },
        SnackBarService,
        {
          provide: ActivatedRoute,
          useValue: {
            queryParamMap: of(),
          },
        },
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

    vi.spyOn(archiveExchangeDataServiceMock, 'addSimpleSearchCriteriaSubject');

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

    vi.spyOn(archiveExchangeDataServiceMock, 'addSimpleSearchCriteriaSubject');

    // When
    component.addCriteria(criteria as SearchCriteriaAddAction);

    // Then
    expect(archiveExchangeDataServiceMock.addSimpleSearchCriteriaSubject).toHaveBeenCalled();
  });

  it('should have 4 vitamui editables inputs and no formFieldValueWrapper when exact search on Title is disabled', fakeAsync(() => {
    // Given
    setTimeout(() =>
      schema.next([
        {
          item: {
            Path: 'Title',
            ApiPath: 'Title',
          } as SchemaElement,
          children: [],
        },
      ]),
    );
    tick(0);
    fixture.detectChanges();

    // When
    const nativeElement = fixture.nativeElement;
    const editableInputs = nativeElement.querySelectorAll('vitamui-common-editable-input');
    const formFieldValueWrapper = nativeElement.querySelectorAll('vitamui-form-field-value-wrapper');

    // Then
    expect(editableInputs.length).toBe(4);
    expect(formFieldValueWrapper.length).toBe(0);
  }));
  it('should have 3 vitamui editables inputs and 1 formFieldValueWrapper when exact search on Title is enabled', fakeAsync(() => {
    // Given
    setTimeout(() =>
      schema.next([
        {
          item: {
            Path: 'Title',
            ApiPath: 'Title',
            CustomSearchTypes: ['Strict'],
          } as SchemaElement,
          children: [],
        },
      ]),
    );
    tick(0);
    fixture.detectChanges();

    // When
    const nativeElement = fixture.nativeElement;
    const editableInputs = nativeElement.querySelectorAll('vitamui-common-editable-input');
    const formFieldValueWrapper = nativeElement.querySelectorAll('vitamui-form-field-value-wrapper');

    // Then
    expect(editableInputs.length).toBe(3);
    expect(formFieldValueWrapper.length).toBe(1);
  }));
});
