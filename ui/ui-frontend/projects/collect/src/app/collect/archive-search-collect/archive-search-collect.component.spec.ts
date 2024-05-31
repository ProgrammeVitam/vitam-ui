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
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { environment } from 'projects/collect/src/environments/environment';
import { Observable, of } from 'rxjs';
import { BASE_URL, InjectorModule, LoggerModule, Transaction, TransactionStatus, WINDOW_LOCATION } from 'vitamui-library';
import { VitamUISnackBar } from '../shared/vitamui-snack-bar/vitamui-snack-bar.service';

import { ArchiveSearchCollectComponent } from './archive-search-collect.component';
import { ArchiveSearchHelperService } from './archive-search-criteria/services/archive-search-helper.service';
import { ArchiveSharedDataService } from './archive-search-criteria/services/archive-shared-data.service';

const translations: any = { TEST: 'Mock translate test' };

class FakeLoader implements TranslateLoader {
  getTranslation(): Observable<any> {
    return of(translations);
  }
}

describe('ArchiveSearchCollectComponent', () => {
  let component: ArchiveSearchCollectComponent;
  let fixture: ComponentFixture<ArchiveSearchCollectComponent>;

  const archiveSearchCommonService = {
    addCriteria: () => of(),
    buildNodesListForQUery: () => of(),
    buildFieldsCriteriaListForQUery: () => of(),
    buildManagementRulesCriteriaListForQuery: () => of(),
    checkIfRulesFacetsCanBeComputed: () => of(),
    updateCriteriaStatus: () => of(),
    removeCriteria: () => of(),
    recursiveCheck: () => of(),
  };
  const matDialogSpy = jasmine.createSpyObj('MatDialog', ['open']);
  matDialogSpy.open.and.returnValue({ afterClosed: () => of(true) });

  const snackBarSpy = jasmine.createSpyObj('MatSnackBar', ['open', 'openFromComponent']);

  const transaction: Transaction = {
    id: 'transactionId',
    archivalAgreement: 'archivalAgreement',
    messageIdentifier: 'messageIdentifier',
    archivalAgencyIdentifier: 'archivalAgencyIdentifier',
    transferringAgencyIdentifier: 'transferringAgencyIdentifier',
    originatingAgencyIdentifier: 'originatingAgencyIdentifier',
    submissionAgencyIdentifier: 'submissionAgencyIdentifier',
    archiveProfile: 'archivalProfile',
    projectId: 'ProjectId',
    comment: 'I am a comment',
    status: TransactionStatus.SENDING,
    legalStatus: 'A legal status',
    acquisitionInformation: 'Protocol',
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ArchiveSearchCollectComponent],
      imports: [
        InjectorModule,
        MatSidenavModule,
        MatSnackBarModule,
        BrowserAnimationsModule,
        LoggerModule.forRoot(),
        HttpClientTestingModule,
        RouterTestingModule,
        TranslateModule.forRoot({
          loader: { provide: TranslateLoader, useClass: FakeLoader },
        }),
      ],
      providers: [
        ArchiveSharedDataService,
        { provide: ArchiveSearchHelperService, useValue: archiveSearchCommonService },
        {
          provide: ActivatedRoute,
          useValue: {
            params: of({ tenantIdentifier: 1 }),
            data: of({ appId: 'COLLECT_APP' }),
            snapshot: {
              queryParamMap: {
                get: () => 'project messageIdentifier',
              },
            },
          },
        },
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: VitamUISnackBar, useValue: snackBarSpy },
        { provide: WINDOW_LOCATION, useValue: window.location },
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: environment, useValue: environment },
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ArchiveSearchCollectComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    component.transaction = transaction;
  });

  it('component should be created', () => {
    expect(component).toBeTruthy();
  });

  it('Some parameters should be true after initializing selection', () => {
    // When
    component.submit();

    // Then
    expect(component.pending).toBeTruthy();
    expect(component.submited).toBeTruthy();
    expect(component.itemSelected).toBe(0);
  });

  it('Some parameters should be false after initializing selection', () => {
    // When
    component.submit();

    // Then
    expect(component.isIndeterminate).toBeFalsy();
    expect(component.isAllChecked).toBeFalsy();
    expect(component.itemNotSelected).toBe(0);
  });

  it('should return true', () => {
    // When
    const response = component.updateUnitsMetadataDisabled();

    // Then
    expect(response).toBeTruthy();
  });
});
