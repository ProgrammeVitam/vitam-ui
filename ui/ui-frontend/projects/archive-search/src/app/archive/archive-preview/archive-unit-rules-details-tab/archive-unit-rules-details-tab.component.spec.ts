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
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { TranslateModule } from '@ngx-translate/core';
import { environment } from 'projects/archive-search/src/environments/environment';
import { BASE_URL, InjectorModule, VitamUISnackBar, WINDOW_LOCATION } from 'ui-frontend-common';
import { ArchiveUnitRulesDetailsTabComponent } from './archive-unit-rules-details-tab.component';

describe('ArchiveUnitRulesDetailsTabComponent', () => {
  let component: ArchiveUnitRulesDetailsTabComponent;
  let fixture: ComponentFixture<ArchiveUnitRulesDetailsTabComponent>;

  const snackBarSpy = jasmine.createSpyObj('MatSnackBar', ['open', 'openFromComponent']);

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ArchiveUnitRulesDetailsTabComponent],
      imports: [InjectorModule, MatSnackBarModule, HttpClientTestingModule, TranslateModule.forRoot()],
      providers: [
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: VitamUISnackBar, useValue: snackBarSpy },
        { provide: WINDOW_LOCATION, useValue: window.location },
        { provide: environment, useValue: environment },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(async(() => {
    fixture = TestBed.createComponent(ArchiveUnitRulesDetailsTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    component.archiveUnit = {
      '#allunitups': [],
      '#id': 'id',
      '#object': '',
      '#unitType': '',
      '#unitups': [],
      '#opi': '',
      Title_: { fr: 'Teste', en: 'Test' },
      Description_: { fr: 'DescriptionFr', en: 'DescriptionEn' },
    };
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should the length of listOfCriteriaSearch to be 1', () => {
    component.selectUnitWithInheritedRules(component.archiveUnit);
    expect(component.listOfCriteriaSearch.length).toEqual(1);
  });

  it('should the archiveUnitRules exists ', () => {
    component.selectUnitWithInheritedRules(component.archiveUnit);
    expect(component.archiveUnitRules).not.toBeNull();
  });
});
