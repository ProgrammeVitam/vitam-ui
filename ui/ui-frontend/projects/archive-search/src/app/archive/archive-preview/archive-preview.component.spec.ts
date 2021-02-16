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
 import { NO_ERRORS_SCHEMA } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
 
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatTreeModule } from '@angular/material/tree';


import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute } from '@angular/router';
 
import { RouterTestingModule } from '@angular/router/testing';
import { environment } from '../../../environments/environment.prod';
import { VitamUILibraryModule } from 'projects/vitamui-library/src/public-api';
import { of } from 'rxjs';
 
import { BASE_URL, InjectorModule, LoggerModule } from 'ui-frontend-common'; 
import { Unit } from '../models/unit.interface';
 
import { ArchivePreviewComponent } from './archive-preview.component';
import { TranslateModule } from '@ngx-translate/core';
 

 
describe('ArchivePreviewComponent', () => {
  let component: ArchivePreviewComponent;
  let fixture: ComponentFixture<ArchivePreviewComponent>;
 

 
  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        MatMenuModule,
        MatTreeModule,
        MatProgressSpinnerModule,
        MatSidenavModule,
        InjectorModule,
        LoggerModule.forRoot(),
        RouterTestingModule,
        VitamUILibraryModule,
        MatIconModule,
        BrowserAnimationsModule,
        TranslateModule.forRoot()
      ],
      declarations: [
        ArchivePreviewComponent
      ],
      providers: [
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: ActivatedRoute, useValue: { params: of({ tenantIdentifier: 1 }), data: of({ appId: 'ARCHIVE_SEARCH_MANAGEMENT_APP' }) } },
        { provide: environment, useValue: environment }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ArchivePreviewComponent);
    component = fixture.componentInstance;
    let archiveUnit: Unit = { '#allunitups': [], '#id': 'id', '#object': '', '#unitType': '', '#unitups': [], "#opi": '', 'Title_': {'fr':'Teste', 'en':'Test'}};
    component.archiveUnit = archiveUnit;
    fixture.detectChanges();
  });

  fit('should create', () => {
    expect(component).toBeTruthy();
  });

});



