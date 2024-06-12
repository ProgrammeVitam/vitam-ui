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

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatLegacySnackBarModule as MatSnackBarModule } from '@angular/material/legacy-snack-bar';
import { TranslateModule } from '@ngx-translate/core';
import { environment } from 'projects/archive-search/src/environments/environment';
import { of } from 'rxjs';
import { BASE_URL, InjectorModule, Unit, WINDOW_LOCATION } from 'vitamui-library';
import { ArchiveService } from '../../archive.service';
import { ArchiveUnitRulesDetailsTabComponent } from './archive-unit-rules-details-tab.component';

describe('ArchiveUnitRulesDetailsTabComponent', () => {
  let component: ArchiveUnitRulesDetailsTabComponent;
  let fixture: ComponentFixture<ArchiveUnitRulesDetailsTabComponent>;

  const unitResponse: Unit = {
    '#allunitups': [],
    '#id': 'id',
    '#object': '',
    '#unitType': undefined,
    '#unitups': [],
    '#opi': '',
    StartDate: new Date(),
    EndDate: new Date(),
    Title_: { fr: 'Teste', en: 'Test' },
    Description_: { fr: 'DescriptionFr', en: 'DescriptionEn' },
  };

  const archiveServiceMock = {
    getBaseUrl: () => '/fake-api',
    selectUnitWithInheritedRules: () => of(unitResponse),
    openSnackBarForWorkflow: () => of({}),
    launchDownloadObjectFromUnit: () => of({}),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ArchiveUnitRulesDetailsTabComponent],
      imports: [InjectorModule, MatSnackBarModule, HttpClientTestingModule, TranslateModule.forRoot()],
      providers: [
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: WINDOW_LOCATION, useValue: window.location },
        { provide: environment, useValue: environment },
        { provide: ArchiveService, useValue: archiveServiceMock },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(waitForAsync(() => {
    fixture = TestBed.createComponent(ArchiveUnitRulesDetailsTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    component.archiveUnit = {
      '#allunitups': [],
      '#id': 'id',
      '#object': '',
      '#unitType': undefined,
      '#unitups': [],
      '#opi': '',
      Title_: { fr: 'Teste', en: 'Test' },
      Description_: { fr: 'DescriptionFr', en: 'DescriptionEn' },
    };
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should the length of listOfCriteriaSearch to be 0', () => {
    component.selectUnitWithInheritedRules(component.archiveUnit);
    expect(component.listOfCriteriaSearch.length).toEqual(0);
  });

  it('should the archiveUnitRules exists ', () => {
    component.selectUnitWithInheritedRules(component.archiveUnit);
    expect(component.archiveUnitRules).not.toBeNull();
  });

  it('should call selectUnitWithInheritedRules of archiveService', () => {
    // Given
    spyOn(archiveServiceMock, 'selectUnitWithInheritedRules').and.callThrough();

    // When
    component.selectUnitWithInheritedRules(component.archiveUnit);

    // Then
    expect(archiveServiceMock.selectUnitWithInheritedRules).toHaveBeenCalled();
  });

  describe('DOM', () => {
    it('should have 8 rows ', () => {
      // When
      const nativeElement = fixture.nativeElement;
      const elementRow = nativeElement.querySelectorAll('.row');

      // Then
      expect(elementRow.length).toBe(8);
    });
    it('should have 7 columns ', () => {
      // When
      const nativeElement = fixture.nativeElement;
      const elementColumn = nativeElement.querySelectorAll('.col');

      // Then
      expect(elementColumn.length).toBe(7);
    });
  });
});
