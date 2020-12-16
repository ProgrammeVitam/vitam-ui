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
// import { of } from 'rxjs';

/* tslint:disable:no-magic-numbers */
import { OverlayContainer, OverlayModule } from '@angular/cdk/overlay';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { Component, ViewChild } from '@angular/core';
import { ComponentFixture, inject, TestBed, waitForAsync } from '@angular/core/testing';
import { FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { TranslateModule } from '@ngx-translate/core';

// import { FlowValidators } from '../../../flow/flow.validators';
import { WINDOW_LOCATION } from '../../../injection-tokens';
import { EmailsInputModule } from '../emails-input/emails-input.module';
import { MultipleEmailInputComponent } from './multiple-email-input.component';

@Component({
  template: `
    <vitamui-common-multiple-email-input [(ngModel)]="value" [label]="label"></vitamui-common-multiple-email-input>
  `
})
class TesthostComponent {
  value: string[];
  defaultValue: string;
  label = 'Test label';

  @ViewChild(MultipleEmailInputComponent) component: MultipleEmailInputComponent;
}

describe('MultipleEmailInputComponent', () => {
  let testhost: TesthostComponent;
  let fixture: ComponentFixture<TesthostComponent>;
  let overlayContainerElement: HTMLElement;

  beforeEach(waitForAsync(() => {

    // const flowValidatorsSpy = jasmine.createSpyObj(
    //   'FlowValidators',
    //   { nameExists: () => of(null), uniqueName: of(null)}
    // );

    TestBed.configureTestingModule({
      imports: [
        OverlayModule,
        FormsModule,
        ReactiveFormsModule,
        MatProgressSpinnerModule,
        EmailsInputModule,
        BrowserAnimationsModule,
        HttpClientTestingModule,
        TranslateModule.forRoot(),
      ],
      declarations: [
        TesthostComponent,
        MultipleEmailInputComponent,
      ],
      providers: [
        { provide: WINDOW_LOCATION, useValue: {} },
        // { provide: FlowValidators, useValue: flowValidatorsSpy },
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

    it('should display the label', () => {
      const elLabel = fixture.nativeElement.querySelector('label');
      expect(elLabel.textContent).toContain('Test label');
    });

    it('should display the list of domains', waitForAsync(() => {
      testhost.value = ['test1.com', 'test2.com', 'test3.com'];
      fixture.detectChanges();
      fixture.whenStable().then(() => {
        fixture.detectChanges();
        const elDomains = fixture.nativeElement.querySelectorAll(
          '.editable-field .editable-field-content .editable-field-text-content > div'
        );
        expect(elDomains.length).toBe(3);
        expect(elDomains[0].textContent).toContain(testhost.value[0]);
        expect(elDomains[1].textContent).toContain(testhost.value[1]);
        expect(elDomains[2].textContent).toContain(testhost.value[2]);
      });
    }));

    it('should display "(par défaut)" next to the selected domain', waitForAsync(() => {
      testhost.value = ['test1.com', 'test2.com', 'test3.com', 'test4.com'];
      testhost.defaultValue = testhost.value[1];
      fixture.detectChanges();
      fixture.whenStable().then(() => {
        fixture.detectChanges();
        const elDomains = fixture.nativeElement.querySelectorAll(
          '.editable-field .editable-field-content .editable-field-text-content > div'
        );
        expect(elDomains.length).toBe(4);
      });
    }));

    it('should have a vitamui-common-emails-input', () => {
      const elDomainInput = fixture.nativeElement.querySelector('.editable-field-control > vitamui-common-emails-input');
      expect(elDomainInput).toBeTruthy();
      expect(elDomainInput.attributes.placeholder.textContent).toBe('nom@domaine.xyz');
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

    it('should have a spinner', () => {
      spyOnProperty(testhost.component, 'showSpinner').and.returnValue(true);
      fixture.detectChanges();
      const elSpinner = fixture.nativeElement.querySelector('.editable-field mat-spinner');
      expect(elSpinner).toBeTruthy();
    });

    it('should hide the spinner', () => {
      spyOnProperty(testhost.component, 'showSpinner').and.returnValue(false);
      fixture.detectChanges();
      const elSpinner = fixture.nativeElement.querySelector('.editable-field mat-spinner');
      expect(elSpinner).toBeFalsy();
    });

  });

  describe('Class', () => {

    it('should set the control value', waitForAsync(() => {
      testhost.value = ['test1.com', 'test2.com'];
      fixture.detectChanges();
      fixture.whenStable().then(() => {
        expect(testhost.component.control.value).toEqual(testhost.value);
      });
    }));

    describe('canConfirm', () => {
      it('should return true when the edit mode is active, the value has changed and is valid', () => {
        testhost.component.editMode = true;
        testhost.component.control.setValue(['test1.com', 'test2.com']);
        testhost.component.control.markAsDirty();
        expect(testhost.component.canConfirm).toBe(true);
      });

      it('should return false if editMode is not active', () => {
        testhost.component.editMode = false;
        testhost.component.control.setValue(['test1.com', 'test2.com']);
        testhost.component.control.markAsDirty();
        expect(testhost.component.canConfirm).toBe(false);
      });

      it('should return false if control is pristine', () => {
        testhost.component.enterEditMode();
        testhost.component.control.setValue(['test1.com', 'test2.com']);
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
        testhost.component.control.setValue(['test1.com', 'test2.com']);
        testhost.component.control.markAsDirty();
        testhost.component.control.markAsPending();
        expect(testhost.component.canConfirm).toBe(false);
      });
    });

    it('should emit a new value', waitForAsync(() => {
      testhost.value = ['test1.com', 'test2.com'];
      fixture.detectChanges();
      fixture.whenStable().then(() => {
        fixture.detectChanges();
        expect(testhost.component.control.value).toEqual(testhost.value);
        testhost.component.enterEditMode();
        testhost.component.control.setValue(['test1.com', 'test3.com', 'test4.com']);
        testhost.component.control.markAsDirty();
        fixture.detectChanges();
        expect(testhost.value).toEqual(['test1.com', 'test2.com']);
        testhost.component.confirm();
        expect(testhost.value).toEqual(['test1.com', 'test3.com', 'test4.com']);
      });
    }));

    it('should reverse the changes', waitForAsync(() => {
      testhost.value = ['test1.com', 'test2.com'];
      fixture.detectChanges();
      fixture.whenStable().then(() => {
        expect(testhost.component.control.value).toEqual(testhost.value);
        testhost.component.enterEditMode();
        testhost.component.control.setValue(['test1.com', 'test3.com', 'test4.com']);
        testhost.component.control.markAsDirty();
        fixture.detectChanges();
        expect(testhost.value).toEqual(['test1.com', 'test2.com']);
        testhost.component.cancel();
        fixture.detectChanges();
        expect(testhost.value).toEqual(['test1.com', 'test2.com']);
        expect(testhost.component.control.value).toEqual(['test1.com', 'test2.com']);
      });
    }));
  });
});
