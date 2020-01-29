import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AuditCreateComponent } from './audit-create.component';

describe('AuditCreateComponent', () => {
  let component: AuditCreateComponent;
  let fixture: ComponentFixture<AuditCreateComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AuditCreateComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AuditCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
