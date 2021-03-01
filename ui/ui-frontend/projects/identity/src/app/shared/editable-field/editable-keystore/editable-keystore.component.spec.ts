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


import { OverlayContainer, OverlayModule } from '@angular/cdk/overlay';
import { Component, ViewChild } from '@angular/core';
import { ComponentFixture, inject, TestBed, waitForAsync } from '@angular/core/testing';
import { ReactiveFormsModule, Validators } from '@angular/forms';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of, throwError as observableThrowError } from 'rxjs';

import { IdentityProvider, newFile } from 'ui-frontend-common';
import { input, VitamUICommonTestModule } from 'ui-frontend-common/testing';
import { IdentityProviderService } from '../../../customer/customer-preview/sso-tab/identity-provider.service';
import { EditableKeystoreComponent } from './editable-keystore.component';


@Component({
  template: `
    <app-editable-keystore [identityProvider]="identityProvider" [disabled]="disabled"></app-editable-keystore>
  `
})
class TesthostComponent {
  identityProvider: IdentityProvider = {
    id: '1',
    customerId: '2',
    name: 'testIDP',
    technicalName: 'Test IDP',
    internal: false,
    keystorePassword: null,
    keystore: null,
    idpMetadata: null,
    patterns: ['test1.com', 'test2.com'],
    enabled: true,
    readonly : false
  };
  disabled: boolean;
  @ViewChild(EditableKeystoreComponent, { static: false }) component: EditableKeystoreComponent;
}

