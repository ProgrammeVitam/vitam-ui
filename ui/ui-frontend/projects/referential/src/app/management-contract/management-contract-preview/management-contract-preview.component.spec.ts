import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ManagementContractPreviewComponent } from './management-contract-preview.component';

describe('ManagementContractPreviewComponent', () => {
  let component: ManagementContractPreviewComponent;
  let fixture: ComponentFixture<ManagementContractPreviewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ManagementContractPreviewComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ManagementContractPreviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
