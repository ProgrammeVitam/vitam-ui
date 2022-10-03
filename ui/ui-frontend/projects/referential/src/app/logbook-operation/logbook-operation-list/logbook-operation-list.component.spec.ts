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
import { EMPTY, of } from 'rxjs';

import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { LogbookDownloadService } from '../logbook-download.service';
import { LogbookSearchService } from '../logbook-search.service';
import { EventTypeBadgeClassPipe } from './event-type-badge-class.pipe';
import { EventTypeColorClassPipe } from './event-type-color-class.pipe';
import { LastEventPipe } from './last-event.pipe';
import { LogbookOperationListComponent } from './logbook-operation-list.component';

describe('LogbookOperationListComponent', () => {
  let component: LogbookOperationListComponent;
  let fixture: ComponentFixture<LogbookOperationListComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [LogbookOperationListComponent, LastEventPipe, EventTypeColorClassPipe, EventTypeBadgeClassPipe],
      providers: [
        { provide: LogbookSearchService, useValue: { search: () => EMPTY } },
        { provide: LogbookDownloadService, useValue: { logbookOperationsReloaded: of([{ id: 'event-01' }]) } },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LogbookOperationListComponent);
    component = fixture.componentInstance;
    component.dataSource = [];
    fixture.detectChanges();
  });

  it('Component should be created', () => {
    expect(component).toBeTruthy();
  });

  it('should return ARCHIVE_TRANSFER', () => {
    // Given
    const type = 'ARCHIVE_TRANSFER';
    const operationLabel = 'ARCHIVE_TRANSFER_LABEL';

    // When
    const response: string = component.manageOperationLabel(type);

    // Then
    expect(response).not.toBeNull();
    expect(response).toEqual(operationLabel);
  });

  it('should have details about transfer reply', () => {
    // Given
    component.operationCategoriesFilterOptions = [];

    // When
    component.refreshOperationCategoriesOptions();

    // Then
    expect(component.operationCategoriesFilterOptions).toBeDefined();
    expect(component.operationCategoriesFilterOptions).not.toBeNull();
    expect(component.operationCategoriesFilterOptions.find((category) => category.label === 'Acquittement de transfert')).toBeDefined();
    expect(component.operationCategoriesFilterOptions.find((category) => category.label === 'Acquittement de transfert')).not.toBeNull();
  });

  it('should return the given value', () => {
    // Given
    const type = 'TRANSFER_REPLY';
    const operationLabel = 'TRANSFER_REPLY';

    // When
    const response: string = component.manageOperationLabel(type);

    // Then
    expect(response).not.toBeNull();
    expect(response).toEqual(operationLabel);
  });

  it('should return 15 as array length', () => {
    // Given
    component.operationCategoriesFilterOptions = [];

    // When
    component.refreshOperationCategoriesOptions();

    // Then
    expect(component.operationCategoriesFilterOptions).toBeDefined();
    expect(component.operationCategoriesFilterOptions).not.toBeNull();
    expect(component.operationCategoriesFilterOptions.length).toEqual(15);
  });

  describe('DOM', () => {
    it('should have 1 table and 1 footer', () => {
      const tableFooterHtmlElements = fixture.nativeElement.querySelectorAll('.vitamui-table-footer');
      const vitamUiTableHtmlElements = fixture.nativeElement.querySelectorAll('.vitamui-table');

      expect(tableFooterHtmlElements).toBeTruthy();
      expect(tableFooterHtmlElements.length).toBe(1);

      expect(vitamUiTableHtmlElements).toBeTruthy();
      expect(vitamUiTableHtmlElements.length).toBe(1);
    });

    it('should have 7 different columns', () => {
      const tableHeaderrHtmlElements = fixture.nativeElement.querySelectorAll('.vitamui-table-header');

      expect(tableHeaderrHtmlElements).toBeTruthy();
      expect(tableHeaderrHtmlElements.length).toBe(7);
    });
  });
});
