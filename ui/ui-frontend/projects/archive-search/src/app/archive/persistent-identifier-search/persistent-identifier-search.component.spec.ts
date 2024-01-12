import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PersistentIdentifierSearchComponent } from './persistent-identifier-search.component';

describe('PersistentIdentifierSearchComponent', () => {
  let component: PersistentIdentifierSearchComponent;
  let fixture: ComponentFixture<PersistentIdentifierSearchComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [PersistentIdentifierSearchComponent]
    })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PersistentIdentifierSearchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
