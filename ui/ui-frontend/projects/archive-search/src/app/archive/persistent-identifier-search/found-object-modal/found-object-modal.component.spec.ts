import { HttpBackend } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { RouterTestingModule } from '@angular/router/testing';
import { MissingTranslationHandler, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { Observable, of } from 'rxjs';
import { BASE_URL, VitamuiMissingTranslationHandler } from 'ui-frontend-common';
import { FoundObjectModalComponent } from './found-object-modal.component';

class FakeTranslateLoader implements TranslateLoader {
  getTranslation(lang: string): Observable<any> {
    if (lang === 'fr') {
      return of({
        // Add wanted translations
      });
    }

    return of({
      // Add wanted translations
    });
  }
}

describe('ErrorResponseModalComponent', () => {
  let component: FoundObjectModalComponent;
  let fixture: ComponentFixture<FoundObjectModalComponent>;
  const matDialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close', 'keydownEvents']);

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        RouterTestingModule,
        MatSnackBarModule,
        TranslateModule.forRoot({
          missingTranslationHandler: { provide: MissingTranslationHandler, useClass: VitamuiMissingTranslationHandler },
          defaultLanguage: 'fr',
          loader: {
            provide: TranslateLoader,
            useClass: FakeTranslateLoader,
            deps: [HttpBackend],
          },
        }),
      ],
      declarations: [FoundObjectModalComponent],
      providers: [
        {
          provide: MatDialogRef,
          useValue: matDialogRefSpy,
        },
        { provide: BASE_URL, useValue: '/fake-api' },
        {
          provide: MAT_DIALOG_DATA,
          useValue: {
            ark: '',
            object: {
              '#id': '',
              '#tenant': '',
              '#unitups': [],
              '#allunitups': [],
              '#operations': [],
              '#opi': '',
              '#originating_agency': '',
              '#originating_agencies': [],
              '#storage': {},
              '#nbobjects': {},
              FileInfo: {},
              '#qualifiers': [{ qualifier: '', '#nbc': 0 }],
              '#approximate_creation_date': '',
              '#approximate_update_date': '',
            },
          },
        },
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FoundObjectModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
