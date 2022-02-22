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


import { Component, forwardRef, Input, ViewChild } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { ControlValueAccessor, NG_VALUE_ACCESSOR, ReactiveFormsModule } from '@angular/forms';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelect, MatSelectModule } from '@angular/material/select';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { EMPTY, of, throwError as observableThrowError } from 'rxjs';
import { AuthnRequestBindingEnum, ConfirmDialogService, newFile } from 'ui-frontend-common';
import { VitamUICommonTestModule } from 'ui-frontend-common/testing';
import { IdentityProviderService } from '../identity-provider.service';
import { IdentityProviderCreateComponent } from './identity-provider-create.component';

@Component({
  selector: 'app-pattern',
  template: '',
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => PatternStubComponent),
    multi: true
  }]
})
class PatternStubComponent implements ControlValueAccessor {
  @Input() options: Array<{ value: string, disabled?: boolean }>;
  @Input() vitamuiMiniMode = false;

  @ViewChild('select', { static: true }) select: MatSelect;

  writeValue() {}
  registerOnChange() {}
  registerOnTouched() {}
}


describe('IdentityProviderCreateComponent', () => {
  let component: IdentityProviderCreateComponent;
  let fixture: ComponentFixture<IdentityProviderCreateComponent>;
  let keystore: File;
  let idpMetadata: File;

  beforeEach(waitForAsync(() => {
    const matDialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);
    const identityProviderServiceSpy = jasmine.createSpyObj('OwnerService', { create: of({}) });
    keystore = newFile(['keystore content'], 'test.jks');
    idpMetadata = newFile(['metadata content'], 'test.jks');

    TestBed.configureTestingModule({
      imports: [
        MatProgressBarModule,
        ReactiveFormsModule,
        MatButtonToggleModule,
        MatSelectModule,
        NoopAnimationsModule,
        VitamUICommonTestModule,
      ],
      declarations: [ IdentityProviderCreateComponent, PatternStubComponent ],
      providers: [
        { provide: MatDialogRef, useValue: matDialogRefSpy },
        { provide: MAT_DIALOG_DATA, useValue: { customer: { id: '42', name: 'OwnerName' } } },
        { provide: IdentityProviderService, useValue: identityProviderServiceSpy },
        { provide: ConfirmDialogService, useValue: { listenToEscapeKeyPress: () => EMPTY } },
      ]
    })
    .compileComponents();
  }));

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
      const matDialogRef =  TestBed.inject(MatDialogRef);
      component.onCancel();
      expect(matDialogRef.close).toHaveBeenCalled();
    });

    it('should not call idpService.create()', () => {
      const idpService =  TestBed.inject(IdentityProviderService);
      component.onSubmit();
      expect(idpService.create).not.toHaveBeenCalled();
    });

    it('should call idpService.create()', () => {
      const idpService =  TestBed.inject(IdentityProviderService);
      const matDialogRef =  TestBed.inject(MatDialogRef);
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
        protocoleType: "SAML"
      });
      component.keystore = keystore;
      component.idpMetadata = idpMetadata;
      component.onSubmit();
      expect(idpService.create).toHaveBeenCalledWith(component.form.value);
      expect(matDialogRef.close).toHaveBeenCalled();
    });

    it('should set the files', () => {
      spyOn(component, 'setKeystore').and.callThrough();
      spyOn(component, 'setIdpMetadata').and.callThrough();
      const elInputs = fixture.nativeElement.querySelectorAll('input[type=file]');
      expect(elInputs.length).toBe(2);
      const customEvent = document.createEvent('CustomEvent');
      customEvent.initCustomEvent('change', false, false, null);
      elInputs[0].dispatchEvent(customEvent);
      elInputs[1].dispatchEvent(customEvent);
      expect(component.setKeystore).toHaveBeenCalled();
      expect(component.setIdpMetadata).toHaveBeenCalled();
      component.setKeystore({ item: () => keystore, length: 1 });
      expect(component.keystore).toEqual(keystore);
      component.setIdpMetadata({ item: () => idpMetadata, length: 1 });
      expect(component.idpMetadata).toEqual(idpMetadata);
    });

    it('should set an error', () => {
      const idpService =  TestBed.inject(IdentityProviderService);
      const matDialogRef =  TestBed.inject(MatDialogRef);
      idpService.create = jasmine.createSpy().and.returnValue(observableThrowError({ error: { error: 'INVALID_KEYSTORE_PASSWORD' } }));
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
        protocoleType:'SAML'
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

    it('should have a title', () => {
      const elTitle = fixture.nativeElement.querySelector('.large');
      expect(elTitle.textContent).toContain('CUSTOMER.SSO.MODAL.TITLE "OwnerName"');
    });

    it('should have all the inputs', () => {

      const elEnabled = fixture.nativeElement.querySelector('vitamui-common-slide-toggle[formControlName=enabled]');
      expect(elEnabled).toBeTruthy();
      expect(elEnabled.textContent).toContain('CUSTOMER.SSO.ACTIVE_SWITCH');

      const elName = fixture.nativeElement.querySelector('vitamui-common-input[formControlName=name]');
      expect(elName).toBeTruthy();

      const elKeystorePassword = fixture.nativeElement.querySelector('vitamui-common-input[formControlName=keystorePassword]');
      expect(elKeystorePassword).toBeTruthy();

      const elPatterns = fixture.nativeElement.querySelector('app-pattern[formControlName=patterns]');
      expect(elPatterns).toBeTruthy();

      const elAutoProvision = fixture.nativeElement.querySelector('vitamui-common-slide-toggle[formControlName=autoProvisioningEnabled]');
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
        protocoleType: "SAML",
      });
      component.keystore = keystore;
      component.idpMetadata = idpMetadata;
      fixture.detectChanges();
      spyOn(component, 'onSubmit');
      elSubmit.click();
      expect(component.onSubmit).toHaveBeenCalledTimes(1);
    });

    it('should have a cancel button', () => {
      const elCancel = fixture.nativeElement.querySelector('button[type=button].btn.cancel');
      expect(elCancel).toBeTruthy();
      expect(elCancel.textContent).toContain('COMMON.UNDO');
      spyOn(component, 'onCancel');
      elCancel.click();
      expect(component.onCancel).toHaveBeenCalledTimes(1);
    });

  });

});
