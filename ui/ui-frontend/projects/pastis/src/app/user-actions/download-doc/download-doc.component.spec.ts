import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UserActionsDownloadDocComponent } from './download-doc.component';

describe('DownloadDocComponent', () => {
  let component: UserActionsDownloadDocComponent;
  let fixture: ComponentFixture<UserActionsDownloadDocComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [UserActionsDownloadDocComponent],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(UserActionsDownloadDocComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
