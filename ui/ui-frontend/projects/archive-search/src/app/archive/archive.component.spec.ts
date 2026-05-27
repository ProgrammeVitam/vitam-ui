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
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialog } from '@angular/material/dialog';
import { MatMenuModule } from '@angular/material/menu';
import { MatSidenavModule } from '@angular/material/sidenav';
import { BrowserAnimationsModule, NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateModule } from '@ngx-translate/core';
import { of } from 'rxjs';
import { AccessContract, Status } from '../../../../vitamui-library/src/lib/models/access-contract.interface';
import { BASE_URL, WINDOW_LOCATION } from '../../../../vitamui-library/src/app/modules/injection-tokens';
import { InjectorModule } from '../../../../vitamui-library/src/app/modules/helper/injector.module';
import { LoggerModule } from '../../../../vitamui-library/src/app/modules/logger/logger.module';
import { SearchBarComponent } from '../../../../vitamui-library/src/app/modules/components/search-bar/search-bar.component';
import { SecurityService } from '../../../../vitamui-library/src/app/modules/security/security.service';
import { VitamUICommonTestModule } from 'vitamui-library/testing';
import { environment } from '../../environments/environment';
import { ArchiveApiService } from '../core/api/archive-api.service';
import { ArchiveComponent } from './archive.component';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

describe('ArchiveComponent', () => {
  let component: ArchiveComponent;
  let fixture: ComponentFixture<ArchiveComponent>;

  const accessContract: AccessContract = {
    id: 'accessContractId',
    tenant: 1,
    version: 12,
    name: 'string',
    identifier: 'string',
    description: 'string',
    status: 'string',
    writingPermission: true,
    writingRestrictedDesc: false,
    everyOriginatingAgency: true,
    everyDataObjectVersion: true,
    creationDate: 'string',
    lastUpdate: 'string',
    activationDate: 'string',
    rootUnits: [],
    accessLog: Status.ACTIVE,
    ruleFilter: true,
    ruleCategoryToFilter: ['rule'],
    ruleCategoryToFilterForTheOtherOriginatingAgencies: [],
    doNotFilterFilingSchemes: false,
    excludedRootUnits: [],
    deactivationDate: 'date',
    dataObjectVersion: [],
    originatingAgencies: [],
  };

  const archiveServiceMock = {
    archive: () => of('test archive'),
    search: () => of([]),
    getAccessContractById: () => of(accessContract),
  };
  const securityServiceMock = {
    hasRole$: () => of(true),
  };

  beforeEach(async () => {
    const matDialogSpy = {
      open: vi.fn().mockName('MatDialog.open'),
    };
    matDialogSpy.open.mockReturnValue({ afterClosed: () => of(true) });
    await TestBed.configureTestingModule({
      declarations: [ArchiveComponent],
      schemas: [NO_ERRORS_SCHEMA],
      imports: [
        MatDatepickerModule,
        MatMenuModule,
        MatSidenavModule,
        InjectorModule,
        RouterTestingModule,
        VitamUICommonTestModule,
        BrowserAnimationsModule,
        LoggerModule.forRoot(),
        RouterTestingModule,
        NoopAnimationsModule,
        SearchBarComponent,
        TranslateModule.forRoot(),
      ],
      providers: [
        FormBuilder,
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: ArchiveApiService, useValue: archiveServiceMock },
        { provide: SecurityService, useValue: securityServiceMock },
        { provide: WINDOW_LOCATION, useValue: window.location },
        {
          provide: ActivatedRoute,
          useValue: { params: of({ tenantIdentifier: 1 }), data: of({ appId: 'ARCHIVE_SEARCH_MANAGEMENT_APP' }) },
        },
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: environment, useValue: environment },
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ArchiveComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('component should be created', () => {
    expect(component).toBeTruthy();
  });

  it('isLPExtended should be falsy', () => {
    component.backToNormalLateralPanel();
    expect(component.isLPExtended).toBeDefined();
    expect(component.isLPExtended).toBeFalsy();
  });

  it('isLPExtended should be truthy', () => {
    component.showExtendedLateralPanel();
    expect(component.isLPExtended).toBeDefined();
    expect(component.isLPExtended).toBeTruthy();
  });
});
