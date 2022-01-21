import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DuplicateMetadataComponent } from './duplicate-metadata.component';

describe('DuplicateMetadataComponent', () => {
  let component: DuplicateMetadataComponent;
  let fixture: ComponentFixture<DuplicateMetadataComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DuplicateMetadataComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DuplicateMetadataComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
