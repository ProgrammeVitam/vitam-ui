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
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { Observable, of } from 'rxjs';
import { InjectorModule, LoggerModule, StartupService } from 'ui-frontend-common';
import { ManagementRulesComponent } from './management-rules.component';

const translations: any = { TEST: 'Mock translate test' };

class FakeLoader implements TranslateLoader {
  getTranslation(): Observable<any> {
    return of(translations);
  }
}

describe('ManagementRulesComponent', () => {
  let component: ManagementRulesComponent;
  let fixture: ComponentFixture<ManagementRulesComponent>;

  const matDialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);
  const matDialogSpy = jasmine.createSpyObj('MatDialog', ['open']);

  const rulesCatygories = [
    { id: 'StorageRule', name: 'name', isDisabled: true },
    { id: 'AppraisalRule', name: 'name', isDisabled: false },
    { id: 'HoldRule', name: 'name', isDisabled: true },
    { id: 'AccessRule', name: 'name', isDisabled: true },
    { id: 'DisseminationRule', name: 'name', isDisabled: true },
    { id: 'ReuseRule', name: 'name', isDisabled: true },
    { id: 'ClassificationRule', name: 'name', isDisabled: true },
  ];

  const startupServiceStub = {
    getPortalUrl: () => '',
    getConfigStringValue: () => '',
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        InjectorModule,
        LoggerModule.forRoot(),
        TranslateModule.forRoot({
          loader: { provide: TranslateLoader, useClass: FakeLoader },
        }),
        MatSnackBarModule,
        HttpClientTestingModule,
        RouterTestingModule,
      ],
      declarations: [ManagementRulesComponent],
      providers: [
        FormBuilder,
        { provide: StartupService, useValue: startupServiceStub },
        { provide: MatDialogRef, useValue: matDialogRefSpy },
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: MAT_DIALOG_DATA, useValue: {} },
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ManagementRulesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
  it('Should have an accessContract ', () => {
    expect(component.accessContract).not.toBeNull();
  });
  it('items Selected should be grather than 0 ', () => {
    expect(component.selectedItem).toBeGreaterThan(0);
  });
  it('Should have a list of search criteria ', () => {
    expect(component.criteriaSearchListToSave).not.toBeNull();
  });

  it('Should the rule Category Selected be   ', () => {
    component.selectRule(rulesCatygories[0]);
    expect(component.ruleCategorySelected).toEqual('StorageRule');
  });
});
