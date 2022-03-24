import {NO_ERRORS_SCHEMA} from '@angular/core';
import {ComponentFixture,TestBed,waitForAsync} from '@angular/core/testing';
import {FormBuilder,ReactiveFormsModule} from '@angular/forms';
import {MatSelectModule} from '@angular/material/select';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {ManagementContract} from 'projects/vitamui-library/src/public-api';
import {of} from 'rxjs';
import {VitamUICommonTestModule} from 'ui-frontend-common/testing';
import {ManagementContractService} from '../../management-contract.service';
import {ManagementContractInformationTabComponent} from './management-contract-information-tab.component';


describe('ManagementContractInformationTabComponent',() => {
  let component: ManagementContractInformationTabComponent;
  let fixture: ComponentFixture<ManagementContractInformationTabComponent>;

  const managementContractServiceMock={
    // tslint:disable-next-line:variable-name
    patch: (_data: any) => of(null)
  };

  const managementContractValue: ManagementContract={
    id: 'id',
    tenant: 0,
    version: 1,
    creationDate: '01-01-2020',
    lastUpdate: '01-01-2020',


// TODO
    identifier: 'SP-000001',


    description: 'Mon Ontologie',
    name: '',
    status: '',
    activationDate: '',
    deactivationDate: '',
    storage: undefined
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        ReactiveFormsModule,
        VitamUICommonTestModule,
        NoopAnimationsModule,
        MatSelectModule
      ],
      declarations: [ManagementContractInformationTabComponent],
      providers: [
        FormBuilder,
        {provide: ManagementContractService,useValue: managementContractServiceMock}
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture=TestBed.createComponent(ManagementContractInformationTabComponent);
    component=fixture.componentInstance;
    component.inputManagementContract=managementContractValue;
    fixture.detectChanges();
  });

  it('should create',() => {
    expect(component).toBeTruthy();
  });
});
