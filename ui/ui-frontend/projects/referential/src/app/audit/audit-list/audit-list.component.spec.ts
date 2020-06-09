import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AuditListComponent } from './audit-list.component';
import {NO_ERRORS_SCHEMA} from "@angular/core";
import { AuditService } from '../audit.service';
import { of } from 'rxjs';

describe('AuditListComponent', () => {
  let component: AuditListComponent;
  let fixture: ComponentFixture<AuditListComponent>;

  beforeEach(async(() => {

    const auditServiceMock = {
      search: () => of(null)
    };

    TestBed.configureTestingModule({
      declarations: [ AuditListComponent ],
      providers:[
        {provide:AuditService, useValue:auditServiceMock}
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
