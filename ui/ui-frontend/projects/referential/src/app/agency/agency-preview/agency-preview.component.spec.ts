import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AgencyPreviewComponent } from './agency-preview.component';

describe('AgencyPreviewComponent', () => {
  let component: AgencyPreviewComponent;
  let fixture: ComponentFixture<AgencyPreviewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AgencyPreviewComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AgencyPreviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
