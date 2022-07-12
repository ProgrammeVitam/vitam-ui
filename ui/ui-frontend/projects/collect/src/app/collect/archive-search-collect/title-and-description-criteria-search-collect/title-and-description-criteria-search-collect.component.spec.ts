import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TitleAndDescriptionCriteriaSearchCollectComponent } from './title-and-description-criteria-search-collect.component';

describe('TitleAndDescriptionCriteriaSearchCollectComponent', () => {
  let component: TitleAndDescriptionCriteriaSearchCollectComponent;
  let fixture: ComponentFixture<TitleAndDescriptionCriteriaSearchCollectComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TitleAndDescriptionCriteriaSearchCollectComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TitleAndDescriptionCriteriaSearchCollectComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
