import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ArkSearchComponent } from './ark-search.component';

describe('ArkSearchComponent', () => {
  let component: ArkSearchComponent;
  let fixture: ComponentFixture<ArkSearchComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ArkSearchComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ArkSearchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
