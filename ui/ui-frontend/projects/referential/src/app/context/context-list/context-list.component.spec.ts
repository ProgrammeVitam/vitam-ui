/*
 *
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
 */

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, fakeAsync, TestBed, tick, waitForAsync } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { RouterTestingModule } from '@angular/router/testing';
import { VitamUISnackBar } from 'projects/ingest/src/app/shared/vitamui-snack-bar';
import { EMPTY, of } from 'rxjs';
import { AuthService, BASE_URL, InjectorModule, LoggerModule, WINDOW_LOCATION } from 'vitamui-library';
import { VitamUICommonTestModule } from 'vitamui-library/testing';
import { ContextService } from '../context.service';
import { ContextListComponent } from './context-list.component';

const snackBarSpy = jasmine.createSpyObj('VitamUISnackBar', ['open', 'openFromComponent']);

describe('ContextListComponent', () => {
  let component: ContextListComponent;
  let fixture: ComponentFixture<ContextListComponent>;

  const matDialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['open', 'close']);
  const matDialogSpy = jasmine.createSpyObj('MatDialog', ['open', 'close']);

  const authServiceMock = {
    user: () => of({}),
  };

  const contextServiceMock = {
    search: () => of([]),
    updated: EMPTY,
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ContextListComponent],
      imports: [
        VitamUICommonTestModule,
        MatProgressSpinnerModule,
        HttpClientTestingModule,
        ReactiveFormsModule,
        InjectorModule,
        LoggerModule.forRoot(),
        RouterTestingModule,
      ],
      providers: [
        { provide: BASE_URL, useValue: '' },
        { provide: MatDialogRef, useValue: matDialogRefSpy },
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: ContextService, useValue: contextServiceMock },
        { provide: AuthService, useValue: authServiceMock },
        { provide: VitamUISnackBar, useValue: snackBarSpy },

        { provide: WINDOW_LOCATION, useValue: window.location },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ContextListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('Component should be created', () => {
    expect(component).toBeTruthy();
  });

  it('Search criteria should exists when we have some status as criteria', fakeAsync(() => {
    // Given
    const filterMap: { [key: string]: any[] } = {
      status: ['ACTIVE', 'INACTIVE'],
    };
    const searchText = 'Search text';

    // When
    component.filterMap = filterMap;
    component.searchText = searchText;
    let criteria = component.buildContextCriteriaFromSearch();
    tick(400);

    // Then
    expect(criteria).not.toBeNull();
    expect(criteria.Name).toEqual(searchText);
    expect(criteria.Identifier).toEqual(searchText);
    expect(criteria.Status).toEqual(['ACTIVE', 'INACTIVE']);
  }));
});
