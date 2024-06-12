import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatLegacyDialogModule as MatDialogModule } from '@angular/material/legacy-dialog';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { Observable, of } from 'rxjs';
import { BASE_URL } from 'vitamui-library';

import { PersistentIdentifierSearchComponent } from './persistent-identifier-search.component';

const translations: any = { TEST: 'Mock translate test' };
class FakeLoader implements TranslateLoader {
  getTranslation(): Observable<any> {
    return of(translations);
  }
}

describe('PersistentIdentifierSearchComponent', () => {
  let component: PersistentIdentifierSearchComponent;
  let fixture: ComponentFixture<PersistentIdentifierSearchComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [PersistentIdentifierSearchComponent],
      imports: [
        MatDialogModule,
        HttpClientTestingModule,
        RouterTestingModule,
        TranslateModule.forRoot({
          loader: { provide: TranslateLoader, useClass: FakeLoader },
        }),
      ],
      providers: [{ provide: BASE_URL, useValue: '/fake-api' }],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PersistentIdentifierSearchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
