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
/* eslint-disable no-magic-numbers */
import { fakeAsync, tick } from '@angular/core/testing';
import { FormControl } from '@angular/forms';
import { from, of } from 'rxjs';
import { SecurityProfileCreateValidators } from './security-profile-create.validators';

describe('SecurityProfile Create Validators', () => {
  describe('uniqueCode', () => {
    it('should return null', fakeAsync(() => {
      const customerServiceSpy = jasmine.createSpyObj('SecurityProfileService', ['existsProperties']);
      customerServiceSpy.existsProperties.and.returnValue(of(false));
      const customerCreateValidators = new SecurityProfileCreateValidators(customerServiceSpy);
      from(customerCreateValidators.uniqueName()(new FormControl('123456'))).subscribe((result) => {
        expect(result).toBeNull();
      });
      tick(400);
      expect(customerServiceSpy.existsProperties).toHaveBeenCalledWith({ name: '123456' });
    }));

    it('should return { uniqueCode: true }', fakeAsync(() => {
      const customerServiceSpy = jasmine.createSpyObj('SecurityProfileService', ['existsProperties']);
      customerServiceSpy.existsProperties.and.returnValue(of(true));
      const customerCreateValidators = new SecurityProfileCreateValidators(customerServiceSpy);
      from(customerCreateValidators.uniqueName()(new FormControl('123456'))).subscribe((result) => {
        expect(result).toEqual({ nameExists: true });
      });
      tick(400);
      expect(customerServiceSpy.existsProperties).toHaveBeenCalledWith({ name: '123456' });
    }));

    it('should not call the service', fakeAsync(() => {
      const customerServiceSpy = jasmine.createSpyObj('SecurityProfileService', ['existsProperties']);
      customerServiceSpy.existsProperties.and.returnValue(of(true));
      const customerCreateValidators = new SecurityProfileCreateValidators(customerServiceSpy);
      from(customerCreateValidators.uniqueName('123456')(new FormControl('123456'))).subscribe((result) => {
        expect(result).toEqual(null);
      });
      tick(400);
      expect(customerServiceSpy.existsProperties).not.toHaveBeenCalled();
    }));

    it('should call the service', fakeAsync(() => {
      const customerServiceSpy = jasmine.createSpyObj('SecurityProfileService', ['existsProperties']);
      customerServiceSpy.existsProperties.and.returnValue(of(true));
      const customerCreateValidators = new SecurityProfileCreateValidators(customerServiceSpy);
      from(customerCreateValidators.uniqueName('123456')(new FormControl('111111'))).subscribe((result) => {
        expect(result).toEqual({ nameExists: true });
      });
      tick(400);
      expect(customerServiceSpy.existsProperties).toHaveBeenCalledWith({ name: '111111' });
    }));
  });
});
