import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AccessionRegisterAdvancedSearchComponent } from './accession-register-advanced-search.component';

describe('AccessionRegisterAdvancedSearchComponent', () => {
  let component: AccessionRegisterAdvancedSearchComponent;
  let fixture: ComponentFixture<AccessionRegisterAdvancedSearchComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AccessionRegisterAdvancedSearchComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AccessionRegisterAdvancedSearchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
