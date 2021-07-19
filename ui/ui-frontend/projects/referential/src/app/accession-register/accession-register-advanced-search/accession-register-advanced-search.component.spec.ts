import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AccessionRegisterAdvancedSearchComponent } from './accession-register-advanced-search.component';
import { VitamUICommonTestModule } from 'ui-frontend-common/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { BASE_URL, ENVIRONMENT, InjectorModule, LoggerModule, RoleToggleModule, TableFilterModule } from 'ui-frontend-common';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatDialogModule } from '@angular/material/dialog';
import { MatMenuModule } from '@angular/material/menu';
import { AccessionRegisterBusiness } from '../accession-register.business';
import { of } from 'rxjs';
import { ReactiveFormsModule } from '@angular/forms';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { SharedModule } from '../../shared/shared.module';
import { VitamUILibraryModule } from '../../../../../vitamui-library/src/lib/vitamui-library.module';
import { AccessionRegisterRoutingModule } from '../accession-register-routing.module';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatSelectModule } from '@angular/material/select';
import { GroupAttributionModule } from '../../../../../identity/src/app/user/group-attribution/group-attribution.module';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTabsModule } from '@angular/material/tabs';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatCardModule } from '@angular/material/card';
import { MatPseudoCheckboxModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { environment } from '../../../../../archive-search/src/environments/environment.prod';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';

describe('AccessionRegisterAdvancedSearchComponent', () => {
  let component: AccessionRegisterAdvancedSearchComponent;
  let fixture: ComponentFixture<AccessionRegisterAdvancedSearchComponent>;
  const accessionRegisterBusiness = {
    getAccessionRegisterStatus: () => of([]),
    setOpenAdvancedSearchPanel: () => {},
    toggleOpenAdvancedSearchPanel: () => {},
    isOpenAdvancedSearchPanel: () => of(true),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        VitamUICommonTestModule,
        RouterTestingModule,
        InjectorModule,
        LoggerModule.forRoot(),
        NoopAnimationsModule,
        ReactiveFormsModule,
        MatMenuModule,
        MatSnackBarModule,
        MatDialogModule,
        MatSidenavModule,
        MatProgressSpinnerModule,
        SharedModule,
        TableFilterModule,
        VitamUILibraryModule,
        AccessionRegisterRoutingModule,
        MatButtonToggleModule,
        MatSelectModule,
        GroupAttributionModule,
        MatProgressBarModule,
        MatTabsModule,
        RoleToggleModule,
        MatCheckboxModule,
        MatCardModule,
        MatPseudoCheckboxModule,
        MatDatepickerModule,
      ],
      declarations: [AccessionRegisterAdvancedSearchComponent],
      providers: [
        { provide: AccessionRegisterBusiness, useValue: accessionRegisterBusiness },
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: ENVIRONMENT, useValue: environment },
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AccessionRegisterAdvancedSearchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
