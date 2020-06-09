import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { MatProgressSpinnerModule } from '@angular/material';
import { VitamUICommonTestModule } from 'ui-frontend-common/testing';

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { BASE_URL } from 'ui-frontend-common';
import { ContextListComponent } from "./context-list.component";
import {NO_ERRORS_SCHEMA} from "@angular/core";

// TODO fix Tests
xdescribe('ContextListComponent', () => {
  let component: ContextListComponent;
  let fixture: ComponentFixture<ContextListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ContextListComponent],
      imports: [
        VitamUICommonTestModule,
        MatProgressSpinnerModule,
        HttpClientTestingModule
      ],
      providers: [
        {provide: BASE_URL, useValue: ""}
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
