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
import { Component, Input } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialogModule } from '@angular/material/dialog';
import { MatMenuModule } from '@angular/material/menu';
import { MatSidenavModule } from '@angular/material/sidenav';
import { ActivatedRoute } from '@angular/router';
import { of, Subject } from 'rxjs';
import { InjectorModule, LoggerModule } from 'vitamui-library';
import { VitamUICommonTestModule } from 'vitamui-library/testing';
import { ExternalParamProfileComponent } from './external-param-profile.component';
import { ExternalParamProfileService } from './external-param-profile.service';
import { SharedService } from './shared.service';
import { ExternalParamProfileDetailComponent } from './external-param-profile-detail/external-param-profile-detail.component';
import { ExternalParamProfileListComponent } from './external-param-profile-list/external-param-profile-list.component';

@Component({
  selector: 'app-external-param-profile-detail',
  template: '',
})
class ExternalParamProfileDetailStubComponent {
  @Input() externalParamProfile: any;
  @Input() readOnly: boolean;
  @Input() tenantIdentifier: string;
}

@Component({
  selector: 'app-external-param-profile-list',
  template: '',
})
class ExternalParamProfileListStubComponent {
  @Input() searchText: string;
}

describe('ExternalParamProfileComponent', () => {
  let component: ExternalParamProfileComponent;
  let fixture: ComponentFixture<ExternalParamProfileComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        VitamUICommonTestModule,
        InjectorModule,
        LoggerModule.forRoot(),
        MatSidenavModule,
        MatDialogModule,
        MatMenuModule,
        ExternalParamProfileComponent,
        ExternalParamProfileDetailStubComponent,
        ExternalParamProfileListStubComponent,
      ],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            data: of({ appId: 'EXTERNAL_PARAM_PROFILE_APP' }),
            params: of({}),
            snapshot: { data: { appId: 'EXTERNAL_PARAM_PROFILE_APP' } },
          },
        },
        {
          provide: ExternalParamProfileService,
          useValue: { updated: new Subject(), getOne: () => of(null), search: () => of([]), loadMore: () => of([]) },
        },
        { provide: SharedService, useValue: { getReadOnly: () => of(false) } },
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
    })
      .overrideComponent(ExternalParamProfileComponent, {
        remove: {
          imports: [ExternalParamProfileDetailComponent, ExternalParamProfileListComponent],
        },
        add: {
          imports: [ExternalParamProfileDetailStubComponent, ExternalParamProfileListStubComponent],
        },
      })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ExternalParamProfileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
