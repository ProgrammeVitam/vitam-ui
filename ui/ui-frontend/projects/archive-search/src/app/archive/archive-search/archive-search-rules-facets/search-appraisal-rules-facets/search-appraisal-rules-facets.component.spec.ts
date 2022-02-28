import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SearchAppraisalRulesFacetsComponent } from './search-appraisal-rules-facets.component';

describe('SearchAppraisalRulesFacetsComponent', () => {
  let component: SearchAppraisalRulesFacetsComponent;
  let fixture: ComponentFixture<SearchAppraisalRulesFacetsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SearchAppraisalRulesFacetsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SearchAppraisalRulesFacetsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
