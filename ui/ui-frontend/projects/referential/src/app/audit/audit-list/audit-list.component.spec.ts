import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import {NO_ERRORS_SCHEMA} from '@angular/core';
import {of} from 'rxjs';
import {AuditService} from '../audit.service';
import {AuditListComponent} from './audit-list.component';

describe('AuditListComponent', () => {
  let component: AuditListComponent;
  let fixture: ComponentFixture<AuditListComponent>;

  beforeEach(waitForAsync(() => {

    const auditServiceMock = {
      search: () => of(null)
    };

    TestBed.configureTestingModule({
      declarations: [AuditListComponent],
      providers: [
        {provide: AuditService, useValue: auditServiceMock}
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AuditListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