describe('EditableKeystoreComponent', () => {
  let testhost: TesthostComponent;
  let fixture: ComponentFixture<TesthostComponent>;
  let overlayContainerElement: HTMLElement;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        ReactiveFormsModule,
        OverlayModule,
        VitamUICommonTestModule,
        NoopAnimationsModule,
      ],
      declarations: [
        TesthostComponent,
        EditableKeystoreComponent,
      ],
      providers: [
        { provide: IdentityProviderService, useValue: { updateKeystore: () => of(null) } },
      ]
    })
    .compileComponents();

    inject([OverlayContainer], (oc: OverlayContainer) => {
      overlayContainerElement = oc.getContainerElement();
    })();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TesthostComponent);
    testhost = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(testhost).toBeTruthy();
  });

  describe('DOM', () => {

    it('should call enterEditMode() on click', () => {
      spyOn(testhost.component, 'enterEditMode');
      const element = fixture.nativeElement.querySelector('.editable-field');
      element.click();
      expect(testhost.component.enterEditMode).toHaveBeenCalled();
    });

    it('should open then close the action buttons', () => {
      testhost.component.enterEditMode();
      fixture.detectChanges();
      expect(overlayContainerElement.querySelector('.editable-field-actions')).toBeTruthy();
      testhost.component.cancel();
      fixture.detectChanges();
      expect(overlayContainerElement.querySelector('.editable-field-actions')).toBeFalsy();
    });

    it('should have a confirm button', () => {
      spyOn(testhost.component, 'confirm');
      testhost.component.enterEditMode();
      testhost.component.file = newFile([''], 'test.jks');
      testhost.component.control.setValue('password1234');
      testhost.component.control.markAsDirty();
      fixture.detectChanges();
      const elButton = overlayContainerElement.querySelector('.editable-field-actions button.editable-field-confirm') as HTMLButtonElement;
      expect(elButton).toBeTruthy();
      elButton.click();
      expect(testhost.component.confirm).toHaveBeenCalled();
    });

    it('should have a cancel button', () => {
      spyOn(testhost.component, 'cancel');
      testhost.component.enterEditMode();
      fixture.detectChanges();
      const elButton = overlayContainerElement.querySelector('.editable-field-actions button.editable-field-cancel') as HTMLButtonElement;
      expect(elButton).toBeTruthy();
      elButton.click();
      expect(testhost.component.cancel).toHaveBeenCalled();
    });

    it('should have an input file', () => {
      const elInput = fixture.nativeElement.querySelector('input[type=file]');
      expect(elInput).toBeTruthy();
    });

    it('should have a password input', () => {
      const elInput = fixture.nativeElement.querySelector('input[type=password]');
      expect(elInput).toBeTruthy();
      input(elInput, 'password1234');
      expect(testhost.component.control.value).toBe('password1234');
    });

    it('should display the file name', waitForAsync(() => {
      testhost.component.file = newFile([''], 'test.jks');
      testhost.component.editMode = true;
      fixture.detectChanges();
      const elFileName = fixture.nativeElement.querySelector('.vitamui-input-file-filename');
      expect(elFileName).toBeTruthy();
      expect(elFileName.textContent).toContain('test.jks');
    }));

    it('should display the errors', () => {
      testhost.component.control.setErrors({ badPassword: true });
      fixture.detectChanges();
      const elError = fixture.nativeElement.querySelector('vitamui-common-input-error');
      expect(elError).toBeTruthy();
      expect(elError.textContent).toContain('Mot de passe incorrect');
    });

  });

  describe('Class', () => {

    describe('canConfirm', () => {
      it('should return true when the edit mode is active, the file and password are set', () => {
        testhost.component.editMode = true;
        testhost.component.file = newFile([''], 'test-file.txt');
        testhost.component.control.setValue('password');
        expect(testhost.component.canConfirm).toBe(true);
      });

      it('should return false if editMode is not active', () => {
        testhost.component.editMode = false;
        testhost.component.control.setValue(newFile([''], 'test-file.txt'));
        testhost.component.control.markAsDirty();
        expect(testhost.component.canConfirm).toBe(false);
      });

      it('should return false if control is pristine', () => {
        testhost.component.enterEditMode();
        testhost.component.control.setValue(newFile([''], 'test-file.txt'));
        expect(testhost.component.canConfirm).toBe(false);
      });

      it('should return false if control is invalid', () => {
        testhost.component.enterEditMode();
        testhost.component.control.setValidators(Validators.required);
        testhost.component.control.setValue(null);
        testhost.component.control.markAsDirty();
        expect(testhost.component.canConfirm).toBe(false);
      });

      it('should return false if control is pending', () => {
        testhost.component.enterEditMode();
        testhost.component.control.setValue(newFile([''], 'test-file.txt'));
        testhost.component.control.markAsDirty();
        testhost.component.control.markAsPending();
        expect(testhost.component.canConfirm).toBe(false);
      });
    });

    describe('setFile', () => {
      it('should set the file', () => {
        const expectedFile = newFile([''], 'test.jks');
        testhost.component.setFile({ item: () => expectedFile, length: 1 });
        expect(testhost.component.file).toBe(expectedFile);
      });
    });

    describe('confirm', () => {
      it('should call updateKeystore', () => {
        const idpService = TestBed.inject(IdentityProviderService);
        spyOn(idpService, 'updateKeystore').and.callThrough();
        testhost.component.editMode = true;
        const expectedFile = newFile([''], 'test.jks');
        testhost.component.file = expectedFile;
        testhost.component.control.setValue('password');
        testhost.component.confirm();
        expect(idpService.updateKeystore).toHaveBeenCalledWith(testhost.identityProvider.id, expectedFile, 'password');
        expect(testhost.component.editMode).toBe(false);
      });

      it('should not call updateKeystore', () => {
        const idpService = TestBed.inject(IdentityProviderService);
        spyOn(idpService, 'updateKeystore').and.callThrough();
        testhost.component.editMode = true;
        const expectedFile = newFile([''], 'test.jks');
        testhost.component.file = expectedFile;
        testhost.component.confirm();
        expect(idpService.updateKeystore).not.toHaveBeenCalled();
        testhost.component.file = null;
        testhost.component.control.setValue('password');
        testhost.component.confirm();
        expect(idpService.updateKeystore).not.toHaveBeenCalled();
      });

      it('should set the error', () => {
        const idpService = TestBed.inject(IdentityProviderService);
        spyOn(idpService, 'updateKeystore').and.returnValue(observableThrowError(null));
        testhost.component.editMode = true;
        const expectedFile = newFile([''], 'test.jks');
        testhost.component.file = expectedFile;
        testhost.component.control.setValue('password');
        testhost.component.confirm();
        expect(idpService.updateKeystore).toHaveBeenCalled();
        expect(testhost.component.control.errors).toEqual({ badPassword: true });
      });
    });

    describe('cancel', () => {
      it('should close the editMode', () => {
        testhost.component.editMode = true;
        testhost.component.cancel();
        expect(testhost.component.editMode).toBe(false);
      });

      it('should set the file to null', () => {
        testhost.component.editMode = true;
        testhost.component.file = newFile([''], 'test.jks');
        testhost.component.cancel();
        expect(testhost.component.file).toBeNull();
      });

      it('should reset the password', () => {
        testhost.component.editMode = true;
        testhost.component.control.setValue('password');
        testhost.component.cancel();
        expect(testhost.component.control.value).toBeNull();
      });
    });

  });
});
