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
 */

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';
import { MatSidenavModule } from '@angular/material/sidenav';
import { BrowserAnimationsModule, NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateModule } from '@ngx-translate/core';
import { of } from 'rxjs';
import { ApplicationService, InjectorModule, LoggerModule, WINDOW_LOCATION } from 'vitamui-library';
import { VitamUICommonTestModule } from 'vitamui-library/testing';
import { ManagementContractComponent } from './management-contract.component';

describe('ManagementContractComponent', () => {
  let component: ManagementContractComponent;
  let fixture: ComponentFixture<ManagementContractComponent>;

  const applicationServiceMock = {
    getActiveTenantAppsMap: () => of([]),
    isApplicationExternalIdentifierEnabled: () => of(true),
    getTenantAppMap: () => of([]),
    openApplication: () => of(),
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
      declarations: [ManagementContractComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: { params: of({ tenantIdentifier: 1 }), data: of({ appId: 'MANAGEMENT_CONTRACT_APP' }) },
        },
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: WINDOW_LOCATION, useValue: window.location },
        { provide: ApplicationService, useValue: applicationServiceMock },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ManagementContractComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('component should be created', () => {
    expect(component).toBeTruthy();
  });

  it('should return Identifier', () => {
    const searchText = 'Identifier';
    component.onSearchSubmit(searchText);
    expect(component.search).not.toBeNull();
    expect(component.search).toEqual(searchText);
  });

  it('should return the initiale value', () => {
    component.onSearchSubmit(null);
    expect(component.search).not.toBeNull();
    expect(component.search).toEqual('');
  });

  it('should call isApplicationExternalIdentifierEnabled of ApplicationService', () => {
    // Given
    spyOn(applicationServiceMock, 'isApplicationExternalIdentifierEnabled').and.callThrough();

    // When
    component.updateSlaveMode();

    // Then
    expect(applicationServiceMock.isApplicationExternalIdentifierEnabled).toHaveBeenCalled();
  });
});
