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
import { Component, forwardRef, Input, NO_ERRORS_SCHEMA, ViewChild } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ControlValueAccessor, NG_VALUE_ACCESSOR, ReactiveFormsModule } from '@angular/forms';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelect, MatSelectModule } from '@angular/material/select';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { EMPTY, of, throwError as observableThrowError } from 'rxjs';
import { ConfirmDialogService } from 'vitamui-library';
import { AuthnRequestBindingEnum, newFile } from 'vitamui-library';
import { VitamUICommonTestModule } from 'vitamui-library/testing';
import { IdentityProviderService } from '../identity-provider.service';
import { IdentityProviderCreateComponent } from './identity-provider-create.component';

@Component({
  selector: 'app-pattern',
  template: '',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => PatternStubComponent),
      multi: true,
    },
  ],
  imports: [
    MatProgressBarModule,
    ReactiveFormsModule,
    MatButtonToggleModule,
    MatSelectModule,
    NoopAnimationsModule,
    VitamUICommonTestModule,
  ],
})
class PatternStubComponent implements ControlValueAccessor {
  @Input()
  options: Array<{
    value: string;
    disabled?: boolean;
  }>;
  @Input()
  vitamuiMiniMode = false;

  @ViewChild('select', { static: true })
  select: MatSelect;

  writeValue() {}
  registerOnChange() {}
  registerOnTouched() {}
}

