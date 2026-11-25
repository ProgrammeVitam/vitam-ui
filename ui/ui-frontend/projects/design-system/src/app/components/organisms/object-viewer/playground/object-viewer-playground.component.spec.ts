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
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { provideTranslateService } from '@ngx-translate/core';
import { LoggerModule } from 'vitamui-library';
import { ObjectViewerPlaygroundComponent } from './object-viewer-playground.component';

describe('ObjectViewerPlaygroundComponent', () => {
  let fixture: ComponentFixture<ObjectViewerPlaygroundComponent>;

  const textarea = (index: number): HTMLTextAreaElement => fixture.nativeElement.querySelectorAll('textarea')[index];

  const type = (index: number, text: string): void => {
    textarea(index).value = text;
    textarea(index).dispatchEvent(new Event('input'));
    fixture.detectChanges();
  };

  const viewer = (): Element | null => fixture.nativeElement.querySelector('vitamui-common-object-viewer');
  const invalidTemplateMessage = (): Element | null =>
    [...fixture.nativeElement.querySelectorAll('p.text-danger')].find((p: Element) =>
      p.textContent.includes('OBJECT_VIEWER.PLAYGROUND.INVALID_TEMPLATE'),
    );

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ObjectViewerPlaygroundComponent, LoggerModule.forRoot()],
      providers: [provideTranslateService(), provideNoopAnimations()],
    });
    fixture = TestBed.createComponent(ObjectViewerPlaygroundComponent);
    fixture.detectChanges();
  });

  it('should display the viewer with the sample inputs', () => {
    expect(viewer()).toBeTruthy();
    expect(invalidTemplateMessage()).toBeFalsy();
  });

  it('should give each JSON input an accessible name', () => {
    expect(textarea(0).getAttribute('aria-label')).toBe('OBJECT_VIEWER.DATA');
    expect(textarea(1).getAttribute('aria-label')).toBe('OBJECT_VIEWER.TEMPLATE');
  });

  it.each(['null', 'false', '0', '""'])('should display the viewer when the data is %s', (text) => {
    type(0, text);

    expect(viewer()).toBeTruthy();
  });

  it('should hide the viewer when the data is not valid JSON', () => {
    type(0, '{');

    expect(viewer()).toBeFalsy();
  });

  it.each(['{}', 'null', 'false', '0', '""', '[{}]', '[{ "ui": { "Path": 0 } }]'])(
    'should display a validation message instead of the viewer when the template is %s',
    (text) => {
      type(1, text);

      expect(viewer()).toBeFalsy();
      expect(invalidTemplateMessage()).toBeTruthy();
    },
  );

  it('should not display the validation message when the template is not valid JSON', () => {
    type(1, '[');

    expect(viewer()).toBeFalsy();
    expect(invalidTemplateMessage()).toBeFalsy();
  });

  it('should display the viewer when the template is an empty list', () => {
    type(1, '[]');

    expect(viewer()).toBeTruthy();
    expect(invalidTemplateMessage()).toBeFalsy();
  });
});
