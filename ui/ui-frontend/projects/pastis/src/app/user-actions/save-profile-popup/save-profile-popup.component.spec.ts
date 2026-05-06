import type { MockedObject } from 'vitest';
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
import { ComponentFixture, fakeAsync, TestBed } from '@angular/core/testing';

import { SaveProfilePopupComponent } from './save-profile-popup.component';
import { MatDialogRef } from '@angular/material/dialog';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { MatRadioModule } from '@angular/material/radio';
import { ApplicationService, InputComponent, SelectComponent, SlideToggleComponent } from 'vitamui-library';
import { ProfileService } from '../../core/services/profile.service';
import { ProfileType } from '../../models/profile-type.enum';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { IdentifierExistsValidator } from '../../validators/IdentifierExistsValidator';
import { BehaviorSubject, of } from 'rxjs';

const mockTranslatedStrings = {
  'PROFILE.POP_UP_SAVE.CHOICE.FIRST_CHOICE_ENREGISTREMENT': 'Choix 1 Enregistrement',
  'PROFILE.POP_UP_SAVE.CHOICE.SECOND_CHOICE_ENREGISTREMENT': 'Choix 2 Enregistrement',
  'PROFILE.POP_UP_SAVE.CHOICE.FIRST_CHOICE_GESTION_NOTICE': 'Choix 1 Gestion',
  'PROFILE.POP_UP_SAVE.CHOICE.SECOND_CHOICE_GESTION_NOTICE': 'Choix 2 Gestion',
  'PROFILE.POP_UP_CREATION_NOTICE.CHOICE.PROFIL_ACTIF': 'Actif',
  'PROFILE.POP_UP_CREATION_NOTICE.CHOICE.PROFIL_INACTIF': 'Inactif',
};

