import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { HoldingSchemaComponent } from './holding-schema.component';

describe('HoldingSchemaComponent', () => {
  let component: HoldingSchemaComponent;
  let fixture: ComponentFixture<HoldingSchemaComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ HoldingSchemaComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(HoldingSchemaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
