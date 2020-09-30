import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import {NO_ERRORS_SCHEMA} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {FileFormat} from 'projects/vitamui-library/src/public-api';
import {of} from 'rxjs';
import {AuthService, BASE_URL, VitamUISnackBar} from 'ui-frontend-common';
import {FileFormatService} from '../file-format.service';
import {FileFormatListComponent} from './file-format-list.component';


describe('FileFormatListComponent', () => {
  let component: FileFormatListComponent;
  let fixture: ComponentFixture<FileFormatListComponent>;

  const fileFormatServiceMock = {
    // tslint:disable-next-line:variable-name
    delete: (_fileFormat: FileFormat) => of(null),
    search: () => of(null)
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [FileFormatListComponent],
      providers: [
        {provide: BASE_URL, useValue: ''},
        {provide: FileFormatService, useValue: fileFormatServiceMock},
        {provide: AuthService, useValue: {user: {proofTenantIdentifier: '1'}}},
        {provide: VitamUISnackBar, useValue: {}},
        {provide: MatDialog, useValue: {}}
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FileFormatListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
