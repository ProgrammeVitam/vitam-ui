import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SaveProfileOptionsComponent } from './save-profile-options.component';

describe('SaveProfileOptionsComponent', () => {
  let component: SaveProfileOptionsComponent;
  let fixture: ComponentFixture<SaveProfileOptionsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SaveProfileOptionsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SaveProfileOptionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
