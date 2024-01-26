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
import { SimpleChange, SimpleChanges } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TranslateModule } from '@ngx-translate/core';
import { of } from 'rxjs';
import { DescriptionLevel, FileInfoDto, ObjectQualifierType, Unit, UnitType, VersionWithQualifierDto } from 'ui-frontend-common';
import { GetorixDepositService } from '../../../getorix-deposit.service';
import { ArchiveUnitInformationsTabComponent } from './archive-unit-informations-tab.component';

describe('ArchiveUnitInformationsTabComponent', () => {
  let component: ArchiveUnitInformationsTabComponent;
  let fixture: ComponentFixture<ArchiveUnitInformationsTabComponent>;

  const getorixDepositMockService = {
    getGetorixDepositById: () => of({}),
    getObjectGroupDetailsById: () => of({}),
    getUnitFullPath: () => of({}),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ArchiveUnitInformationsTabComponent],
      imports: [HttpClientTestingModule, TranslateModule.forRoot()],
      providers: [{ provide: GetorixDepositService, useValue: getorixDepositMockService }],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ArchiveUnitInformationsTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('component should work normaly after archiveUnit change', () => {
    let archiveUnitSimplesChange: SimpleChange = {
      previousValue: {},
      currentValue: {},
      firstChange: false,
      isFirstChange: () => false,
    };
    let SimpleChanges: SimpleChanges = {
      archiveUnit: archiveUnitSimplesChange,
    };

    component.archiveUnit = {
      '#allunitups': [],
      '#id': 'archiveUnitTestID',
      '#object': 'objectId',
      '#unitType': UnitType.INGEST,
      '#unitups': [],
      '#opi': '',
      '#tenant': 1,
      DescriptionLevel: DescriptionLevel.ITEM,
      Title_: { fr: 'Teste', en: 'Test' },
      Description_: { fr: 'DescriptionFr', en: 'DescriptionEn' },
    };

    component.ngOnChanges(SimpleChanges);
    expect(component).toBeTruthy();
  });

  it('should return true', () => {
    // Given
    component.archiveUnit = {
      '#allunitups': [],
      '#id': 'archiveUnitTestID',
      '#object': 'objectId',
      '#unitType': UnitType.INGEST,
      '#unitups': [],
      '#opi': '',
      '#tenant': 1,
      DescriptionLevel: DescriptionLevel.ITEM,
      Title_: { fr: 'Teste', en: 'Test' },
      Description_: { fr: 'DescriptionFr', en: 'DescriptionEn' },
    };

    // When
    const response = component.unitHasObject();

    // Then
    expect(response).toBeTruthy();
  });

  it('should call getUnitFullPath of GetorixDepositService when the unitId is defined', () => {
    // Given
    component.archiveUnitId = 'archiveUnitId';
    spyOn(getorixDepositMockService, 'getUnitFullPath').and.callThrough();
    // When
    component.getUnitFullPath();

    // Then
    expect(getorixDepositMockService.getUnitFullPath).toHaveBeenCalled();
    expect(getorixDepositMockService.getUnitFullPath).toHaveBeenCalledWith('archiveUnitId');
  });

  it('should call getObjectGroupDetailsById of GetorixDepositService', () => {
    // Given
    const unit = { '#id': 'unitId', '#object': 'objectId' } as Unit;
    spyOn(getorixDepositMockService, 'getObjectGroupDetailsById').and.callThrough();
    // When
    component.getObjectGroupDetailsById(unit);
    // Then
    expect(getorixDepositMockService.getObjectGroupDetailsById).toHaveBeenCalled();
    expect(getorixDepositMockService.getObjectGroupDetailsById).toHaveBeenCalledWith(unit['#object']);
  });

  it('should return PDF as Object Type', () => {
    // Given
    const fileInfo: FileInfoDto = {
      Filename: 'Filename.pdf',
      CreatingApplicationName: 'CreatingApplicationName',
      CreatingApplicationVersion: 'CreatingApplicationVersion',
      CreatingOs: 'CreatingOs',
      CreatingOsVersion: 'CreatingOsVersion',
      LastModified: 'LastModified',
      DateCreatedByApplication: 'DateCreatedByApplication',
    };

    const versionWithQualifier = {
      opened: false,
      version: 3,
      qualifier: ObjectQualifierType.BINARYMASTER,
      FileInfo: fileInfo,
    } as VersionWithQualifierDto;

    component.versionsWithQualifiersOrdered.push(versionWithQualifier);

    const response = component.getObjectType();

    expect(response).not.toBeNull();
    expect(response).toEqual('PDF');
  });

  it('component should work normaly after archiveUnitId change', () => {
    let archiveUnitIdSimplesChange: SimpleChange = {
      previousValue: 'archiveUnitIdFirst',
      currentValue: 'archiveUnitIdIdNew',
      firstChange: false,
      isFirstChange: () => false,
    };
    let SimpleChanges: SimpleChanges = {
      archiveUnitId: archiveUnitIdSimplesChange,
    };

    component.ngOnChanges(SimpleChanges);
    expect(component).toBeTruthy();
  });

  it('should return false', () => {
    // Given
    component.archiveUnit = {
      '#allunitups': [],
      '#id': 'archiveUnitTestID',
      '#object': 'objectId',
      '#unitType': UnitType.INGEST,
      '#unitups': [],
      '#opi': '',
      '#tenant': 1,
      DescriptionLevel: DescriptionLevel.RECORD_GRP,
      Title_: { fr: 'Teste', en: 'Test' },
      Description_: { fr: 'DescriptionFr', en: 'DescriptionEn' },
    };

    // When
    const response = component.unitHasObject();

    // Then
    expect(response).toBeFalsy();
  });
});
