import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ManagementContractRetentionPolicyTabComponent } from './management-contract-retention-policy-tab.component';

describe('ManagementContractRetentionPolicyTabComponent', () => {
  let component: ManagementContractRetentionPolicyTabComponent;
  let fixture: ComponentFixture<ManagementContractRetentionPolicyTabComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ManagementContractRetentionPolicyTabComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ManagementContractRetentionPolicyTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
