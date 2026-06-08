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
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateProfilNoticeComponent } from './create-profil-notice.component';
import { MatDialogRef } from '@angular/material/dialog';
import { ProfileService } from '../../core/services/profile.service';
import { ApplicationService, InputComponent, SelectComponent, SlideToggleComponent } from 'vitamui-library';
import { TranslateModule } from '@ngx-translate/core';
import { BehaviorSubject, of } from 'rxjs';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProfileType } from '../../models/profile-type.enum';
import { MatRadioModule } from '@angular/material/radio';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

describe('CreateProfilNoticeComponent', () => {
  let component: CreateProfilNoticeComponent;
  let fixture: ComponentFixture<CreateProfilNoticeComponent>;
  let mockDialogRef: any;
  let mockProfileService: any;
  let mockAppService: any;
  let mockTranslate: any;
  let externalIdSubject: BehaviorSubject<boolean>;

  beforeEach(() => {
    mockDialogRef = {
      close: vi.fn().mockName('MatDialogRef.close'),
    };
    mockProfileService = {
      checkPuaProfile: vi.fn().mockName('ProfileService.checkPuaProfile'),
      checkPaProfile: vi.fn().mockName('ProfileService.checkPaProfile'),
      controlSchema: vi.fn().mockName('ProfileService.controlSchema'),
    };
    mockAppService = {
      isApplicationExternalIdentifierEnabled: vi.fn().mockName('ApplicationService.isApplicationExternalIdentifierEnabled'),
    };
    mockProfileService.controlSchema = new BehaviorSubject<string>(JSON.stringify({ additionalProperties: true }));
    mockTranslate = {
      instant: vi.fn().mockName('TranslateService.instant'),
    };

    mockTranslate.instant.mockImplementation((key: string) => key);
    externalIdSubject = new BehaviorSubject<boolean>(true);
    mockAppService.isApplicationExternalIdentifierEnabled.mockReturnValue(externalIdSubject.asObservable());

    TestBed.configureTestingModule({
      imports: [
        ReactiveFormsModule,
        TranslateModule.forRoot(),
        MatRadioModule,
        InputComponent,
        SelectComponent,
        SlideToggleComponent,
        NoopAnimationsModule,
        CreateProfilNoticeComponent,
      ],
      providers: [
        FormBuilder,
        { provide: MatDialogRef, useValue: mockDialogRef },
        { provide: ProfileService, useValue: mockProfileService },
        { provide: ApplicationService, useValue: mockAppService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CreateProfilNoticeComponent);
    component = fixture.componentInstance;
  });

  it('should validate identifier via checkPaProfile when not PUA', async () => {
    component.profileTypeSignal.set(ProfileType.PA);
    fixture.detectChanges();
    mockProfileService.checkPaProfile.mockReturnValue(of(true));
    component.noticeForm.controls.identifier?.setValue('DUPLICATE');

    expect(mockProfileService.checkPaProfile).toHaveBeenCalledWith(expect.objectContaining({ identifier: 'DUPLICATE' }));
  });

  it('should disable identifier validators if External ID is disabled', async () => {
    fixture.detectChanges();
    component.noticeForm.controls.profileType?.setValue(ProfileType.PUA);

    externalIdSubject.next(false);

    component.profileTypeSignal.set(ProfileType.PA);
    component.profileTypeSignal.set(ProfileType.PUA);
    fixture.detectChanges();

    const identifierCtrl = component.noticeForm.controls.identifier;

    expect(component.externalIdentifierEnabledSignal()).toBe(false);

    expect(identifierCtrl?.hasValidator(Validators.required)).toBe(false);
    expect(identifierCtrl?.hasValidator(Validators.minLength(2))).toBe(false);
    expect(identifierCtrl?.hasValidator(Validators.maxLength(100))).toBe(false);

    identifierCtrl?.setValue('');
    expect(identifierCtrl?.valid).toBe(true);
    expect(identifierCtrl?.validator).toBeNull();
  });

  it('should check PA and set isIdentifierOK to true if unique', async () => {
    component.profileTypeSignal.set(ProfileType.PA);
    fixture.detectChanges();
    mockProfileService.checkPaProfile.mockReturnValue(of(false));
    component.noticeForm.controls.identifier?.setValue('UNIQUE_TEST');

    expect(mockProfileService.checkPaProfile).toHaveBeenCalledWith(expect.objectContaining({ identifier: 'UNIQUE_TEST' }));
    expect(component.noticeForm.controls.identifier.invalid).toBe(false);
  });

  it('should check PA and alert if duplicated', async () => {
    component.profileTypeSignal.set(ProfileType.PA);
    fixture.detectChanges();
    mockAppService.isApplicationExternalIdentifierEnabled.mockReturnValue(of(true));
    mockProfileService.checkPaProfile.mockReturnValue(of(true));

    component.noticeForm.controls.identifier?.setValue('DUPLICATE_PA');

    expect(mockProfileService.checkPaProfile).toHaveBeenCalledWith(expect.objectContaining({ identifier: 'DUPLICATE_PA' }));
    expect(component.noticeForm.controls.identifier.invalid).toBe(true);
  });

  it('should check PUA and set isIdentifierOK to true if unique', async () => {
    component.profileTypeSignal.set(ProfileType.PUA);
    fixture.detectChanges();
    mockProfileService.checkPuaProfile.mockReturnValue(of(false));
    component.noticeForm.controls.identifier?.setValue('UNIQUE_PUA');

    expect(mockProfileService.checkPuaProfile).toHaveBeenCalledWith(expect.objectContaining({ identifier: 'UNIQUE_PUA' }));
    expect(component.noticeForm.controls.identifier.invalid).toBe(false);
  });

  it('should check PUA and alert if duplicated', async () => {
    component.profileTypeSignal.set(ProfileType.PUA);
    fixture.detectChanges();
    mockProfileService.checkPuaProfile.mockReturnValue(of(true));
    component.noticeForm.controls.identifier?.setValue('DUPLICATE_TEST');

    expect(mockProfileService.checkPuaProfile).toHaveBeenCalledWith(expect.objectContaining({ identifier: 'DUPLICATE_TEST' }));
    component.noticeForm.controls.profileType?.setValue(ProfileType.PUA);
    expect(component.noticeForm.controls.identifier.invalid).toBe(true);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
