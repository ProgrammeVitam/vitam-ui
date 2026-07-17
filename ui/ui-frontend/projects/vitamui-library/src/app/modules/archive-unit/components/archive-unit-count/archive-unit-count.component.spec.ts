/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
import { HttpBackend, provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA, SimpleChange } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { By } from '@angular/platform-browser';
import { MissingTranslationHandler, TranslateLoader } from '@ngx-translate/core';
import { Observable, of, throwError } from 'rxjs';
import { LoggerModule } from '../../../logger';
import { VitamuiMissingTranslationHandler } from '../../../missing-translation-handler';
import { PluralPipe } from '../../../pipes/plural.pipe';
import { ArchiveUnitCountComponent } from './archive-unit-count.component';

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
      declarations: [ArchiveUnitCountComponent, PluralPipe],
      schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
      imports: [LoggerModule.forRoot(), MatProgressSpinnerModule, LoggerModule.forRoot()],
      providers: [provideHttpClient(withInterceptorsFromDi()), provideHttpClientTesting()],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ArchiveUnitCountComponent);
    component = fixture.componentInstance;
    fixture.changeDetectorRef.detectChanges();
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

    fixture.changeDetectorRef.detectChanges();

    expect(component.canLoadExactCount).toEqual(false);
  });

  it('should allow exact count loading when archive count is equal to the threshold', () => {
    component.archiveUnitCount = 10000;
    component.threshold = 10000;
    component.ngOnChanges({
      archiveUnitCount: new SimpleChange(null, 10000, true),
      threshold: new SimpleChange(null, 10000, true),
    });

    fixture.changeDetectorRef.detectChanges();

    expect(component.canLoadExactCount).toEqual(true);
  });

  it('should not allow exact count loading if the previous exact count load is over the threshold', async () => {
    const fakeSearchService = {
      getTotalTrackHitsByCriteria: (): Observable<number> => of(1000001),
    };

    component.search = fakeSearchService.getTotalTrackHitsByCriteria();
    component.archiveUnitCount = 10000;
    component.threshold = 10000;
    component.ngOnChanges({
      archiveUnitCount: new SimpleChange(null, 10000, true),
      threshold: new SimpleChange(null, 10000, true),
    });

    fixture.changeDetectorRef.detectChanges();

    expect(component.canLoadExactCount).toEqual(true);
    expect(component.search).toBeTruthy();

    const linkDebugElement = fixture.debugElement.query(By.css('a'));
    const linkNativeElement = linkDebugElement.nativeElement;
    const spy = vi.spyOn(component, 'loadExactCount');

    linkNativeElement.click();

    await fixture.whenStable();

    expect(spy).toHaveBeenCalled();
    expect(component.canLoadExactCount).toEqual(false);
    expect(component.archiveUnitCount).toEqual(1000001);

    fixture.changeDetectorRef.detectChanges();

    const element = fixture.debugElement.query(By.css('a'));

    expect(element).toBeFalsy();
  });

  it('should allow exact count loading if the previous exact count load has failed', async () => {
    const fakeSearchService = {
      getTotalTrackHitsByCriteria: (): Observable<number> => throwError(new Error('Track Total Hits loading failure')),
    };

    component.search = fakeSearchService.getTotalTrackHitsByCriteria();
    component.archiveUnitCount = 10000;
    component.threshold = 10000;
    component.ngOnChanges({
      archiveUnitCount: new SimpleChange(null, 10000, true),
      threshold: new SimpleChange(null, 10000, true),
    });

    fixture.changeDetectorRef.detectChanges();

    expect(component.canLoadExactCount).toEqual(true);
    expect(component.search).toBeTruthy();

    const linkDebugElement = fixture.debugElement.query(By.css('a'));
    const linkNativeElement = linkDebugElement.nativeElement;
    const spy = vi.spyOn(component, 'loadExactCount');

    linkNativeElement.click();

    await fixture.whenStable();

    expect(spy).toHaveBeenCalled();
    expect(component.canLoadExactCount, 'canLoadExactCount must be true').toEqual(true);
    expect(component.archiveUnitCount).toEqual(10000);

    fixture.changeDetectorRef.detectChanges();

    const element = fixture.debugElement.query(By.css('a'));

    expect(element).toBeTruthy();
  });

  it('should allow exact count loading again when the search query has changed', async () => {
    const fakeSearchService = {
      getTotalTrackHitsByCriteria: (): Observable<number> => of(1000001),
    };
    const firstSearch = fakeSearchService.getTotalTrackHitsByCriteria();

    component.search = firstSearch;
    component.archiveUnitCount = 10000;
    component.threshold = 10000;
    component.ngOnChanges({
      archiveUnitCount: new SimpleChange(null, 10000, true),
      threshold: new SimpleChange(null, 10000, true),
    });

    fixture.changeDetectorRef.detectChanges();

    expect(component.canLoadExactCount).toEqual(true);
    expect(component.search).toBeTruthy();

    const linkDebugElement = fixture.debugElement.query(By.css('a'));
    const linkNativeElement = linkDebugElement.nativeElement;
    const spy = vi.spyOn(component, 'loadExactCount');

    linkNativeElement.click();

    await fixture.whenStable();

    expect(spy).toHaveBeenCalled();
    expect(component.canLoadExactCount).toEqual(false);
    expect(component.archiveUnitCount).toEqual(1000001);

    fixture.changeDetectorRef.detectChanges();

    const element = fixture.debugElement.query(By.css('a'));

    expect(element).toBeFalsy();

    // Update component with a second search query

    const otherFakeSearchService = {
      getTotalTrackHitsByCriteria: (): Observable<number> => of(25000),
    };
    const secondSearch = otherFakeSearchService.getTotalTrackHitsByCriteria();

    component.search = secondSearch;
    component.ngOnChanges({
      search: new SimpleChange(firstSearch, secondSearch, false),
    });

    fixture.changeDetectorRef.detectChanges();

    const elementAfterSearchQueryUpdate = fixture.debugElement.query(By.css('a'));

    expect(elementAfterSearchQueryUpdate).toBeTruthy();
  });

  it('should not allow exact count loading again when the search query has changed and exact count was reloaded', async () => {
    const fakeSearchService = {
      getTotalTrackHitsByCriteria: (): Observable<number> => of(1000001),
    };
    const firstSearch = fakeSearchService.getTotalTrackHitsByCriteria();

    component.search = firstSearch;
    component.archiveUnitCount = 10000;
    component.threshold = 10000;
    component.ngOnChanges({
      archiveUnitCount: new SimpleChange(null, 10000, true),
      threshold: new SimpleChange(null, 10000, true),
    });

    fixture.changeDetectorRef.detectChanges();

    expect(component.canLoadExactCount).toEqual(true);
    expect(component.search).toBeTruthy();

    const linkDebugElement = fixture.debugElement.query(By.css('a'));
    const linkNativeElement = linkDebugElement.nativeElement;
    const spy = vi.spyOn(component, 'loadExactCount');

    linkNativeElement.click();

    await fixture.whenStable();

    expect(spy).toHaveBeenCalled();
    expect(component.canLoadExactCount).toEqual(false);
    expect(component.archiveUnitCount).toEqual(1000001);

    fixture.changeDetectorRef.detectChanges();

    const element = fixture.debugElement.query(By.css('a'));

    expect(element).toBeFalsy();

    // Update component with a second search query

    const otherFakeSearchService = {
      getTotalTrackHitsByCriteria: (): Observable<number> => of(25000),
    };
    const secondSearch = otherFakeSearchService.getTotalTrackHitsByCriteria();

    component.search = secondSearch;
    component.ngOnChanges({
      search: new SimpleChange(firstSearch, secondSearch, false),
    });

    fixture.changeDetectorRef.detectChanges();

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

    fixture.changeDetectorRef.detectChanges();

    const elementAfterSecondLoadExactCount = fixture.debugElement.query(By.css('a'));

    expect(elementAfterSecondLoadExactCount).toBeFalsy();
  });
});
