import {HttpClientTestingModule} from '@angular/common/http/testing';
import {NO_ERRORS_SCHEMA} from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {BASE_URL} from 'ui-frontend-common';
import {VitamUICommonTestModule} from 'ui-frontend-common/testing';
import {ContextListComponent} from './context-list.component';

// TODO fix Tests
xdescribe('ContextListComponent', () => {
  let component: ContextListComponent;
  let fixture: ComponentFixture<ContextListComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ContextListComponent],
      imports: [
        VitamUICommonTestModule,
        MatProgressSpinnerModule,
        HttpClientTestingModule
      ],
      providers: [
        {provide: BASE_URL, useValue: ''}
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ContextListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