describe('IdentityProviderCreateComponent', () => {
  let component: IdentityProviderCreateComponent;
  let fixture: ComponentFixture<IdentityProviderCreateComponent>;
  let keystore: File;
  let idpMetadata: File;

  beforeEach(async () => {
    const matDialogRefSpy = {
      close: vi.fn().mockName('MatDialogRef.close'),
    };
    const identityProviderServiceSpy = {
      create: vi.fn().mockName('OwnerService.create').mockReturnValue(of({})),
    };
    keystore = newFile(['keystore content'], 'test.jks');
    idpMetadata = newFile(['metadata content'], 'test.jks');

    await TestBed.configureTestingModule({
      imports: [
        MatProgressBarModule,
        ReactiveFormsModule,
        MatButtonToggleModule,
        MatSelectModule,
        NoopAnimationsModule,
        VitamUICommonTestModule,
        IdentityProviderCreateComponent,
        PatternStubComponent,
      ],
      schemas: [NO_ERRORS_SCHEMA],
      providers: [
        { provide: MatDialogRef, useValue: matDialogRefSpy },
        { provide: MAT_DIALOG_DATA, useValue: { customer: { id: '42', name: 'OwnerName' } } },
        { provide: IdentityProviderService, useValue: identityProviderServiceSpy },
        { provide: ConfirmDialogService, useValue: { listenToEscapeKeyPress: () => EMPTY } },
      ],
    })
      .overrideComponent(IdentityProviderCreateComponent, {
        set: {
          template: `
            <form [formGroup]="form" (ngSubmit)="onSubmit()">
              <input formControlName="customerId" />
              <input formControlName="name" />
              <input formControlName="internal" />
              <input formControlName="keystorePassword" />
              <input formControlName="patterns" />
              <input formControlName="enabled" />
              <input formControlName="mailAttribute" />
              <input formControlName="identifierAttribute" />
              <input formControlName="authnRequestBinding" />
              <input formControlName="autoProvisioningEnabled" />
              <input formControlName="protocoleType" />
              <input formControlName="maximumAuthenticationLifetime" />
              <input formControlName="wantsAssertionsSigned" />
              <input formControlName="authnRequestSigned" />
              <input formControlName="propagateLogout" />
              <input type="file" (change)="setKeystore($event.target.files)" />
              <input type="file" (change)="setIdpMetadata($event.target.files)" />
              <button type="submit">COMMON.SUBMIT</button>
              <button type="button" class="btn cancel" (click)="onCancel()">COMMON.UNDO</button>
            </form>
            <vitamui-slide-toggle [attr.formControlName]="'enabled'">CUSTOMER.SSO.ACTIVE_SWITCH</vitamui-slide-toggle>
            <vitamui-input [attr.formControlName]="'name'"></vitamui-input>
            <vitamui-input [attr.formControlName]="'keystorePassword'"></vitamui-input>
            <app-pattern [attr.formControlName]="'patterns'"></app-pattern>
            <vitamui-slide-toggle [attr.formControlName]="'autoProvisioningEnabled'">CUSTOMER.SSO.AUTO_PROVISIONING</vitamui-slide-toggle>
          `,
        },
      })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(IdentityProviderCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Class', () => {
    it('should call dialogRef.close', () => {
      const matDialogRef = TestBed.inject(MatDialogRef);
      component.onCancel();
      expect(matDialogRef.close).toHaveBeenCalled();
    });

    it('should not call idpService.create()', () => {
      const idpService = TestBed.inject(IdentityProviderService);
      component.onSubmit();
      expect(idpService.create).not.toHaveBeenCalled();
    });

    it('should call idpService.create()', () => {
      const idpService = TestBed.inject(IdentityProviderService);
      const matDialogRef = TestBed.inject(MatDialogRef);
      component.form.setValue({
        customerId: '1234',
        name: 'Test IDP',
        internal: true,
        keystorePassword: 'testpassword1234',
        patterns: ['test.com', 'test.fr'],
        enabled: true,
        mailAttribute: '',
        identifierAttribute: '',
        authnRequestBinding: AuthnRequestBindingEnum.POST,
        autoProvisioningEnabled: true,
        protocoleType: 'SAML',
        maximumAuthenticationLifetime: 0,
        wantsAssertionsSigned: true,
        authnRequestSigned: true,
        propagateLogout: false,
      });
      component.keystore = keystore;
      component.idpMetadata = idpMetadata;
      component.onSubmit();
      expect(idpService.create).toHaveBeenCalledWith(component.form.value);
      expect(matDialogRef.close).toHaveBeenCalled();
    });

    it('should set the files', () => {
      vi.spyOn(component, 'setKeystore');
      vi.spyOn(component, 'setIdpMetadata');
      const elInputs = fixture.nativeElement.querySelectorAll('input[type=file]');
      expect(elInputs.length).toBe(2);
      const customEvent = document.createEvent('CustomEvent');
      customEvent.initCustomEvent('change', false, false, null);
      elInputs[0].dispatchEvent(customEvent);
      elInputs[1].dispatchEvent(customEvent);
      expect(component.setKeystore).toHaveBeenCalled();
      expect(component.setIdpMetadata).toHaveBeenCalled();
      const mockKeystoreFileList = {
        item: () => keystore,
        length: 1,
        [Symbol.iterator]: function* () {
          yield keystore;
        },
      } as unknown as FileList;
      component.setKeystore(mockKeystoreFileList);
      expect(component.keystore).toEqual(keystore);
      const mockIdpMetadataFileList = {
        item: () => idpMetadata,
        length: 1,
        [Symbol.iterator]: function* () {
          yield idpMetadata;
        },
      } as unknown as FileList;
      component.setIdpMetadata(mockIdpMetadataFileList);
      expect(component.idpMetadata).toEqual(idpMetadata);
    });

    it('should set an error', () => {
      const idpService = TestBed.inject(IdentityProviderService);
      const matDialogRef = TestBed.inject(MatDialogRef);
      idpService.create = vi.fn().mockReturnValue(observableThrowError({ error: { error: 'INVALID_KEYSTORE_PASSWORD' } }));
      component.form.setValue({
        customerId: '1234',
        name: 'Test IDP',
        internal: true,
        keystorePassword: 'testpassword1234',
        patterns: ['test.com', 'test.fr'],
        enabled: true,
        mailAttribute: '',
        identifierAttribute: '',
        authnRequestBinding: AuthnRequestBindingEnum.POST,
        autoProvisioningEnabled: true,
        protocoleType: 'SAML',
        maximumAuthenticationLifetime: 0,
        wantsAssertionsSigned: true,
        authnRequestSigned: true,
        propagateLogout: false,
      });
      component.keystore = keystore;
      component.idpMetadata = idpMetadata;
      component.onSubmit();
      expect(idpService.create).toHaveBeenCalledWith(component.form.value);
      expect(matDialogRef.close).not.toHaveBeenCalled();
      expect(component.form.get('keystorePassword').errors).toEqual({ badPassword: true });
    });
  });

  describe('DOM', () => {
    it('should have all the inputs', () => {
      const elEnabled = fixture.nativeElement.querySelector('vitamui-slide-toggle[formControlName=enabled]');
      expect(elEnabled).toBeTruthy();
      expect(elEnabled.textContent).toContain('CUSTOMER.SSO.ACTIVE_SWITCH');

      const elName = fixture.nativeElement.querySelector('vitamui-input[formControlName=name]');
      expect(elName).toBeTruthy();

      const elKeystorePassword = fixture.nativeElement.querySelector('vitamui-input[formControlName=keystorePassword]');
      expect(elKeystorePassword).toBeTruthy();

      const elPatterns = fixture.nativeElement.querySelector('app-pattern[formControlName=patterns]');
      expect(elPatterns).toBeTruthy();

      const elAutoProvision = fixture.nativeElement.querySelector('vitamui-slide-toggle[formControlName=autoProvisioningEnabled]');
      expect(elAutoProvision).toBeTruthy();
      expect(elAutoProvision.textContent).toContain('CUSTOMER.SSO.AUTO_PROVISIONING');
    });

    it('should have a submit button', () => {
      const elSubmit = fixture.nativeElement.querySelector('button[type=submit]');
      expect(elSubmit).toBeTruthy();
      expect(elSubmit.textContent).toContain('COMMON.SUBMIT');
      component.form.setValue({
        customerId: '1234',
        name: 'Test IDP',
        internal: true,
        keystorePassword: 'testpassword1234',
        patterns: ['test.com', 'test.fr'],
        enabled: true,
        mailAttribute: '',
        identifierAttribute: '',
        authnRequestBinding: AuthnRequestBindingEnum.POST,
        autoProvisioningEnabled: true,
        protocoleType: 'SAML',
        maximumAuthenticationLifetime: 0,
        wantsAssertionsSigned: true,
        authnRequestSigned: true,
        propagateLogout: false,
      });
      component.keystore = keystore;
      component.idpMetadata = idpMetadata;
      fixture.detectChanges();
      vi.spyOn(component, 'onSubmit');
      elSubmit.click();
      expect(component.onSubmit).toHaveBeenCalledTimes(1);
    });

    it('should have a cancel button', () => {
      const elCancel = fixture.nativeElement.querySelector('button[type=button].btn.cancel');
      expect(elCancel).toBeTruthy();
      expect(elCancel.textContent).toContain('COMMON.UNDO');
      vi.spyOn(component, 'onCancel');
      elCancel.click();
      expect(component.onCancel).toHaveBeenCalledTimes(1);
    });
  });
});
