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
import { Validators } from '@angular/forms';

import { EditableFieldComponent } from './editable-field.component';

describe('EditableFieldComponent', () => {
  let component: EditableFieldComponent;

  beforeEach(() => {
    component = new EditableFieldComponent({ nativeElement: document.createElement('dummy') });
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('showSpinner', () => {
    it('should return true when the control is pending and dirty', () => {
      component.control.markAsPending();
      component.control.markAsDirty();
      expect(component.showSpinner).toBe(true);
    });

    it('should return false when the control is pristine', () => {
      component.control.markAsPending();
      expect(component.showSpinner).toBe(false);
    });

    it('should return false when the control is not pending', () => {
      component.control.markAsDirty();
      expect(component.showSpinner).toBe(false);
    });
  });

  describe('canConfirm', () => {
    it('should return true when editMode is active and the control is dirty, valid and not pending', () => {
      component.enterEditMode();
      component.control.setValue('valid');
      component.control.markAsDirty();
      expect(component.canConfirm).toBe(true);
    });

    it('should return false if editMode is not active', () => {
      component.editMode = false;
      component.control.setValue('valid');
      component.control.markAsDirty();
      expect(component.canConfirm).toBe(false);
    });

    it('should return false if control is pristine', () => {
      component.enterEditMode();
      component.control.setValue('valid');
      expect(component.canConfirm).toBe(false);
    });

    it('should return false if control is invalid', () => {
      component.enterEditMode();
      component.control.setValidators(Validators.required);
      component.control.setValue(null);
      component.control.markAsDirty();
      expect(component.canConfirm).toBe(false);
    });

    it('should return false if control is pending', () => {
      component.enterEditMode();
      component.control.setValue('valid');
      component.control.markAsDirty();
      component.control.markAsPending();
      expect(component.canConfirm).toBe(false);
    });
  });

  it('should enter the editMode', () => {
    expect(component.editMode).toBe(false);
    component.enterEditMode();
    expect(component.editMode).toBe(true);
  });

  describe('confirm', () => {
    it('should leave the edit mode, emit the new value and reset the control', () => {
      spyOn(component, 'onChange');
      component.enterEditMode();
      component.control.setValue('New value');
      component.control.markAsDirty();
      component.confirm();
      expect(component.editMode).toBe(false);
      expect(component.onChange).toHaveBeenCalledWith('New value');
      expect(component.control.pristine).toBeTruthy();
      expect(component.control.value).toBe('New value');
    });

    it('should not do anything', () => {
      spyOn(component, 'onChange');
      component.enterEditMode();
      component.control.setValue('New value');
      component.control.markAsDirty();
      component.control.markAsPending();
      component.confirm();
      expect(component.editMode).toBe(true);
      expect(component.onChange).not.toHaveBeenCalledWith('New value');
      expect(component.control.pristine).toBeFalsy();
      expect(component.control.value).toBe('New value');
    });
  });

  describe('cancel', () => {
    it('should leave the edit mode and reset the value', () => {
      spyOn(component, 'onChange');
      component.writeValue('Original value');
      component.enterEditMode();
      component.control.setValue('New value');
      component.control.markAsDirty();
      component.cancel();
      expect(component.editMode).toBe(false);
      expect(component.onChange).not.toHaveBeenCalledWith('New value');
      expect(component.control.pristine).toBeTruthy();
      expect(component.control.value).toBe('Original value');
    });

    it('should not reset the value if the edit mode is not active', () => {
      component.editMode = false;
      component.writeValue('Original value');
      component.control.setValue('New value');
      component.cancel();
      expect(component.control.value).toBe('New value');
    });
  });

  it('should call confirm', () => {
    spyOn(component, 'confirm');
    component.onEnter(new KeyboardEvent('Enter'));
    expect(component.confirm).toHaveBeenCalled();
  });

  it('should call cancel', () => {
    spyOn(component, 'cancel');
    component.onEscape(new KeyboardEvent('Escape'));
    expect(component.cancel).toHaveBeenCalled();
  });

  it('should set disabled', () => {
    expect(component.disabled).toBeFalsy();
    component.setDisabledState(true);
    expect(component.disabled).toBeTruthy();
    component.setDisabledState(false);
    expect(component.disabled).toBeFalsy();
  });

});
