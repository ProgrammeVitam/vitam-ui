import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TitleAndDescriptionCriteriaSearchComponent } from './title-and-description-criteria-search.component';

describe('TitleAndDescriptionCriteriaSearchComponent', () => {
  let component: TitleAndDescriptionCriteriaSearchComponent;
  let fixture: ComponentFixture<TitleAndDescriptionCriteriaSearchComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TitleAndDescriptionCriteriaSearchComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TitleAndDescriptionCriteriaSearchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
