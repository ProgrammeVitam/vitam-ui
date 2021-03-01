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

import { ɵisObservable as isObservable, ɵisPromise as isPromise } from '@angular/core';
import { fakeAsync, tick } from '@angular/core/testing';
import { FormControl } from '@angular/forms';
import { from, Observable,  of } from 'rxjs';

import { TenantFormValidators } from './tenant-form.validators';

function toObservable(r: any): Observable<any> {
  const obs = isPromise(r) ? from(r) : r;
  if (!(isObservable(obs))) {
    throw new Error(`Expected validator to return Promise or Observable.`);
  }

  return obs;
}

describe('Tenant Form Validators', () => {

  it('should return null', fakeAsync(() => {
    const tenantServiceSpy = jasmine.createSpyObj('TenantService', ['exists']);
    tenantServiceSpy.exists.and.returnValue(of(false));
    const tenantFormValidators = new TenantFormValidators(tenantServiceSpy);
    toObservable(tenantFormValidators.uniqueName()(new FormControl('name'))).subscribe((result) => {
      expect(result).toBeNull();
    });
    tick(400);
    expect(tenantServiceSpy.exists).toHaveBeenCalledWith('name');
  }));

  it('should return { uniqueName: true }', fakeAsync(() => {
    const tenantServiceSpy = jasmine.createSpyObj('TenantService', ['exists']);
    tenantServiceSpy.exists.and.returnValue(of(true));
    const tenantFormValidators = new TenantFormValidators(tenantServiceSpy);
    toObservable(tenantFormValidators.uniqueName()(new FormControl('name'))).subscribe((result) => {
      expect(result).toEqual({ uniqueName: true });
    });
    tick(400);
    expect(tenantServiceSpy.exists).toHaveBeenCalledWith('name');
  }));

  it('should not call the service', fakeAsync(() => {
    const tenantServiceSpy = jasmine.createSpyObj('TenantService', ['exists']);
    tenantServiceSpy.exists.and.returnValue(of(true));
    const tenantFormValidators = new TenantFormValidators(tenantServiceSpy);
    toObservable(tenantFormValidators.uniqueName('name')(new FormControl('name'))).subscribe((result) => {
      expect(result).toEqual(null);
    });
    tick(400);
    expect(tenantServiceSpy.exists).not.toHaveBeenCalled();
  }));

  it('should call the service', fakeAsync(() => {
    const tenantServiceSpy = jasmine.createSpyObj('TenantService', ['exists']);
    tenantServiceSpy.exists.and.returnValue(of(true));
    const tenantFormValidators = new TenantFormValidators(tenantServiceSpy);
    toObservable(tenantFormValidators.uniqueName('tenantName')(new FormControl('name'))).subscribe((result) => {
      expect(result).toEqual({ uniqueName: true });
    });
    tick(400);
    expect(tenantServiceSpy.exists).toHaveBeenCalledWith('name');
  }));

});
