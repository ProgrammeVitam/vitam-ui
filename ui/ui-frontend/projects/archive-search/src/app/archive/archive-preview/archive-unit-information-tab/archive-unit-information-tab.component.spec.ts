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
 *
 *
 */

import { NO_ERRORS_SCHEMA, Pipe, PipeTransform } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatLegacyMenuModule as MatMenuModule } from '@angular/material/legacy-menu';
import { MatLegacyProgressSpinnerModule as MatProgressSpinnerModule } from '@angular/material/legacy-progress-spinner';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatTreeModule } from '@angular/material/tree';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateModule } from '@ngx-translate/core';
import { environment } from 'projects/archive-search/src/environments/environment';
import { of } from 'rxjs';
import {
  BASE_URL,
  DescriptionLevel,
  ENVIRONMENT,
  InjectorModule,
  LoggerModule,
  StartupService,
  Unit,
  UnitType,
  WINDOW_LOCATION,
} from 'vitamui-library';
import { ArchiveService } from '../../archive.service';
import { ArchiveUnitInformationTabComponent } from './archive-unit-information-tab.component';

@Pipe({
  name: 'dateTime',
  standalone: true,
})
export class MockDateTimePipe implements PipeTransform {
  transform(value: string = ''): any {
    return value;
  }
}

@Pipe({
  name: 'truncate',
  standalone: true,
})
class MockTruncatePipe implements PipeTransform {
  transform(value: number): number {
    return value;
  }
}

@Pipe({
  name: 'unitI18n',
  standalone: true,
})
class MockUnitI18nPipe implements PipeTransform {
  transform(value: number): number {
    return value;
  }
}

describe('ArchiveUnitInformationTabComponent', () => {
  let component: ArchiveUnitInformationTabComponent;
  let fixture: ComponentFixture<ArchiveUnitInformationTabComponent>;

  const matDialogSpy = jasmine.createSpyObj('MatDialog', ['open']);

  const activatedRouteMock = {
    params: of({ tenantIdentifier: 1 }),
    data: of({ appId: 'ARCHIVE_SEARCH_MANAGEMENT_APP' }),
  };

  const archiveServiceMock = {
    getBaseUrl: () => '/fake-api',
    buildArchiveUnitPath: () => of({ resumePath: '', fullPath: '' }),
    receiveDownloadProgressSubject: () => of(true),
    updateUnit: () => of({}),
    openSnackBarForWorkflow: () => of({}),
    downloadObjectFromUnit: () => of({}),
  };

  const startUpServiceMock = {
    getPortalUrl: () => '',
    setTenantIdentifier: () => {},
    getLogoutUrl: () => '',
    getCasUrl: () => '',
    getSearchUrl: () => '',
    getArchivesSearchUrl: () => '',
    getReferentialUrl: () => '',
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        MatMenuModule,
        MatTreeModule,
        MatProgressSpinnerModule,
        MatSidenavModule,
        InjectorModule,
        LoggerModule.forRoot(),
        RouterTestingModule,
        MatIconModule,
        BrowserAnimationsModule,
        TranslateModule.forRoot(),
        ArchiveUnitInformationTabComponent,
        MockTruncatePipe,
        MockDateTimePipe,
        MockUnitI18nPipe,
      ],
      providers: [
        FormBuilder,
        { provide: ArchiveService, useValue: archiveServiceMock },
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: ActivatedRoute, useValue: activatedRouteMock },
        { provide: ENVIRONMENT, useValue: environment },
        { provide: WINDOW_LOCATION, useValue: window.location },
        { provide: StartupService, useValue: startUpServiceMock },
        { provide: MatDialog, useValue: matDialogSpy },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ArchiveUnitInformationTabComponent);
    component = fixture.componentInstance;
    const archiveUnit: Unit = {
      Title_: { fr: 'Teste', en: 'Test' },
      Description_: { fr: 'DescriptionFr', en: 'DescriptionEn' },
      DescriptionLevel: DescriptionLevel.RECORD_GRP,
      Title: 'Gambetta par producteur1',
      Description: 'Station Gambetta ligne 3 Paris',
      '#id': 'aeaqaaaaaehkfhaythjgjhfg545szniaaacq',
      '#tenant': 1,
      '#unitups': [],
      '#min': 1,
      '#max': 1,
      '#allunitups': [],
      '#unitType': UnitType.INGEST,
      '#operations': ['aeeaaaaaaghpgxejaaxyyamdrz6r6daaaaaq'],
      '#opi': 'aeeaaaaaaghpgxejaaxyyamdrz6r6daaaaaq',
      '#originating_agency': 'producteur1',
      '#originating_agencies': ['producteur1'],
      '#storage': {
        strategyId: 'default',
      },
      '#sedaVersion': '2.1',
      '#approximate_creation_date': '2022-09-30T13:01:56.381',
      '#approximate_update_date': '2022-09-30T13:01:56.381',
    };
    component.archiveUnit = archiveUnit;
    fixture.detectChanges();
  });

  it('Component should be created', () => {
    expect(component).toBeTruthy();
  });

  it('fullPath should be true ', () => {
    // When
    component.showArchiveUniteFullPath();

    // Then
    expect(component.fullPath).toBeTruthy();
  });

  it('should call downloadObjectFromUnit of archiveService ', () => {
    // Given
    const unit: Unit = {
      '#allunitups': [],
      '#id': 'id',
      '#object': '',
      '#unitType': UnitType.INGEST,
      '#unitups': [],
      '#opi': '',
      Title: 'test tets',
      Description_: { fr: 'DescriptionFr', en: 'DescriptionEn' },
    };
    spyOn(archiveServiceMock, 'downloadObjectFromUnit').and.callThrough();

    // When
    component.onDownloadObjectFromUnit(unit);

    // Then
    expect(archiveServiceMock.downloadObjectFromUnit).toHaveBeenCalled();
  });

  describe('DOM', () => {
    it('should have one button ', () => {
      // When
      const nativeElement = fixture.nativeElement;
      const elementBtn = nativeElement.querySelectorAll('button[type=button]');

      // Then
      expect(elementBtn.length).toBe(1);
    });
    it('should have 8 rows ', () => {
      // When
      const nativeElement = fixture.nativeElement;
      const elementRow = nativeElement.querySelectorAll('.row');

      // Then
      expect(elementRow.length).toBe(8);
    });
    it('should have 5 columns ', () => {
      // When
      const nativeElement = fixture.nativeElement;
      const elementColumn = nativeElement.querySelectorAll('.col-12');

      // Then
      expect(elementColumn.length).toBe(5);
    });
  });
});
