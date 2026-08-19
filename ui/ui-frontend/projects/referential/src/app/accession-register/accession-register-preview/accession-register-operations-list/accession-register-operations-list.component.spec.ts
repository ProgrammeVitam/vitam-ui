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
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatTreeModule } from '@angular/material/tree';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RegisterValueEventModel } from 'vitamui-library';
import { InjectorModule, LoggerModule, RegisterValueEventType } from 'vitamui-library';
import { AccessionRegisterOperationsListComponent } from './accession-register-operations-list.component';

export class AccessionRegisterFixtures {
  public static newOperations(name: RegisterValueEventType): RegisterValueEventModel {
    const date = new Date();
    date.setHours(Math.floor(Math.random() * (12 + 1)));
    return {
      Opc: name,
      OpType: name,
      Gots: Math.floor(Math.random() * (10 + 1)),
      Units: Math.floor(Math.random() * (10 + 1)),
      Objects: Math.floor(Math.random() * (10 + 1)),
      ObjSize: Math.floor(Math.random() * (100 + 1)),
      CreationDate: date.toISOString(),
    };
  }
}

describe('AccessionRegisterOperationsListComponent', () => {
  let component: AccessionRegisterOperationsListComponent;
  let fixture: ComponentFixture<AccessionRegisterOperationsListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        MatMenuModule,
        MatTreeModule,
        MatProgressSpinnerModule,
        MatSidenavModule,
        InjectorModule,
        LoggerModule.forRoot(),
        MatIconModule,
        BrowserAnimationsModule,
        AccessionRegisterOperationsListComponent,
      ],
      providers: [],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AccessionRegisterOperationsListComponent);
    component = fixture.componentInstance;
    component.operations = [
      AccessionRegisterFixtures.newOperations(RegisterValueEventType.PRESERVATION),
      AccessionRegisterFixtures.newOperations(RegisterValueEventType.INGEST),
      AccessionRegisterFixtures.newOperations(RegisterValueEventType.PRESERVATION),
      AccessionRegisterFixtures.newOperations(RegisterValueEventType.INGEST),
      AccessionRegisterFixtures.newOperations(RegisterValueEventType.TRANSFER_REPLY),
      AccessionRegisterFixtures.newOperations(RegisterValueEventType.ELIMINATION),
    ];
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
