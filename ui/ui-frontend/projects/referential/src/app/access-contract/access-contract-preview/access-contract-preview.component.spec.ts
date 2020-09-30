import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import {AccessContractPreviewComponent} from './access-contract-preview.component';

// TODO fix tests
xdescribe('AccessContractPreviewComponent', () => {
  let component: AccessContractPreviewComponent;
  let fixture: ComponentFixture<AccessContractPreviewComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [AccessContractPreviewComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AccessContractPreviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
