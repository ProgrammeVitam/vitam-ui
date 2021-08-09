import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ApparaisalRuleSearchComponent } from './apparaisal-rule-search.component';

describe('ApparaisalRuleSearchComponent', () => {
  let component: ApparaisalRuleSearchComponent;
  let fixture: ComponentFixture<ApparaisalRuleSearchComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ApparaisalRuleSearchComponent],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ApparaisalRuleSearchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
