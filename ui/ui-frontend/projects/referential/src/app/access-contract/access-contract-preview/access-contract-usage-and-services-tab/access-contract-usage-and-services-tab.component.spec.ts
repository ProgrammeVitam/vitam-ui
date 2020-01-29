import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AccessContractUsageAndServicesTabComponent } from './access-contract-usage-and-services-tab.component';

describe('AccessContractUsageAndServicesTabComponent', () => {
  let component: AccessContractUsageAndServicesTabComponent;
  let fixture: ComponentFixture<AccessContractUsageAndServicesTabComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AccessContractUsageAndServicesTabComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AccessContractUsageAndServicesTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
