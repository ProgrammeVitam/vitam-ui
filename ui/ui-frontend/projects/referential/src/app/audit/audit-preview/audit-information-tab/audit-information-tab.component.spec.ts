import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AuditInformationTabComponent } from './audit-information-tab.component';

describe('AuditInformationTabComponent', () => {
  let component: AuditInformationTabComponent;
  let fixture: ComponentFixture<AuditInformationTabComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AuditInformationTabComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AuditInformationTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
