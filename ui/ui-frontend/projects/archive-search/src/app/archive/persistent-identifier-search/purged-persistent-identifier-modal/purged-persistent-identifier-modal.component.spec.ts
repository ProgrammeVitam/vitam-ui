import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PurgedPersistentIdentifierModalComponent } from './purged-persistent-identifier-modal.component';

describe('ErrorResponseModalComponent', () => {
  let component: PurgedPersistentIdentifierModalComponent;
  let fixture: ComponentFixture<PurgedPersistentIdentifierModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PurgedPersistentIdentifierModalComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PurgedPersistentIdentifierModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
