import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ManagementContractCreateComponent } from './management-contract-create.component';

describe('ManagementContractCreateComponent', () => {
  let component: ManagementContractCreateComponent;
  let fixture: ComponentFixture<ManagementContractCreateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ManagementContractCreateComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ManagementContractCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
