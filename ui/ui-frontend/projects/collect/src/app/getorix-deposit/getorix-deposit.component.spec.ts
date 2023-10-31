/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { InjectorModule, LoggerModule } from 'ui-frontend-common';
import { GetorixDepositComponent } from './getorix-deposit.component';

describe('GetorixDepositComponent', () => {
  let component: GetorixDepositComponent;
  let fixture: ComponentFixture<GetorixDepositComponent>;

  const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [GetorixDepositComponent],
      imports: [HttpClientTestingModule, TranslateModule.forRoot(), InjectorModule, LoggerModule.forRoot()],
      providers: [
        {
          provide: Router,
          useValue: routerSpy,
        },
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(GetorixDepositComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('component should be created', () => {
    expect(component).toBeTruthy();
  });

  it('should redirect to /create', () => {
    const router = TestBed.inject(Router);
    component.startDepositCreation();
    expect(router.navigate).toHaveBeenCalledWith([undefined, 'create']);
  });

  describe('DOM', () => {
    it('should have 1 column-1 ', () => {
      // When
      const nativeElement = fixture.nativeElement;
      const elementColumn = nativeElement.querySelectorAll('.col-1');

      // Then
      expect(elementColumn.length).toBe(1);
    });
    it('should have 2 buttons ', () => {
      // When
      const nativeElement = fixture.nativeElement;
      const elementBtn = nativeElement.querySelectorAll('button');

      // Then
      expect(elementBtn.length).toBe(2);
    });
    it('should have 2 rows ', () => {
      // When
      const nativeElement = fixture.nativeElement;
      const elementRow = nativeElement.querySelectorAll('.row');

      // Then
      expect(elementRow.length).toBe(2);
    });
    it('should have 1 column-11 ', () => {
      // When
      const nativeElement = fixture.nativeElement;
      const elementColumn = nativeElement.querySelectorAll('.col-11');

      // Then
      expect(elementColumn.length).toBe(1);
    });
  });
});
