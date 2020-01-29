import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import {AuthService, BASE_URL} from 'ui-frontend-common';
import { FileFormatListComponent } from "./file-format-list.component";
import { NO_ERRORS_SCHEMA } from "@angular/core";
import { FileFormatService } from "../file-format.service";
import { of } from "rxjs";
import { FileFormat } from "vitamui-library";

describe('FileFormatListComponent', () => {
  let component: FileFormatListComponent;
  let fixture: ComponentFixture<FileFormatListComponent>;

  const fileFormatServiceMock = {
    delete: (_fileFormat: FileFormat) => of(null),
    search: () => of(null)
  };

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [FileFormatListComponent],
      providers: [
        { provide: BASE_URL, useValue: "" },
        { provide: FileFormatService, useValue: fileFormatServiceMock},
        { provide: AuthService, useValue: {user: {proofTenantIdentifier: '1'}}}
      ],
      schemas: [ NO_ERRORS_SCHEMA ]
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
