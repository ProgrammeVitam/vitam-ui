import {NO_ERRORS_SCHEMA} from '@angular/core';
import {ComponentFixture,TestBed,waitForAsync} from '@angular/core/testing';
import {FormBuilder,ReactiveFormsModule} from '@angular/forms';
import {MatSelectModule} from '@angular/material/select';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {of} from 'rxjs';
import {VitamUICommonTestModule} from 'ui-frontend-common/testing';
import {ManagementContractService} from '../../management-contract.service';
import {ManagementContractStorageTabComponent} from './management-contract-storage-tab.component';


describe('ManagementContractInformationTabComponent',() => {
  let component: ManagementContractStorageTabComponent;
  let fixture: ComponentFixture<ManagementContractStorageTabComponent>;

  const managementContractServiceMock={
    // tslint:disable-next-line:variable-name
    patch: (_data: any) => of(null)
  };

  const managementContractValue: { identifier: string; deactivationDate: string; lastUpdate: string; name: string; description: string; id: string; storage: undefined; versionRetentionPolicy: undefined; creationDate: string; activationDate: string; version: number; tenant: number; status: string }={
    id: 'id',
    tenant: 0,
    version: 1,
    creationDate: '01-01-2020',
    lastUpdate: '01-01-2020',
    identifier: 'SP-000001',
    description: 'Mon Ontologie',
    name: '',
    status: '',
    activationDate: '',
    deactivationDate: '',
    storage: undefined,
    versionRetentionPolicy: undefined
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        ReactiveFormsModule,
        VitamUICommonTestModule,
        NoopAnimationsModule,
        MatSelectModule
      ],
      declarations: [ManagementContractStorageTabComponent],
      providers: [
        FormBuilder,
        {provide: ManagementContractService,useValue: managementContractServiceMock}
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture=TestBed.createComponent(ManagementContractStorageTabComponent);
    component=fixture.componentInstance;
    component.inputManagementContract=managementContractValue;
    fixture.detectChanges();
  });

  it('should create',() => {
    expect(component).toBeTruthy();
  });
});
