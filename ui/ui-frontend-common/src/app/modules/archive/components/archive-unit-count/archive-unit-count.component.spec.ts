import { HttpBackend, HttpClient } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { CUSTOM_ELEMENTS_SCHEMA, SimpleChange } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { By } from '@angular/platform-browser';
import { MissingTranslationHandler, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { MultiTranslateHttpLoader } from 'ngx-translate-multi-http-loader';
import { Observable, of, throwError } from 'rxjs';
import { LoggerModule } from '../../../logger';
import { VitamuiMissingTranslationHandler } from '../../../missing-translation-handler';
import { PipesModule } from '../../../pipes/pipes.module';
import { ArchiveUnitCountComponent } from './archive-unit-count.component';

export function httpLoaderFactory(httpBackend: HttpBackend): MultiTranslateHttpLoader {
  return new MultiTranslateHttpLoader(new HttpClient(httpBackend), [
    { prefix: './assets/shared-i18n/', suffix: '.json' },
    { prefix: './assets/i18n/', suffix: '.json' },
  ]);
}

class FakeTranslateLoader implements TranslateLoader {
  getTranslation(lang: string): Observable<any> {
    if (lang === 'fr') {
      return of({
        ARCHIVE_SEARCH: {
          RESULTS: 'résultats',
          ONE_SELECTED: 'séléctionné',
          MORE_THAN: '+ de',
        },
      });
    }

    return of({
      ARCHIVE_SEARCH: {
        RESULTS: 'results',
        ONE_SELECTED: 'selected',
        MORE_THAN: '+ than',
      },
    });
  }
}

describe('ArchiveUnitCountComponent', () => {
  let component: ArchiveUnitCountComponent;
  let fixture: ComponentFixture<ArchiveUnitCountComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ArchiveUnitCountComponent],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
      imports: [
        LoggerModule.forRoot(),
        HttpClientTestingModule,
        TranslateModule.forRoot({
          missingTranslationHandler: { provide: MissingTranslationHandler, useClass: VitamuiMissingTranslationHandler },
          defaultLanguage: 'fr',
          loader: {
            provide: TranslateLoader,
            useClass: FakeTranslateLoader,
            deps: [HttpBackend],
          },
        }),
        PipesModule,
        MatTooltipModule,
        MatProgressSpinnerModule,
        LoggerModule.forRoot(),
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ArchiveUnitCountComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should not allow exact count loading when archive count is under the threshold', () => {
    component.archiveUnitCount = 5000;
    component.threshold = 10000;
    component.ngOnChanges({
      archiveUnitCount: new SimpleChange(null, 5000, true),
      threshold: new SimpleChange(null, 10000, true),
    });

    fixture.detectChanges();

    expect(component.canLoadExactCount).toEqual(false);
  });

  it('should allow exact count loading when archive count is equal to the threshold', () => {
    component.archiveUnitCount = 10000;
    component.threshold = 10000;
    component.ngOnChanges({
      archiveUnitCount: new SimpleChange(null, 10000, true),
      threshold: new SimpleChange(null, 10000, true),
    });

    fixture.detectChanges();

    expect(component.canLoadExactCount).toEqual(true);
  });

  it('should not allow exact count loading if the previous exact count load is over the threshold', async () => {
    const fakeSearchService = {
      getTotalTrackHitsByCriteria: (searchCriterias: any[]): Observable<number> => {
        console.log('getTotalTrackHitsByCriteria called');

        return of(1000001);
      },
    };

    component.search = fakeSearchService.getTotalTrackHitsByCriteria([{}, {}]);
    component.archiveUnitCount = 10000;
    component.threshold = 10000;
    component.ngOnChanges({
      archiveUnitCount: new SimpleChange(null, 10000, true),
      threshold: new SimpleChange(null, 10000, true),
    });

    fixture.detectChanges();

    expect(component.canLoadExactCount).toEqual(true);
    expect(component.search).toBeTruthy();

    const linkDebugElement = fixture.debugElement.query(By.css('a'));
    const linkNativeElement = linkDebugElement.nativeElement;
    const spy = spyOn(component, 'loadExactCount').and.callThrough();

    linkNativeElement.click();

    await fixture.whenStable();

    expect(spy).toHaveBeenCalled();
    expect(component.canLoadExactCount).toEqual(false);
    expect(component.archiveUnitCount).toEqual(1000001);

    fixture.detectChanges();

    const element = fixture.debugElement.query(By.css('a'));

    expect(element).toBeFalsy();
  });

  it('should allow exact count loading if the previous exact count load has failed', async () => {
    const fakeSearchService = {
      getTotalTrackHitsByCriteria: (searchCriterias: any[]): Observable<number> => {
        console.log('getTotalTrackHitsByCriteria called');

        return throwError(new Error('Track Total Hits loading failure'));
      },
    };

    component.search = fakeSearchService.getTotalTrackHitsByCriteria([{}, {}]);
    component.archiveUnitCount = 10000;
    component.threshold = 10000;
    component.ngOnChanges({
      archiveUnitCount: new SimpleChange(null, 10000, true),
      threshold: new SimpleChange(null, 10000, true),
    });

    fixture.detectChanges();

    expect(component.canLoadExactCount).toEqual(true, 'canLoadExactCount must be true');
    expect(component.search).toBeTruthy('component.search is not truthy');

    const linkDebugElement = fixture.debugElement.query(By.css('a'));
    const linkNativeElement = linkDebugElement.nativeElement;
    const spy = spyOn(component, 'loadExactCount').and.callThrough();

    linkNativeElement.click();

    await fixture.whenStable();

    expect(spy).toHaveBeenCalled();
    expect(component.canLoadExactCount).withContext('canLoadExactCount must be true').toEqual(true);
    expect(component.archiveUnitCount).toEqual(10000);

    fixture.detectChanges();

    const element = fixture.debugElement.query(By.css('a'));

    expect(element).toBeTruthy('element <a> is not truthy');
  });

  it('should allow exact count loading again when the search query has changed', async () => {
    const fakeSearchService = {
      getTotalTrackHitsByCriteria: (searchCriterias: any[]): Observable<number> => {
        console.log('getTotalTrackHitsByCriteria called');

        return of(1000001);
      },
    };
    const firstSearch = fakeSearchService.getTotalTrackHitsByCriteria([{}, {}]);

    component.search = firstSearch;
    component.archiveUnitCount = 10000;
    component.threshold = 10000;
    component.ngOnChanges({
      archiveUnitCount: new SimpleChange(null, 10000, true),
      threshold: new SimpleChange(null, 10000, true),
    });

    fixture.detectChanges();

    expect(component.canLoadExactCount).toEqual(true);
    expect(component.search).toBeTruthy();

    const linkDebugElement = fixture.debugElement.query(By.css('a'));
    const linkNativeElement = linkDebugElement.nativeElement;
    const spy = spyOn(component, 'loadExactCount').and.callThrough();

    linkNativeElement.click();

    await fixture.whenStable();

    expect(spy).toHaveBeenCalled();
    expect(component.canLoadExactCount).toEqual(false);
    expect(component.archiveUnitCount).toEqual(1000001);

    fixture.detectChanges();

    const element = fixture.debugElement.query(By.css('a'));

    expect(element).toBeFalsy();

    // Update component with a second search query

    const otherFakeSearchService = {
      getTotalTrackHitsByCriteria: (searchCriterias: any[]): Observable<number> => {
        console.log('getTotalTrackHitsByCriteria called');

        return of(25000);
      },
    };
    const secondSearch = otherFakeSearchService.getTotalTrackHitsByCriteria([{}, {}]);

    component.search = secondSearch;
    component.ngOnChanges({
      search: new SimpleChange(firstSearch, secondSearch, false),
    });

    fixture.detectChanges();

    const elementAfterSearchQueryUpdate = fixture.debugElement.query(By.css('a'));

    expect(elementAfterSearchQueryUpdate).toBeTruthy();
  });

  it('should not allow exact count loading again when the search query has changed and exact count was reloaded', async () => {
    const fakeSearchService = {
      getTotalTrackHitsByCriteria: (searchCriterias: any[]): Observable<number> => {
        console.log('getTotalTrackHitsByCriteria called');

        return of(1000001);
      },
    };
    const firstSearch = fakeSearchService.getTotalTrackHitsByCriteria([{}, {}]);

    component.search = firstSearch;
    component.archiveUnitCount = 10000;
    component.threshold = 10000;
    component.ngOnChanges({
      archiveUnitCount: new SimpleChange(null, 10000, true),
      threshold: new SimpleChange(null, 10000, true),
    });

    fixture.detectChanges();

    expect(component.canLoadExactCount).toEqual(true);
    expect(component.search).toBeTruthy();

    const linkDebugElement = fixture.debugElement.query(By.css('a'));
    const linkNativeElement = linkDebugElement.nativeElement;
    const spy = spyOn(component, 'loadExactCount').and.callThrough();

    linkNativeElement.click();

    await fixture.whenStable();

    expect(spy).toHaveBeenCalled();
    expect(component.canLoadExactCount).toEqual(false);
    expect(component.archiveUnitCount).toEqual(1000001);

    fixture.detectChanges();

    const element = fixture.debugElement.query(By.css('a'));

    expect(element).toBeFalsy();

    // Update component with a second search query

    const otherFakeSearchService = {
      getTotalTrackHitsByCriteria: (searchCriterias: any[]): Observable<number> => {
        console.log('getTotalTrackHitsByCriteria called');

        return of(25000);
      },
    };
    const secondSearch = otherFakeSearchService.getTotalTrackHitsByCriteria([{}, {}]);

    component.search = secondSearch;
    component.ngOnChanges({
      search: new SimpleChange(firstSearch, secondSearch, false),
    });

    fixture.detectChanges();

    const elementAfterSearchQueryUpdate = fixture.debugElement.query(By.css('a'));

    expect(elementAfterSearchQueryUpdate).toBeTruthy();

    // Load a second time trackTotalHits

    const secondLinkDebugElement = fixture.debugElement.query(By.css('a'));
    const linsecondNativeElement = secondLinkDebugElement.nativeElement;

    linsecondNativeElement.click();

    await fixture.whenStable();

    expect(spy).toHaveBeenCalled();
    expect(component.canLoadExactCount).toEqual(false);
    expect(component.archiveUnitCount).toEqual(25000);

    fixture.detectChanges();

    const elementAfterSecondLoadExactCount = fixture.debugElement.query(By.css('a'));

    expect(elementAfterSecondLoadExactCount).toBeFalsy();
  });
});
