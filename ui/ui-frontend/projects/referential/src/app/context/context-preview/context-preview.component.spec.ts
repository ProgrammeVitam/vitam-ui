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
import { MatDialog } from '@angular/material/dialog';

import { ContextService } from '../context.service';
import { ContextPreviewComponent } from './context-preview.component';
import { Context } from 'vitamui-library';
import { of } from 'rxjs';

describe('ContextPreviewComponent', () => {
  let component: ContextPreviewComponent;
  let fixture: ComponentFixture<ContextPreviewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ContextPreviewComponent],
      providers: [{ provide: MatDialog, useValue: {} }],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
    })
      .overrideProvider(ContextService, { useValue: { updated: of() } })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ContextPreviewComponent);
    component = fixture.componentInstance;
    component.context = {
      id: 'aegqaaaaaahbzl4naaovqamcuzgxyjyaaaaq',
      name: 'admin-context',
      identifier: 'CT-000001',
      status: 'ACTIVE',
      creationDate: '2022-08-16T10:57:52.168',
      lastUpdate: '2022-08-16T13:12:53.416',
      enableControl: 'false',
      securityProfile: 'admin-security-profile',
      permissions: [],
      activationDate: 'activationDate',
      deactivationDate: 'deactivationDate',
    } satisfies Context;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
