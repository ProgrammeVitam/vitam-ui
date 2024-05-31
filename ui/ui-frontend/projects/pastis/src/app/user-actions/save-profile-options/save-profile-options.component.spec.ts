import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateModule } from '@ngx-translate/core';
import { of } from 'rxjs';

import { SaveProfileOptionsComponent } from './save-profile-options.component';

const matDialogData = jasmine.createSpyObj('MAT_DIALOG_DATA', ['open']);
matDialogData.open.and.returnValue({ afterClosed: () => of(true) });
const matDialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['open']);
matDialogRefSpy.open.and.returnValue({ afterClosed: () => of(true) });

describe('SaveProfileOptionsComponent', () => {
  let component: SaveProfileOptionsComponent;
  let fixture: ComponentFixture<SaveProfileOptionsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [SaveProfileOptionsComponent],
      imports: [RouterTestingModule, TranslateModule.forRoot()],
      providers: [
        { provide: MatDialogRef, useValue: matDialogRefSpy },
        { provide: MAT_DIALOG_DATA, useValue: matDialogData },
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SaveProfileOptionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
