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

import createSpyObj = jasmine.createSpyObj;
import { Clipboard } from '@angular/cdk/clipboard';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateModule } from '@ngx-translate/core';
import { environment } from 'projects/collect/src/environments/environment';
import { of } from 'rxjs';
import {
  ApiUnitObject,
  BASE_URL,
  ENVIRONMENT,
  InjectorModule,
  LoggerModule,
  ObjectQualifierType,
  Unit,
  VersionWithQualifierDto,
  WINDOW_LOCATION,
} from 'ui-frontend-common';
import { ArchiveCollectService } from '../../archive-collect.service';
import { CollectObjectGroupDetailsTabComponent } from './collect-object-group-details-tab.component';

describe('CollectObjectGroupDetailsTabComponent', () => {
  let component: CollectObjectGroupDetailsTabComponent;
  let fixture: ComponentFixture<CollectObjectGroupDetailsTabComponent>;

  const clipboardSpy = createSpyObj<Clipboard>('Clipboard', ['copy']);
  const archiveCollectServiceSpy = createSpyObj<ArchiveCollectService>('ArchiveService', [
    'launchDownloadObjectFromUnit',
    'getObjectGroupDetailsById',
  ]);

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        InjectorModule,
        LoggerModule.forRoot(),
        MatSnackBarModule,
        RouterTestingModule,
        BrowserAnimationsModule,
        TranslateModule.forRoot(),
      ],
      declarations: [CollectObjectGroupDetailsTabComponent],
      providers: [
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: ENVIRONMENT, useValue: environment },
        { provide: WINDOW_LOCATION, useValue: window.location },
        { provide: ArchiveCollectService, useValue: archiveCollectServiceSpy },
        { provide: Clipboard, useValue: clipboardSpy },
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CollectObjectGroupDetailsTabComponent);
    component = fixture.componentInstance;
    const archiveUnit: Unit = {
      '#allunitups': [],
      '#id': 'archiveUnitTestID',
      '#object': 'objectId',
      '#unitType': '',
      '#unitups': [],
      '#opi': '',
      '#tenant': 1,
      Title_: { fr: 'Teste', en: 'Test' },
      Description_: { fr: 'DescriptionFr', en: 'DescriptionEn' },
    };
    component.archiveUnit = archiveUnit;
    component.accessContract = 'accessContractTest';
    component.versionsWithQualifiersOrdered = [];
    fixture.detectChanges();
  });

  it('Component should be created', () => {
    expect(component).toBeTruthy();
  });

  it('should be opened', () => {
    // Given
    const versionWithQualifier = {
      opened: false,
      version: 2,
      qualifier: ObjectQualifierType.BINARYMASTER,
    } as VersionWithQualifierDto;

    // When
    component.openClose(versionWithQualifier);

    // Then
    expect(versionWithQualifier).not.toBeNull();
    expect(versionWithQualifier.opened).toBeTruthy();
  });

  it('should return true', () => {
    // Given
    component.archiveUnit = {
      '#allunitups': [],
      '#id': 'archiveUnitTestID',
      '#object': 'objectId',
      '#unitType': '',
      '#unitups': [],
      '#opi': '',
      '#tenant': 1,
      DescriptionLevel: 'Item',
      Title_: { fr: 'Teste', en: 'Test' },
      Description_: { fr: 'DescriptionFr', en: 'DescriptionEn' },
    };

    // When
    const response = component.unitHasObject();

    // Then
    expect(response).toBeTruthy();
  });

  it('should copy to clipboard', () => {
    component.copyToClipboard('à copié');
    expect(clipboardSpy.copy).toHaveBeenCalledWith('à copié');
  });

  it('onClickDownloadObject', () => {
    const event = {
      stopPropagation: () => {},
    } as Event;
    const preventDefaultSpy = spyOn(event, 'stopPropagation');
    component.onClickDownloadObject(event, newVersionWithQualifier(ObjectQualifierType.BINARYMASTER, 1));
    expect(archiveCollectServiceSpy.launchDownloadObjectFromUnit).toHaveBeenCalledWith(
      'archiveUnitTestID',
      'objectId',
      1,
      'accessContractTest',
      ObjectQualifierType.BINARYMASTER,
      1
    );
    expect(preventDefaultSpy).toHaveBeenCalled();
  });

  it('should change the open close status', () => {
    // Given
    const versionWithQualifier = {
      opened: false,
      version: 58,
      qualifier: ObjectQualifierType.BINARYMASTER,
    } as VersionWithQualifierDto;
    expect(component).toBeTruthy();
    component.versionsWithQualifiersOrdered.push(versionWithQualifier);

    // When
    component.setFirstVersionWithQualifierOpen();

    // Then
    expect(component.versionsWithQualifiersOrdered[0]).not.toBeNull();
    expect(component.versionsWithQualifiersOrdered[0].opened).toBeTruthy();
  });

  it('getObjectGroupDetailsById', () => {
    const unit = newUnit('unitId', 'objectId');
    archiveCollectServiceSpy.getObjectGroupDetailsById.and.returnValue(of(newApiUnitObject()));
    component.getObjectGroupDetailsById(unit);
    expect(archiveCollectServiceSpy.getObjectGroupDetailsById).toHaveBeenCalled();
    expect(archiveCollectServiceSpy.getObjectGroupDetailsById).toHaveBeenCalledWith(unit['#object']);
  });

  it('should return false', () => {
    // Given
    component.archiveUnit = {
      '#allunitups': [],
      '#id': 'archiveUnitTestID',
      '#object': 'objectId',
      '#unitType': '',
      '#unitups': [],
      '#opi': '',
      '#tenant': 1,
      DescriptionLevel: 'RecordGrp',
      Title_: { fr: 'Teste', en: 'Test' },
      Description_: { fr: 'DescriptionFr', en: 'DescriptionEn' },
    };

    // When
    const response = component.unitHasObject();

    // Then
    expect(response).toBeFalsy();
  });

  it('should be closed', () => {
    // Given
    const versionWithQualifier = {
      opened: true,
      version: 8,
      qualifier: ObjectQualifierType.BINARYMASTER,
    } as VersionWithQualifierDto;

    // When
    component.openClose(versionWithQualifier);

    // Then
    expect(versionWithQualifier).not.toBeNull();
    expect(versionWithQualifier.opened).toBeFalsy();
  });

  function newVersionWithQualifier(qualifier: ObjectQualifierType, version: number): VersionWithQualifierDto {
    return { qualifier, version } as VersionWithQualifierDto;
  }

  function newUnit(id: string, objectId: string): Unit {
    return { '#id': id, '#object': objectId } as Unit;
  }

  function newApiUnitObject(): ApiUnitObject {
    return { '#id': 'ApiUnitObjectID' } as ApiUnitObject;
  }
});
