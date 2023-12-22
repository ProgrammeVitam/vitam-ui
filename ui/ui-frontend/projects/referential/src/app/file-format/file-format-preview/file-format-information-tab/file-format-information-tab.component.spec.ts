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
import {ComponentFixture, fakeAsync, TestBed, tick, waitForAsync} from '@angular/core/testing';

import {NO_ERRORS_SCHEMA} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {ActivatedRoute} from '@angular/router';
import {FileFormat} from 'projects/vitamui-library/src/public-api';
import {of} from 'rxjs';
import {WINDOW_LOCATION} from 'ui-frontend-common';
import {FileFormatService} from '../../file-format.service';
import {FileFormatInformationTabComponent} from './file-format-information-tab.component';
import {TranslateModule} from '@ngx-translate/core';
import {VitamUICommonTestModule} from 'ui-frontend-common/testing';

describe('FileFormatInformationTabComponent', () => {
  let component: FileFormatInformationTabComponent;
  let fixture: ComponentFixture<FileFormatInformationTabComponent>;

  const fileFormatServiceMock = {
    // tslint:disable-next-line:variable-name
    patch: (_data: any) => of(null),
  };

  const fileFormatValue = {
    puid: 'EXTERNAL_puid',
    name: 'Name',
    mimeType: 'application/puid',
    version: '1.0',
    versionPronom: '3.0',
    extensions: ['.puid'],
  };

  const previousValue: FileFormat = {
    id: 'vitam_id',
    documentVersion: 0,
    version: '1.0',
    versionPronom: '3.0',
    puid: 'EXTERNAL_puid',
    name: 'Name',
    description: 'Format de Fichier',
    mimeType: 'application/puid',
    hasPriorityOverFileFormatIDs: [],
    group: 'test',
    alert: false,
    comment: 'No Comment',
    extensions: ['.puid'],
    createdDate: '20/02/2020',
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [TranslateModule.forRoot(), ReactiveFormsModule, VitamUICommonTestModule],
      declarations: [FileFormatInformationTabComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: { params: of({ tenantIdentifier: 1 }), data: of({ appId: 'MANAGEMENT_CONTRACT_APP' }) },
        },
        { provide: WINDOW_LOCATION, useValue: window.location },
        { provide: FileFormatService, useValue: fileFormatServiceMock },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FileFormatInformationTabComponent);
    component = fixture.componentInstance;
    component.form.setValue(fileFormatValue);
    component.previousValue = (): FileFormat => previousValue;
    fixture.detectChanges();
  });

  it('should create', fakeAsync(() => {
    tick();
    expect(component).toBeTruthy();
  }));
});
