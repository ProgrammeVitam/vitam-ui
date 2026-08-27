import type { Mock } from 'vitest';
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
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { IngestComponent } from './ingest.component';

import { Component } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatMenuModule } from '@angular/material/menu';
import { MatSidenavModule } from '@angular/material/sidenav';
import { Direction, InjectorModule, LoggerModule, SearchBarComponent } from 'vitamui-library';
import { VitamUICommonTestModule } from 'vitamui-library/testing';
import { environment } from '../../environments/environment';
import { IngestType } from '../core/common/ingest-type.enum';
import { UploadService } from '../core/common/upload.service';
import { IngestService } from './ingest.service';

@Component({
  selector: 'app-ingest-list',
  template: '',
  imports: [MatDatepickerModule, MatMenuModule, MatSidenavModule, VitamUICommonTestModule],
})
export class IngestListStubComponent {
  emitOrderChange() {}
}

describe('IngestComponent test:', () => {
  let component: IngestComponent;
  let fixture: ComponentFixture<IngestComponent>;

  const ingestServiceMock = {
    ingest: () => of('test ingest'),
    search: () => of([]),
  };
  const uploadServiceSpy = {
    uploadFile: vi.fn().mockName('UploadService.uploadFile').mockReturnValue(of({})),
    filesStatus: vi.fn().mockName('UploadService.filesStatus').mockReturnValue(of([])),
  };

  beforeEach(async () => {
    const matDialogSpy = {
      open: vi.fn().mockName('MatDialog.open'),
    };
    matDialogSpy.open.mockReturnValue({ afterClosed: () => of(true) });
    await TestBed.configureTestingModule({
      imports: [
        MatDatepickerModule,
        MatMenuModule,
        MatSidenavModule,
        InjectorModule,
        VitamUICommonTestModule,
        LoggerModule.forRoot(),
        SearchBarComponent,
        IngestComponent,
        IngestListStubComponent,
      ],
      providers: [
        FormBuilder,
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: IngestService, useValue: ingestServiceMock },
        { provide: UploadService, useValue: uploadServiceSpy },
        {
          provide: ActivatedRoute,
          useValue: {
            params: of({ tenantIdentifier: 1 }),
            data: of({ appId: 'INGEST_MANAGEMENT_APP' }),
            snapshot: { data: { appId: 'INGEST_MANAGEMENT_APP' } },
          },
        },
        { provide: environment, useValue: environment },
      ],
    })
      .overrideTemplate(IngestComponent, '<div></div>')
      .compileComponents();
  });

  let ingestListStub: any;

  beforeEach(() => {
    fixture = TestBed.createComponent(IngestComponent);
    component = fixture.componentInstance;
    ingestListStub = TestBed.createComponent(IngestListStubComponent).componentInstance;
    Object.defineProperty(component, 'ingestListComponent', {
      writable: true,
      configurable: true,
      value: () => ingestListStub,
    });
    Object.defineProperty(component, 'searchBar', {
      writable: true,
      configurable: true,
      value: () => ({}),
    });
    fixture.detectChanges();
  });

  it('should be truthy', () => {
    expect(component).toBeTruthy();
  });

  it('should call open', () => {
    const matDialogSpy = TestBed.inject(MatDialog);
    component.openImportSipDialog(IngestType.DEFAULT_WORKFLOW);
    expect(matDialogSpy.open).toHaveBeenCalled();
    expect(vi.mocked(matDialogSpy.open as Mock).mock.calls.length).toBe(1);
  });

  it('should open a modal with IngestComponent', () => {
    const matDialogSpy = TestBed.inject(MatDialog);
    component.openImportSipDialog(IngestType.DEFAULT_WORKFLOW);
    expect(vi.mocked(matDialogSpy.open as Mock).mock.calls.length).toBe(1);
    expect(matDialogSpy.open).toHaveBeenCalled();
  });

  it('should update search term on search submit', () => {
    component.onSearchSubmit('my search');
    expect(component.search).toBe('my search');
  });

  it('should update filters when start date and end date change', () => {
    const startDate = new Date('2026-01-01');
    const endDate = new Date('2026-01-10');
    component.dateRangeFilterForm.controls.startDate.setValue(startDate);
    expect(component.filters.startDate).toEqual(startDate);

    component.dateRangeFilterForm.controls.endDate.setValue(endDate);
    expect(component.filters.endDate).toEqual(endDate);
    expect(ingestListStub.direction).toBe(Direction.DESCENDANT);
  });

  it('should refresh ingest list', () => {
    const emitOrderChangeSpy = vi.spyOn(ingestListStub, 'emitOrderChange');
    component.refresh();
    expect(ingestListStub.direction).toBe(Direction.DESCENDANT);
    expect(emitOrderChangeSpy).toHaveBeenCalled();
  });
});
