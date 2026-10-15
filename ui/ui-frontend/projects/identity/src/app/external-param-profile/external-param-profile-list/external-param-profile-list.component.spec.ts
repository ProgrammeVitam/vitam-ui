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
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { of, Subject } from 'rxjs';
import { VitamUICommonTestModule } from 'vitamui-library/testing';
import { ProfileValidators } from '../../hierarchy/profile.validators';
import { ProfileService } from '../../profile/profile.service';
import { ExternalParamProfileService } from '../external-param-profile.service';
import { ExternalParamProfileListComponent } from './external-param-profile-list.component';
import { CollapseComponent } from 'vitamui-library';
import { CommonModule } from '@angular/common';

describe('ExternalParamProfileListComponent', () => {
  let component: ExternalParamProfileListComponent;
  let fixture: ComponentFixture<ExternalParamProfileListComponent>;

  const matDialogRefSpy = {
    close: vi.fn().mockName('MatDialogRef.close'),
  };
  const profileServiceSpy = {
    create: vi.fn().mockName('ProfileService.create').mockReturnValue(of({})),
  };
  const profileValidatorsSpy = {
    create: vi.fn().mockName('ProfileValidators.create').mockReturnValue(of({})),
  };

  const externalParamListServiceSpy = {
    search: () => of([]),
    canLoadMore: true,
    loadMore: () => of([]),
    updated: new Subject(),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        ReactiveFormsModule,
        MatProgressBarModule,
        MatButtonToggleModule,
        VitamUICommonTestModule,
        ExternalParamProfileListComponent,
        CollapseComponent,
        CommonModule,
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
