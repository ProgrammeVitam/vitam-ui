import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AuditPreviewComponent } from './audit-preview.component';
import {NO_ERRORS_SCHEMA} from "@angular/core";

describe('AuditPreviewComponent', () => {
  let component: AuditPreviewComponent;
  let fixture: ComponentFixture<AuditPreviewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AuditPreviewComponent ],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AuditPreviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
