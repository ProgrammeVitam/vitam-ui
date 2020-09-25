import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { UploadTreesPlansComponent } from './upload-trees-plans.component';

describe('UploadTreesPlansComponent', () => {
  let component: UploadTreesPlansComponent;
  let fixture: ComponentFixture<UploadTreesPlansComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ UploadTreesPlansComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UploadTreesPlansComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
