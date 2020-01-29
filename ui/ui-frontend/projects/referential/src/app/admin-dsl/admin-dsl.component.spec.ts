import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminDslComponent } from './admin-dsl.component';

describe('SecurisationComponent', () => {
  let component: AdminDslComponent;
  let fixture: ComponentFixture<AdminDslComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AdminDslComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AdminDslComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
