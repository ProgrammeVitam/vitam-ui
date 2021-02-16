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
import { waitForAsync, ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { MatNativeDateModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';

import { IngestComponent } from './ingest.component';
import { InjectorModule, LoggerModule, SearchBarModule } from 'ui-frontend-common';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { environment } from '../../environments/environment';
import { VitamUICommonTestModule } from 'ui-frontend-common/testing';
import { IngestService } from './ingest.service';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { Component, NO_ERRORS_SCHEMA } from '@angular/core';
import { MatMenuModule } from '@angular/material/menu';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatDialog } from '@angular/material/dialog';
import { IngestListComponent } from './ingest-list/ingest-list.component';
import { UploadService } from '../core/common/upload.service';

@Component({ selector: 'app-ingest-list', template: '' })
export class IngestListStubComponent {
      emitOrderChange() {}
}

describe('IngestComponent', () => {
  let component: IngestComponent;
  let fixture: ComponentFixture<IngestComponent>;

  const ingestServiceMock = {
    ingest: () => of('test ingest'),
    search: () => of([])
  };
  const uploadServiceSpy = jasmine.createSpyObj('UploadService', { uploadFile: of({}), filesStatus: of([]) });

  beforeEach(waitForAsync(() => {
    const matDialogSpy = jasmine.createSpyObj('MatDialog', ['open']);
    matDialogSpy.open.and.returnValue({ afterClosed: () => of(true) });
    TestBed.configureTestingModule({
      imports: [
        MatDatepickerModule,
        MatNativeDateModule,
        MatMenuModule,
        MatSidenavModule,
        InjectorModule,
        RouterTestingModule,
        VitamUICommonTestModule,
        BrowserAnimationsModule,
        LoggerModule.forRoot(),
        RouterTestingModule,
        NoopAnimationsModule,
        SearchBarModule
      ],
      declarations: [
        IngestComponent,
        IngestListStubComponent
      ],
      providers: [
        FormBuilder,
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: IngestService, useValue: ingestServiceMock },
        { provide: UploadService, useValue: uploadServiceSpy },
        { provide: ActivatedRoute, useValue: { params: of({ tenantIdentifier: 1 }), data: of({ appId: 'INGEST_MANAGEMENT_APP' }) } },
        { provide: environment, useValue: environment }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(IngestComponent);
    component = fixture.componentInstance;
    component.ingestListComponent = TestBed.createComponent(IngestListStubComponent).componentInstance as IngestListComponent;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call open', () => {
    const matDialogSpy = TestBed.get(MatDialog);
    component.openImportSipDialog('DEFULT_WORKFLOW');
    expect(matDialogSpy.open).toHaveBeenCalled();
    expect(matDialogSpy.open.calls.count()).toBe(1);
  });

  it('should open a modal with IngestComponent', () => {
    const matDialogSpy = TestBed.get(MatDialog);
    component.openImportSipDialog('DEFULT_WORKFLOW');
    expect(matDialogSpy.open.calls.count()).toBe(1);
    expect(matDialogSpy.open).toHaveBeenCalled();
  });
});