describe('SaveProfilePopupComponent', () => {
  let component: SaveProfilePopupComponent;
  let fixture: ComponentFixture<SaveProfilePopupComponent>;
  let mockDialogRef: MockedObject<MatDialogRef<SaveProfilePopupComponent>>;
  let mockProfileService: any;
  let mockAppService: MockedObject<ApplicationService>;
  let mockTranslateService: MockedObject<TranslateService>;
  let externalIdSubject: BehaviorSubject<boolean>;

  beforeEach(() => {
    mockDialogRef = {
      close: vi.fn().mockName('MatDialogRef.close'),
    };
    mockProfileService = {
      checkPuaProfile: vi.fn().mockName('ProfileService.checkPuaProfile'),
      checkPaProfile: vi.fn().mockName('ProfileService.checkPaProfile'),
      getAllProfilesPUA: vi.fn().mockName('ProfileService.getAllProfilesPUA'),
      getAllProfilesPA: vi.fn().mockName('ProfileService.getAllProfilesPA'),
      controlSchema: vi.fn().mockName('ProfileService.controlSchema'),
    };
    mockProfileService.checkPaProfile.mockReturnValue(of(false));
    mockAppService = {
      isApplicationExternalIdentifierEnabled: vi.fn().mockName('ApplicationService.isApplicationExternalIdentifierEnabled'),
    };
    mockProfileService.controlSchema = new BehaviorSubject<string>(JSON.stringify({ additionalProperties: true }));
    mockTranslateService = {
      instant: vi.fn().mockName('TranslateService.instant'),
    };

    mockTranslateService.instant.mockImplementation((key: keyof typeof mockTranslatedStrings) => mockTranslatedStrings[key] || key);
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
        SaveProfilePopupComponent,
      ],
      providers: [
        FormBuilder,
        IdentifierExistsValidator,
        { provide: MatDialogRef, useValue: mockDialogRef },
        {
          provide: ProfileService,
          useValue: mockProfileService,
        },
        { provide: ApplicationService, useValue: mockAppService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(SaveProfilePopupComponent);
    component = fixture.componentInstance;
  });

  it('should close dialog with action "local" when valueSelected is false', () => {
    component.valueSelected.set(false);

    component.validate();

    expect(mockDialogRef.close).toHaveBeenCalledWith({ success: true, action: 'local' });
  });

  it('should close dialog with action "creation" and form data when gestionNotice is true and form is valid', () => {
    fixture.detectChanges();
    component.valueSelected.set(true);
    component.gestionNotice.set(true);
    component.externalIdentifierEnabled = false;

    component.noticeForm.controls.name.setValue('test-name');
    component.noticeForm.controls.identifier.setValue('test-id');

    vi.spyOn(component as any, 'updateControlSchema');

    component.validate();
    expect(mockDialogRef.close).toHaveBeenCalledWith({
      success: true,
      action: 'creation',
      data: component.noticeForm.getRawValue(),
    });
  });

  it('should close dialog with action "rattachement" when gestionNotice is false', () => {
    fixture.detectChanges();

    component.valueSelected.set(true);
    component.gestionNotice.set(false);
    component.selectedProfile = { identifier: 'id', name: 'name' } as any;

    component.validate();

    expect(mockDialogRef.close).toHaveBeenCalledWith({
      success: true,
      action: 'rattachement',
      data: component.selectedProfile,
    });
  });

  it('should check PA and call validate identifier', fakeAsync(() => {
    fixture.detectChanges();

    mockProfileService.profileType = ProfileType.PA;
    mockProfileService.checkPaProfile.mockReturnValue(of(false));

    component.noticeForm.controls.identifier.setValue('UNIQUE_PA');
    expect(mockProfileService.checkPaProfile).toHaveBeenCalledWith(expect.objectContaining({ identifier: 'UNIQUE_PA' }));
    expect(component.noticeForm.controls.identifier.invalid).toBe(false);
  }));

  it('should check PUA and alert if identifier is duplicated', fakeAsync(() => {
    const MOCK_PROFILE_DESCRIPTIONS = [
      { identifier: 'PUA_001', name: 'Profile PUA One' },
      { identifier: 'PUA_002', name: 'Profile PUA Two' },
    ];
    mockProfileService.profileType = ProfileType.PUA;
    mockProfileService.checkPuaProfile.mockReturnValue(of(true));
    mockProfileService.getAllProfilesPUA.mockReturnValue(of(MOCK_PROFILE_DESCRIPTIONS));
    fixture.detectChanges();

    component.noticeForm.controls.identifier.setValue('DUPLICATE_PUA');
    expect(mockProfileService.checkPuaProfile).toHaveBeenCalledWith(expect.objectContaining({ identifier: 'DUPLICATE_PUA' }));
    expect(component.noticeForm.controls.identifier.invalid).toBe(true);
  }));

  it('should set validators on identifier when external ID is enabled', fakeAsync(() => {
    fixture.detectChanges();

    mockAppService.isApplicationExternalIdentifierEnabled.mockReturnValue(of(true));
    mockProfileService.profileType = ProfileType.PA;

    component.noticeForm.controls.identifier.setValue('');
    component['setUpIdentifierValidator']();

    const ctrl = component.noticeForm.controls.identifier;

    expect(ctrl?.hasValidator(Validators.required)).toBe(true);
    expect(ctrl.valid).toBe(false);
  }));

  it('should clear validators on identifier when external ID is disabled', fakeAsync(() => {
    fixture.detectChanges();

    mockAppService.isApplicationExternalIdentifierEnabled.mockReturnValue(of(false));
    mockProfileService.profileType = ProfileType.PUA;

    component.noticeForm.controls.identifier.setValidators([Validators.required]);
    component.noticeForm.controls.identifier.setValue('');
    component['setUpIdentifierValidator']();

    const ctrl = component.noticeForm.controls.identifier;
    expect(ctrl.validator).toBeNull();
    expect(ctrl.valid).toBe(true);
  }));
});
