import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AccessContractNodesTabComponent } from './access-contract-nodes-tab.component';

describe('AccessContractNodesTabComponent', () => {
  let component: AccessContractNodesTabComponent;
  let fixture: ComponentFixture<AccessContractNodesTabComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AccessContractNodesTabComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AccessContractNodesTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
