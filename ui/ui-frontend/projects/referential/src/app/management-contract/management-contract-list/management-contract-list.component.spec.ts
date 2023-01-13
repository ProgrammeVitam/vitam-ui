/**
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
 */

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatSidenavModule } from '@angular/material/sidenav';
import { BrowserAnimationsModule, NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateModule } from '@ngx-translate/core';
import { of, Subject } from 'rxjs';
import { BASE_URL, InjectorModule, LoggerModule, ManagementContract, SearchService, WINDOW_LOCATION } from 'ui-frontend-common';
import { VitamUICommonTestModule } from 'ui-frontend-common/testing';
import { ManagementContractService } from '../management-contract.service';
import { ManagementContractListComponent } from './management-contract-list.component';

describe('ManagementContractListComponent', () => {
  let component: ManagementContractListComponent;
  let fixture: ComponentFixture<ManagementContractListComponent>;
  const updated = new Subject<ManagementContract>();

  const managementContractServiceMock = {
    updated,
    get: () => of({}),
    getAll: () => of([]),
    getAllForTenant: () => of([]),
    search: () => of([]),
    exists: () => of(true),
    existsProperties: () => of(true),
    patch: () => of({}),
    create: () => of({}),
  };

  const searchServiceeMock = {
    search: () => of([]),
  };

  beforeEach(async () => {
    const matDialogSpy = jasmine.createSpyObj('MatDialog', ['open']);
    matDialogSpy.open.and.returnValue({ afterClosed: () => of(true) });

    await TestBed.configureTestingModule({
      imports: [
        ReactiveFormsModule,
        MatSidenavModule,
        InjectorModule,
        VitamUICommonTestModule,
        HttpClientTestingModule,
        TranslateModule.forRoot(),
        RouterTestingModule,
        LoggerModule.forRoot(),
        BrowserAnimationsModule,
        NoopAnimationsModule,
      ],
      declarations: [ManagementContractListComponent],
      providers: [
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: WINDOW_LOCATION, useValue: window.location },
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: ManagementContractService, useValue: managementContractServiceMock },
        { provide: SearchService, useValue: searchServiceeMock },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ManagementContractListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('component should be created', () => {
    expect(component).toBeTruthy();
  });

  it('should return the criteria search', () => {
    // Given
    component._searchText = 'IdentifierCriteria';
    const expectedResppnse = { Name: 'IdentifierCriteria', Identifier: 'IdentifierCriteria', Status: ['ACTIVE', 'INACTIVE'] };

    // When
    const criteria = component.buildManagementContractCriteriaFromSearch();

    // Then
    expect(criteria).toEqual(expectedResppnse);
  });

  it('should have the initiale values', () => {
    // Given
    const expectedResponse = ['ACTIVE', 'INACTIVE'];

    // Then
    expect(component.filterMap.status).toBeDefined();
    expect(component.filterMap.status).not.toBeNull();
    expect(component.filterMap.status.length).toEqual(2);
    expect(component.filterMap.status).toEqual(expectedResponse);
  });

  it('should not call search of SearchService', () => {
    // Given
    spyOn(searchServiceeMock, 'search').and.callThrough();

    // When
    component.pending = true;
    component.searchManagementContractOrdered();

    // Then
    expect(searchServiceeMock.search).not.toHaveBeenCalled();
  });

  it('should return the criteria search when no status filter is given', () => {
    // Given
    component.filterMap.status = [];
    component._searchText = 'IdentifierCriteria';
    const expectedResppnse = { Name: 'IdentifierCriteria', Identifier: 'IdentifierCriteria' };

    // When
    const criteria = component.buildManagementContractCriteriaFromSearch();

    // Then
    expect(criteria).toEqual(expectedResppnse);
  });

  it('filterMap should have values', () => {
    // Then
    expect(component.filterMap.status).toBeDefined();
    expect(component.filterMap.status).not.toBeNull();
    expect(component.filterMap.status.length).toEqual(2);
    expect(component.filterMap.status[0]).toEqual('ACTIVE');
  });

  describe('DOM', () => {
    it('should have 1 button ', () => {
      const nativeElement = fixture.nativeElement;
      const elementBtn = nativeElement.querySelectorAll('button');
      expect(elementBtn.length).toBe(1);
    });

    it('should have 5 vitamui order button', () => {
      const nativeElement = fixture.nativeElement;
      const vitamUiOrderBtn = nativeElement.querySelectorAll('vitamui-common-order-by-button');
      expect(vitamUiOrderBtn.length).toBe(5);
    });
  });
});
