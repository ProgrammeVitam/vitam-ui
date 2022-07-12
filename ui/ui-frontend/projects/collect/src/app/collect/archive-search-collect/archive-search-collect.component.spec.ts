import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ArchiveSearchCollectComponent } from './archive-search-collect.component';

describe('ArchiveSearchCollectComponent', () => {
  let component: ArchiveSearchCollectComponent;
  let fixture: ComponentFixture<ArchiveSearchCollectComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ArchiveSearchCollectComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ArchiveSearchCollectComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
