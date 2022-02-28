import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ArchiveSearchRulesFacetsComponent } from './archive-search-rules-facets.component';

describe('ArchiveSearchRulesFacetsComponent', () => {
  let component: ArchiveSearchRulesFacetsComponent;
  let fixture: ComponentFixture<ArchiveSearchRulesFacetsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ArchiveSearchRulesFacetsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ArchiveSearchRulesFacetsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
