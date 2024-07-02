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
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';

import { MatNativeDateModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { BrowserAnimationsModule, NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';

import { Component, NO_ERRORS_SCHEMA } from '@angular/core';
import { MatLegacyDialog as MatDialog, MatLegacyDialogModule as MatDialogModule } from '@angular/material/legacy-dialog';
import { MatLegacyMenuModule as MatMenuModule } from '@angular/material/legacy-menu';
import { MatSidenavModule } from '@angular/material/sidenav';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { InjectorModule, LoggerModule, SearchBarComponent } from 'vitamui-library';
import { VitamUICommonTestModule } from 'vitamui-library/testing';
import { environment } from '../../environments/environment';
import { IngestType } from '../core/common/ingest-type.enum';
import { IngestService } from '../ingest/ingest.service';
import { HoldingFillingSchemeComponent } from './holding-filling-scheme.component';

@Component({
  selector: 'app-ingest-list',
  template: '',
  standalone: true,
  imports: [
    MatDatepickerModule,
    MatNativeDateModule,
    MatMenuModule,
    MatSidenavModule,
    InjectorModule,
    VitamUICommonTestModule,
    RouterTestingModule,
    SearchBarComponent,
    MatDialogModule,
  ],
})
class IngestListStubComponent {}

describe('HoldingFilingSchemeComponent', () => {
  let component: HoldingFillingSchemeComponent;
  let fixture: ComponentFixture<HoldingFillingSchemeComponent>;

  const ingestServiceMock = {
    ingest: () => of('test ingest'),
    search: () => of([]),
  };

  beforeEach(async () => {
    const matDialogSpy = jasmine.createSpyObj('MatDialog', ['open']);
    matDialogSpy.open.and.returnValue({ afterClosed: () => of(true) });
    await TestBed.configureTestingModule({
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
        NoopAnimationsModule,
        SearchBarComponent,
        MatDialogModule,
        HoldingFillingSchemeComponent,
        IngestListStubComponent,
      ],
      providers: [
        FormBuilder,
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: IngestService, useValue: ingestServiceMock },
        {
          provide: ActivatedRoute,
          useValue: { params: of({ tenantIdentifier: 1 }), data: of({ appId: 'HOLDING_FILLING_SCHEME_APP' }) },
        },
        { provide: environment, useValue: environment },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(HoldingFillingSchemeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call open', () => {
    const matDialogSpy = TestBed.inject(MatDialog);
    component.openImportTreePlanPopup(IngestType.HOLDING_SCHEME);
    expect(matDialogSpy.open).toHaveBeenCalled();
    expect(matDialogSpy.open).toHaveBeenCalledTimes(1);
  });

  it('should open a modal with HoldingFillingSchemeComponent', () => {
    const matDialogSpy = TestBed.inject(MatDialog);
    component.openImportTreePlanPopup(IngestType.FILING_SCHEME);
    expect(matDialogSpy.open).toHaveBeenCalledTimes(1);
  });
});
