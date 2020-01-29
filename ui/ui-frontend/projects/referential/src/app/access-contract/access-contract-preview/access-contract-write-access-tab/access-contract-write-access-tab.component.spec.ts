import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AccessContractWriteAccessTabComponent } from './access-contract-write-access-tab.component';

describe('AccessContractUsageAndServicesTabComponent', () => {
  let component: AccessContractWriteAccessTabComponent;
  let fixture: ComponentFixture<AccessContractWriteAccessTabComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AccessContractWriteAccessTabComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AccessContractWriteAccessTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
