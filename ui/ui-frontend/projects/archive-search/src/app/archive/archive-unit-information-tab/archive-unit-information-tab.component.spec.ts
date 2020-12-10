import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ArchiveUnitInformationTabComponent } from './archive-unit-information-tab.component';

describe('ArchiveUnitInformationTabComponent', () => {
  let component: ArchiveUnitInformationTabComponent;
  let fixture: ComponentFixture<ArchiveUnitInformationTabComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ArchiveUnitInformationTabComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ArchiveUnitInformationTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
