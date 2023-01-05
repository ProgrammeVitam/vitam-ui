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

import { DatePipe } from '@angular/common';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NO_ERRORS_SCHEMA, Pipe, PipeTransform } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTreeModule } from '@angular/material/tree';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { environment } from 'projects/archive-search/src/environments/environment';
import { Observable, of } from 'rxjs';
import { BASE_URL, InjectorModule, LoggerModule, VitamuiRoles, WINDOW_LOCATION } from 'ui-frontend-common';
import { ArchiveSharedDataService } from '../../core/archive-shared-data.service';
import { ArchiveService } from '../archive.service';
import { ArchiveSearchHelperService } from '../common-services/archive-search-helper.service';
import { ArchiveUnitDipService } from '../common-services/archive-unit-dip.service';
import { ArchiveUnitEliminationService } from '../common-services/archive-unit-elimination.service';
import { ComputeInheritedRulesService } from '../common-services/compute-inherited-rules.service';
import { UpdateUnitManagementRuleService } from '../common-services/update-unit-management-rule.service';
import { PagedResult, SearchCriteriaStatusEnum } from '../models/search.criteria';
import { VitamUISnackBar } from '../shared/vitamui-snack-bar';
import { ArchiveSearchComponent } from './archive-search.component';
import { TransferAcknowledgmentComponent } from './transfer-acknowledgment/transfer-acknowledgment.component';

@Pipe({ name: 'truncate' })
class MockTruncatePipe implements PipeTransform {
  transform(value: number): number {
    return value;
  }
}

const translations: any = { TEST: 'Mock translate test' };

class FakeLoader implements TranslateLoader {
  getTranslation(): Observable<any> {
    return of(translations);
  }
}

describe('ArchiveSearchComponent', () => {
  let component: ArchiveSearchComponent;
  let fixture: ComponentFixture<ArchiveSearchComponent>;
  const pagedResult: PagedResult = { pageNumbers: 1, facets: [], results: [], totalResults: 1 };

  const matDialogSpy = jasmine.createSpyObj('MatDialog', ['open']);
  matDialogSpy.open.and.returnValue({ afterClosed: () => of(true) });

  const snackBarSpy = jasmine.createSpyObj('MatSnackBar', ['open', 'openFromComponent']);

  const archiveServiceStub = {
    loadFilingHoldingSchemeTree: () => of([]),
    getOntologiesFromJson: () => of([]),
    searchArchiveUnitsByCriteria: () => of(pagedResult),
    hasArchiveSearchRole: () => of(true),
    getAccessContractById: () => of({})
  };
  const archiveSearchCommonService = {
    addCriteria: () => of(),
    removeCriteria: () => of(),
    buildNodesListForQUery: () => of(),
    buildManagementRulesCriteriaListForQuery: () => of(),
    buildFieldsCriteriaListForQUery: () => of(),
  };

  const updateUnitManagementRuleServiceMock = {
    goToUpdateManagementRule: () => of(),
  };
  const archiveUnitEliminationServiceMock = {
    launchEliminationAnalysisModal: () => of(),
    launchEliminationModal: () => of(),
  };
  const archiveUnitDipServiceMock = {
    launchExportDipModal: () => of(),
  };
  const computeInheritedRulesServiceMock = {
    launchComputedInheritedRulesModal: () => of(),
  };

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        MatMenuModule,
        MatTreeModule,
        MatProgressSpinnerModule,
        MatSidenavModule,
        InjectorModule,
        LoggerModule.forRoot(),
        TranslateModule.forRoot({
          loader: { provide: TranslateLoader, useClass: FakeLoader },
        }),
        MatSnackBarModule,
        HttpClientTestingModule,
        RouterTestingModule,
      ],
      declarations: [ArchiveSearchComponent, MockTruncatePipe],
      providers: [
        FormBuilder,
        ArchiveSharedDataService,
        DatePipe,
        { provide: ArchiveService, useValue: archiveServiceStub },
        { provide: ArchiveSearchHelperService, useValue: archiveSearchCommonService },
        {
          provide: ActivatedRoute,
          useValue: { params: of({ tenantIdentifier: 1 }), data: of({ appId: 'ARCHIVE_SEARCH_MANAGEMENT_APP' }) },
        },
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: VitamUISnackBar, useValue: snackBarSpy },
        { provide: WINDOW_LOCATION, useValue: window.location },
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: environment, useValue: environment },
        { provide: UpdateUnitManagementRuleService, useValue: updateUnitManagementRuleServiceMock },
        { provide: ArchiveUnitEliminationService, useValue: archiveUnitEliminationServiceMock },
        { provide: ComputeInheritedRulesService, useValue: computeInheritedRulesServiceMock },
        { provide: ArchiveUnitDipService, useValue: archiveUnitDipServiceMock },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ArchiveSearchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('Component should be created', () => {
    expect(component).toBeTruthy();
  });

  it('should have the corrects values', () => {
    expect(component.DEFAULT_ELIMINATION_ANALYSIS_THRESHOLD).toEqual(100000);
    expect(component.DEFAULT_DIP_EXPORT_THRESHOLD).toEqual(100000);
    expect(component.DEFAULT_ELIMINATION_THRESHOLD).toEqual(10000);
    expect(component.DEFAULT_TRANSFER_THRESHOLD).toEqual(100000);
    expect(component.DEFAULT_UPDATE_MGT_RULES_THRESHOLD).toEqual(100000);
  });

  it('should be true', () => {
    component.showHideDuaEndDate(true);
    expect(component.showDuaEndDate).toBeTruthy();
  });

  it('should be false', () => {
    component.showHidePanel(false);
    expect(component.showCriteriaPanel).toBeFalsy();
  });

  it('should call hasArchiveSearchRole', () => {
    spyOn(archiveServiceStub, 'hasArchiveSearchRole').and.callThrough();
    // When
    component.checkUserHasRole(VitamuiRoles.ROLE_EXPORT_DIP, 1);

    // Then
    expect(archiveServiceStub.hasArchiveSearchRole).toHaveBeenCalled();
  });
  it('should open a modal with TransferAcknowledgmentComponent', () => {
    component.accessContract = 'accessContract';
    component.showAcknowledgmentTransferForm();
    expect(matDialogSpy.open).toHaveBeenCalledWith(TransferAcknowledgmentComponent, {
      panelClass: 'vitamui-modal',
      disableClose: true,
      data: {
        accessContract: 'accessContract',
        tenantIdentifier: '1',
      },
    });
  });

  describe('submit', () => {
    it('should check all criteria as included when submit', () => {
      component.submit();
      component.searchCriterias.forEach((criteria) => {
        criteria.values.forEach((criteriaValue) => {
          expect(criteriaValue.status).toEqual(SearchCriteriaStatusEnum.NOT_INCLUDED);
        });
      });
    });
  });

  describe('DOM', () => {
    it('should have 5 rows ', () => {
      // When
      const nativeElement = fixture.nativeElement;
      const elementRow = nativeElement.querySelectorAll('.row');

      // Then
      expect(elementRow.length).toBe(5);
    });

    it('should have 1 vitamui-common-menu-button ', () => {
      // When
      const nativeElement = fixture.nativeElement;
      const elementRow = nativeElement.querySelectorAll('vitamui-common-menu-button');

      // Then
      expect(elementRow.length).toBe(1);
    });
    it('should have 2 buttons ', () => {
      const elementBtn = fixture.nativeElement.querySelectorAll('button[type=button]');
      expect(elementBtn.length).toBe(2);
    });
  });
});
