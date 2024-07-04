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
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatLegacyDialogRef as MatDialogRef, MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA } from '@angular/material/legacy-dialog';
import { MatLegacyProgressBarModule as MatProgressBarModule } from '@angular/material/legacy-progress-bar';
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { Observable, of, Subject } from 'rxjs';
import { CollapseModule } from 'vitamui-library';
import { VitamUICommonTestModule } from 'vitamui-library/testing';
import { ProfileValidators } from '../../hierarchy/profile.validators';
import { ProfileService } from '../../profile/profile.service';
import { ExternalParamProfileService } from '../external-param-profile.service';
import { ExternalParamProfileListComponent } from './external-param-profile-list.component';

const translations: any = { TEST: 'Mock translate test' };

class FakeLoader implements TranslateLoader {
  getTranslation(): Observable<any> {
    return of(translations);
  }
}

describe('ExternalParamProfileListComponent', () => {
  let component: ExternalParamProfileListComponent;
  let fixture: ComponentFixture<ExternalParamProfileListComponent>;

  const matDialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);
  const profileServiceSpy = jasmine.createSpyObj('ProfileService', { create: of({}) });
  const profileValidatorsSpy = jasmine.createSpyObj('ProfileValidators', { create: of({}) });

  const externalParamListServiceSpy = {
    search: () => of([]),
    canLoadMore: true,
    loadMore: () => of([]),
    updated: new Subject(),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        TranslateModule.forRoot({
          loader: { provide: TranslateLoader, useClass: FakeLoader },
        }),
        ReactiveFormsModule,
        MatProgressBarModule,
        CollapseModule,
        MatButtonToggleModule,
        VitamUICommonTestModule,
        ExternalParamProfileListComponent,
      ],
      providers: [
        { provide: MatDialogRef, useValue: matDialogRefSpy },
        { provide: ExternalParamProfileService, useValue: externalParamListServiceSpy },
        { provide: MAT_DIALOG_DATA, useValue: {} },
        { provide: ProfileService, useValue: profileServiceSpy },
        { provide: ProfileValidators, useValue: profileValidatorsSpy },
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ExternalParamProfileListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
