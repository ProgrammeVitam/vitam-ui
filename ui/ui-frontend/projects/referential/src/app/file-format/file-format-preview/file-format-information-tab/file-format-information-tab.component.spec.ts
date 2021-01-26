import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import {NO_ERRORS_SCHEMA} from '@angular/core';
import {FormBuilder} from '@angular/forms';
import {FileFormat} from 'projects/vitamui-library/src/public-api';
import {of} from 'rxjs';
import {FileFormatService} from '../../file-format.service';
import {FileFormatInformationTabComponent} from './file-format-information-tab.component';

describe('FileFormatInformationTabComponent', () => {
  let component: FileFormatInformationTabComponent;
  let fixture: ComponentFixture<FileFormatInformationTabComponent>;

  const fileFormatServiceMock = {
    // tslint:disable-next-line:variable-name
    patch: (_data: any) => of(null)
  };

  const fileFormatValue = {
    puid: 'EXTERNAL_puid',
    name: 'Name',
    mimeType: 'application/puid',
    version: '1.0',
    versionPronom: '3.0',
    extensions: ['.puid']
  };

  const previousValue: FileFormat = {
    id: 'vitam_id',
    documentVersion: 0,
    version: '1.0',
    versionPronom: '3.0',
    puid: 'EXTERNAL_puid',
    name: 'Name',
    description: 'Format de Fichier',
    mimeType: 'application/puid',
    hasPriorityOverFileFormatIDs: [],
    group: 'test',
    alert: false,
    comment: 'No Comment',
    extensions: ['.puid'],
    createdDate: '20/02/2020'
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [FileFormatInformationTabComponent],
      providers: [
        FormBuilder,
        {provide: FileFormatService, useValue: fileFormatServiceMock}
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FileFormatInformationTabComponent);
    component = fixture.componentInstance;
    component.form.setValue(fileFormatValue);
    component.previousValue = (): FileFormat => previousValue;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
