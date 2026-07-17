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
import { Clipboard } from '@angular/cdk/clipboard';
import { NO_ERRORS_SCHEMA, SimpleChange } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import {
  AccessContract,
  AccessContractService,
  ApiUnitObject,
  DescriptionLevel,
  ObjectQualifierType,
  TenantSelectionService,
  Unit,
  VersionWithQualifierDto,
} from 'vitamui-library';
import { ArchiveService } from '../../archive.service';
import { ArchiveUnitObjectsDetailsTabComponent } from './archive-unit-objects-details-tab.component';
import { vi } from 'vitest';
const createSpyObj = (name: string, methods: string[]) => Object.fromEntries(methods.map((m) => [m, vi.fn()]));
const anything = () => expect.anything();
import { ActivatedRoute } from '@angular/router';

describe('ArchiveUnitObjectsDetailsTabComponent', () => {
  let component: ArchiveUnitObjectsDetailsTabComponent;
  let fixture: ComponentFixture<ArchiveUnitObjectsDetailsTabComponent>;
  const clipboardSpy = createSpyObj('Clipboard', ['copy']);
  const archiveServiceSpy = createSpyObj('ArchiveService', [
    'downloadObjectFromUnit',
    'getObjectById',
    'getAccessContractById',
    'hasArchiveSearchRole',
  ]);

  archiveServiceSpy.getAccessContractById.mockReturnValue(of({} as AccessContract));
  archiveServiceSpy.hasArchiveSearchRole.mockReturnValue(of(true));
  const tenantSelectionServiceSpy = {
    getSelectedTenant: vi.fn().mockName('TenantSelectionService.getSelectedTenant').mockReturnValue({
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
    }),
  };

  const accessContractServiceMock = {
    currentAccessContract$: of({
      dataObjectVersion: [ObjectQualifierType.BINARYMASTER],
    } as AccessContract),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [],
      declarations: [ArchiveUnitObjectsDetailsTabComponent],
      providers: [
        { provide: ArchiveService, useValue: archiveServiceSpy },
        { provide: TenantSelectionService, useValue: tenantSelectionServiceSpy },
        { provide: Clipboard, useValue: clipboardSpy },
        { provide: AccessContractService, useValue: accessContractServiceMock },
        {
          provide: ActivatedRoute,
          useValue: {
            queryParams: of(),
          },
        },
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
    const preventDefaultSpy = vi.spyOn(event, 'stopPropagation');
    component.onClickDownloadObject(event, newVersionWithQualifier(ObjectQualifierType.BINARYMASTER, 1));
    expect(archiveServiceSpy.downloadObjectFromUnit).toHaveBeenCalledWith('archiveUnitTestID', ObjectQualifierType.BINARYMASTER, 1);
    expect(preventDefaultSpy).toHaveBeenCalled();
  });

  it('getObjectVersionsWithQualifiers', () => {
    const unit = {
      '#allunitups': [],
      '#id': 'archiveUnitTestID',
      '#object': 'objectId',
      '#unitType': null,
      '#unitups': [],
      '#opi': '',
      '#tenant': 1,
      DescriptionLevel: DescriptionLevel.ITEM as any,
      Title_: { fr: 'Teste', en: 'Test' },
      Description_: { fr: 'DescriptionFr', en: 'DescriptionEn' },
    } as Unit;
    archiveServiceSpy.getObjectById.mockReturnValue(of(newApiUnitObject()));
    component.archiveUnit = unit;
    component.ngOnChanges({
      archiveUnit: new SimpleChange(null, unit, true),
    });
    component.getObjectVersionsWithQualifiers();
    expect(archiveServiceSpy.getObjectById).toHaveBeenCalled();
    expect(archiveServiceSpy.getObjectById).toHaveBeenCalledWith(component.archiveUnit['#id'], anything());
  });

  it('unitHasObject should return true', () => {
    // Given
    component.archiveUnit = {
      '#allunitups': [],
      '#id': 'archiveUnitTestID',
      '#object': 'objectId',
      '#unitType': null,
      '#unitups': [],
      '#opi': '',
      '#tenant': 1,
      DescriptionLevel: DescriptionLevel.ITEM as any,
      Title_: { fr: 'Teste', en: 'Test' },
      Description_: { fr: 'DescriptionFr', en: 'DescriptionEn' },
    };

    // When
    const response = component.unitHasObject();

    // Then
    expect(response).toBeTruthy();
  });

  it('unitHasObject should return false', () => {
    // Given
    component.archiveUnit = {
      '#allunitups': [],
      '#id': 'archiveUnitTestID',
      '#unitType': null,
      '#unitups': [],
      '#opi': '',
      '#tenant': 1,
      DescriptionLevel: DescriptionLevel.RECORD_GRP as any,
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

  function newApiUnitObject(): ApiUnitObject {
    return { '#id': 'ApiUnitObjectID' } as ApiUnitObject;
  }
});
