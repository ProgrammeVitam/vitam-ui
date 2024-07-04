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
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatLegacyDialogModule as MatDialogModule } from '@angular/material/legacy-dialog';
import { MatSidenavModule } from '@angular/material/sidenav';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateModule } from '@ngx-translate/core';
import { InjectorModule, LoggerModule, WINDOW_LOCATION } from 'vitamui-library';
import { VitamUICommonTestModule } from 'vitamui-library/testing';
import { ManagementContractPreviewComponent } from './management-contract-preview.component';

describe('ManagementContractPreviewComponent', () => {
  let component: ManagementContractPreviewComponent;
  let fixture: ComponentFixture<ManagementContractPreviewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        MatSidenavModule,
        InjectorModule,
        VitamUICommonTestModule,
        HttpClientTestingModule,
        TranslateModule.forRoot(),
        RouterTestingModule,
        LoggerModule.forRoot(),
        MatDialogModule,
        ManagementContractPreviewComponent,
      ],
      providers: [{ provide: WINDOW_LOCATION, useValue: window.location }],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ManagementContractPreviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('component should be created', () => {
    expect(component).toBeTruthy();
  });

  it('should return false', () => {
    // Given
    const event = { id: 'id', eventDetails: 'test test' };

    // When
    const respone = component.filterEvents(event);

    // Then
    expect(respone).toBeFalsy();
  });

  it('should return true', () => {
    // Given
    const event = {
      id: 'aeeaaaaaaghjmgheaax42amfo3qv6jyaaaaq',
      evId: 'aeeaaaaaaghjmgheaax42amfo3qv6jyaaaaq',
      evIdReq: 'aeeaaaaaaghjmgheaax42amfo3qv6jyaaaaq',
      evType: 'PROCESS_SIP_UNITARY',
      evTypeProc: 'INGEST',
      evDateTime: '2023-01-03T09:05:59.803',
      outcome: 'STARTED',
      outDetail: 'STP_IMPORT_MANAGEMENT_CONTRACT.STARTED',
      outMessg: 'Début du processus du SIP : aeeaaaaaaghjmgheaax42amfo3qv6jyaaaaq',
      evDetData:
        '{\n  "EvDetailReq" : "Versement",\n  "EvDateTimeReq" : "2023-01-03T09:05:57",\n  "ArchivalAgreement" : "ArchivalAgreement0",\n  "ServiceLevel" : null,\n  "_up" : [ "aeaqaaaaaehffa3qaavjkamfop2tqpyaaada" ]\n}',
      obId: 'aeeaaaaaaghjmgheaax42amfo3qv6jyaaaaq',
      agId: '{"Name":"vitam-env-itrec-vm-2","Role":"ingest-external","ServerId":1050024164,"SiteId":1,"GlobalPlatformId":244717796}',
      agIdApp: 'CT-000001',
      agIdExt: '{"originatingAgency":"Identifier0","TransferringAgency":"Vitam","ArchivalAgency":"Vitam","submissionAgency":"Identifier0"}',
      rightsStatementIdentifier: '{"ArchivalAgreement":"ArchivalAgreement0"}',

      obIdIn: 'Versement',
    };

    // When
    const respone = component.filterEvents(event);

    // Then
    expect(respone).toBeTruthy();
  });

  it('should return the exact array ', () => {
    // Given
    component.tabUpdated = [false, false];
    const expectedArray = [true, false];
    // When
    component.updatedChange(true, 0);

    // Then
    expect(component.tabUpdated).not.toBeNull();
    expect(component.tabUpdated.length).toEqual(2);
    expect(component.tabUpdated).toEqual(expectedArray);
  });

  describe('DOM', () => {
    it('should have 4 angular mat tab', () => {
      const nativeElement = fixture.nativeElement;
      const elementMatTab = nativeElement.querySelectorAll('mat-tab');
      expect(elementMatTab.length).toBe(4);
    });
  });
});
