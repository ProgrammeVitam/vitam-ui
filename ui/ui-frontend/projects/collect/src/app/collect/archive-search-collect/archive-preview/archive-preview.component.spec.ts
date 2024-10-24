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
import { NO_ERRORS_SCHEMA, Pipe, PipeTransform } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatIconModule } from '@angular/material/icon';
import { MatLegacyMenuModule as MatMenuModule } from '@angular/material/legacy-menu';
import { MatLegacyProgressSpinnerModule as MatProgressSpinnerModule } from '@angular/material/legacy-progress-spinner';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatLegacyTabChangeEvent as MatTabChangeEvent } from '@angular/material/legacy-tabs';
import { MatTreeModule } from '@angular/material/tree';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateModule } from '@ngx-translate/core';
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
import { environment } from '../../../../environments/environment';
import { ArchiveCollectService } from '../archive-collect.service';
import { ArchivePreviewComponent } from './archive-preview.component';

describe('ArchivePreviewComponent', () => {
  let component: ArchivePreviewComponent;
  let fixture: ComponentFixture<ArchivePreviewComponent>;

  @Pipe({ name: 'truncate' })
  class MockTruncatePipe implements PipeTransform {
    transform(value: number): number {
      return value;
    }
  }

  @Pipe({ name: 'unitI18n' })
  class MockUnitI18nPipe implements PipeTransform {
    transform(value: number): number {
      return value;
    }
  }

  beforeEach(async () => {
    const archiveServiceMock = {
      getBaseUrl: () => '/fake-api',
      buildArchiveUnitPath: () => of({ resumePath: '', fullPath: '' }),
      receiveDownloadProgressSubject: () => of(true),
    };

    await TestBed.configureTestingModule({
      imports: [
        BrowserAnimationsModule,
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
      ],
      declarations: [ArchivePreviewComponent, MockTruncatePipe, MockUnitI18nPipe],
      providers: [
        { provide: ArchiveCollectService, useValue: archiveServiceMock },
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: ENVIRONMENT, useValue: environment },
        { provide: WINDOW_LOCATION, useValue: window.location },
        { provide: StartupService, useValue: { getPortalUrl: () => '', setTenantIdentifier: () => {} } },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ArchivePreviewComponent);
    component = fixture.componentInstance;
    component.archiveUnit = {
      '#allunitups': [],
      '#id': 'id',
      '#object': '',
      '#unitType': null,
      '#unitups': [],
      '#opi': '',
      Title_: { fr: 'Teste', en: 'Test' },
      Description_: { fr: 'DescriptionFr', en: 'DescriptionEn' },
    };
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should the selectedIndex to be 0 after closing extended panel ', () => {
    component.emitClose();
    expect(component.selectedIndex).toEqual(0);
  });

  it('should show the extended panel ', () => {
    // Given
    const matTabEvent = { index: 1 } as MatTabChangeEvent;

    // When
    component.selectedTabChangeEvent(matTabEvent);

    // Then
    expect(component.isPanelextended).toBeTruthy();
  });

  it('should return vitamui-icon-folder" as response ', () => {
    const archiveUnit: Unit = {
      '#id': 'aeaqaaaaaehlvxukaazfaame7fyo5myaaaba',
      Title: 'Porte de Bagnolet par producteur1',
      DescriptionLevel: DescriptionLevel.RECORD_GRP,
      Description: 'Station Porte de Bagnolet ligne 3 Paris',
      '#tenant': 1,
      '#unitups': ['aeaqaaaaaehlvxukaazfaame7fyo5myaaaca'],
      '#min': 1,
      '#max': 2,
      '#allunitups': ['aeaqaaaaaehlvxukaazfaame7fyo5myaaaca'],
      '#unitType': UnitType.INGEST,
      '#operations': ['aeeaaaaaaghnanqdabliwame7fyokjqaaaaq'],
      '#opi': 'aeeaaaaaaghnanqdabliwame7fyokjqaaaaq',
      '#originating_agency': 'producteur1',
      '#originating_agencies': ['producteur1'],
      StartDate: new Date('2016-06-03T15:28:00'),
      EndDate: new Date('2016-06-03T15:28:00'),
      Xtag: [],
      Vtag: [],
      '#storage': {
        strategyId: 'default',
      },
      '#qualifiers': [],
      OriginatingSystemId: ['OriginatingSystemId_00'],
      PhysicalAgency: [],
      PhysicalStatus: [],
      PhysicalType: [],
      Keyword: [],
      '#approximate_creation_date': '2022-12-10T00:30:42.568',
      '#approximate_update_date': '2022-12-10T00:30:42.568',
      originating_agencyName: 'Service producteur1',
    };

    const response = component.getArchiveUnitIcon(archiveUnit);

    expect(response).toEqual('vitamui-icon-folder');
  });

  it('should return the exact values ', () => {
    component.updateMetadataDesc();
    expect(component.isPanelextended).toBeTruthy();
    expect(component.updateStarted).toBeTruthy();
  });

  it('should show the extended panel ', () => {
    // Given
    const matTabEvent = { index: 2 } as MatTabChangeEvent;

    // When
    component.selectedTabChangeEvent(matTabEvent);

    // Then
    expect(component.isPanelextended).toBeTruthy();
  });
});
