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

import { Clipboard } from '@angular/cdk/clipboard';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TranslateModule } from '@ngx-translate/core';
import { of } from 'rxjs';
import {
  AccessContract,
  ApiUnitObject,
  DescriptionLevel,
  ObjectQualifierType,
  TenantSelectionService,
  Unit,
  VersionWithQualifierDto,
} from 'vitamui-library';
import { ArchiveService } from '../../archive.service';
import { ArchiveUnitObjectsDetailsTabComponent } from './archive-unit-objects-details-tab.component';
import createSpyObj = jasmine.createSpyObj;
import anything = jasmine.anything;

describe('ArchiveUnitObjectsDetailsTabComponent tests', () => {
  let component: ArchiveUnitObjectsDetailsTabComponent;
  let fixture: ComponentFixture<ArchiveUnitObjectsDetailsTabComponent>;
  const clipboardSpy = createSpyObj<Clipboard>('Clipboard', ['copy']);
  const archiveServiceSpy = createSpyObj<ArchiveService>('ArchiveService', [
    'downloadObjectFromUnit',
    'getObjectById',
    'getAccessContractById',
    'hasArchiveSearchRole',
  ]);

  archiveServiceSpy.getAccessContractById.and.returnValue(of({} as AccessContract));
  archiveServiceSpy.hasArchiveSearchRole.and.returnValue(of(true));
  const tenantSelectionServiceSpy = jasmine.createSpyObj('TenantSelectionService', {
    getSelectedTenant: {
      name: 'tenantName',
      identifier: 2,
      ownerId: 'owner',
      customerId: 'customer',
      enabled: true,
      proof: false,
      readonly: true,
      ingestContractHoldingIdentifier: 'string',
      itemIngestContractIdentifier: 'string',
      accessContractHoldingIdentifier: 'string',
      accessContractLogbookIdentifier: 'string',
    },
  });

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TranslateModule.forRoot()],
      declarations: [ArchiveUnitObjectsDetailsTabComponent],
      providers: [
        { provide: ArchiveService, useValue: archiveServiceSpy },
        { provide: TenantSelectionService, useValue: tenantSelectionServiceSpy },
        { provide: Clipboard, useValue: clipboardSpy },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ArchiveUnitObjectsDetailsTabComponent);
    component = fixture.componentInstance;
    component.archiveUnit = {
      '#allunitups': [],
      '#id': 'archiveUnitTestID',
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
    expect(archiveServiceSpy.downloadObjectFromUnit).toHaveBeenCalledWith('archiveUnitTestID', ObjectQualifierType.BINARYMASTER, 1);
    expect(preventDefaultSpy).toHaveBeenCalled();
  });

  it('getObjectVersionsWithQualifiers', () => {
    const unit = newUnit('zertyuhtfrc');
    archiveServiceSpy.getObjectById.and.returnValue(of(newApiUnitObject()));
    component.getObjectVersionsWithQualifiers(unit);
    expect(archiveServiceSpy.getObjectById).toHaveBeenCalled();
    expect(archiveServiceSpy.getObjectById).toHaveBeenCalledWith(unit['#id'], anything());
  });

  it('should return true', () => {
    // Given
    component.archiveUnit = {
      '#allunitups': [],
      '#id': 'archiveUnitTestID',
      '#object': 'objectId',
      '#unitType': null,
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

  it('should return false', () => {
    // Given
    component.archiveUnit = {
      '#allunitups': [],
      '#id': 'archiveUnitTestID',
      '#object': 'objectId',
      '#unitType': null,
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

  it('should return true', () => {
    // Given
    component.archiveUnit = {
      '#allunitups': [],
      '#id': 'archiveUnitTestID',
      '#unitType': null,
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
    expect(response).toBeFalsy();
  });

  function newVersionWithQualifier(qualifier: ObjectQualifierType, version: number): VersionWithQualifierDto {
    return { qualifier, version } as VersionWithQualifierDto;
  }

  function newUnit(id: string): Unit {
    return { '#id': id } as Unit;
  }

  function newApiUnitObject(): ApiUnitObject {
    return { '#id': 'ApiUnitObjectID' } as ApiUnitObject;
  }
});
