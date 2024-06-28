/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */

import { HighlightPipe } from './highlight.pipe';
import { DomSanitizer } from '@angular/platform-browser';
import { TestBed } from '@angular/core/testing';
import { SecurityContext } from '@angular/core';

describe('HighlightPipe', () => {
  let sanitizer: DomSanitizer;
  beforeEach(() => {
    sanitizer = TestBed.inject(DomSanitizer);
  });

  it('create an instance', () => {
    const pipe = new HighlightPipe(sanitizer);
    expect(pipe).toBeTruthy();
  });

  [
    [null, 'search in empty value', ''],
    [undefined, 'search in empty value', ''],
    ['', 'search in empty value', ''],
    ['This is some text', null, /^This is some text$/],
    ['This is some text', undefined, /^This is some text$/],
    ['This is some text', '', /^This is some text$/],
    ['This is some text', 'TOTO', /^This is some text$/],
    ['This is some text', 'this', /^<span.*>This<\/span> is some text$/],
    ['This is some text', 'some', /^This is <span.*>some<\/span> text$/],
    ['This is some text', 'text', /^This is some <span.*>text<\/span>$/],
    ['Abracadabra', 'ab', /^<span.*>Ab<\/span>racad<span.*>ab<\/span>ra$/],
    ['Abracadabra', 'ra', /^Ab<span.*>ra<\/span>cadab<span.*>ra<\/span>$/],
    ['Café coffee CAFÉ', 'cafe', /^<span.*>Café<\/span> coffee <span.*>CAFÉ<\/span>$/],
    ['Cafe coffee CAFE', 'café', /^<span.*>Cafe<\/span> coffee <span.*>CAFE<\/span>$/],
    ['Value with <script>alert("malicious script")</script>', 'value', /^<span.*>Value<\/span> with $/],
  ].forEach(([value, search, expected]: [string, string, RegExp | null | undefined]) =>
    it(`highlighting "${search}" in "${value}"`, () => {
      const pipe = new HighlightPipe(sanitizer);
      const transformedValue = pipe.transform(value, search);
      const actual = sanitizer.sanitize(SecurityContext.HTML, transformedValue);
      if (expected instanceof RegExp) {
        expect(actual).toMatch(expected);
      } else {
        expect(actual).toBe(expected);
      }
    }),
  );
});
