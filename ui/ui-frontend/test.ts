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

/// <reference types="vitest/globals" />

import '@angular/compiler';
import '@analogjs/vitest-angular/setup-zone';
import 'zone.js/testing';

import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { TranslateModule } from '@ngx-translate/core';
import type { Provider } from '@angular/core';

const sanitizeSelector = (selector: string) => selector.replace(/,{2,}/g, ',');
const sanitizeStyle = (style: string) =>
  style.replace(/\.mat-mdc-form-field-infix:has\(textarea\[cols\]\)\s*\{[^}]*\}/g, '');

const textContentDescriptor = Object.getOwnPropertyDescriptor(Node.prototype, 'textContent');
if (textContentDescriptor?.set && textContentDescriptor.get && !(HTMLStyleElement.prototype as any).__vitamuiTextContentPatched) {
  Object.defineProperty(HTMLStyleElement.prototype, 'textContent', {
    configurable: true,
    get() {
      return textContentDescriptor.get.call(this);
    },
    set(value: string) {
      textContentDescriptor.set.call(this, typeof value === 'string' ? sanitizeStyle(value) : value);
    },
  });
  (HTMLStyleElement.prototype as any).__vitamuiTextContentPatched = true;
}

window.addEventListener('error', (event) => {
  if (event.error instanceof SyntaxError && event.error.message.includes('mat-option,,,.mat-mdc-option textarea[cols]')) {
    event.preventDefault();
  }
});

const patchSelectorMethod = <T extends (...args: any[]) => any>(prototype: any, methodName: string) => {
  const original = prototype[methodName] as T;
  prototype[methodName] = function (selector: string, ...args: any[]) {
    try {
      return original.call(this, selector, ...args);
    } catch (error) {
      const sanitizedSelector = typeof selector === 'string' ? sanitizeSelector(selector) : selector;
      if (sanitizedSelector !== selector) {
        return original.call(this, sanitizedSelector, ...args);
      }

      throw error;
    }
  } as T;
};

[Document.prototype, DocumentFragment.prototype, Element.prototype].forEach((prototype) => {
  patchSelectorMethod(prototype, 'querySelector');
  patchSelectorMethod(prototype, 'querySelectorAll');
});
patchSelectorMethod(Element.prototype, 'matches');

const configureTestingModule = TestBed.configureTestingModule.bind(TestBed);
TestBed.configureTestingModule = ((moduleDef: any) =>
  configureTestingModule({
    ...moduleDef,
    imports: [...(moduleDef?.imports ?? []), TranslateModule.forRoot()],
    schemas: [...(moduleDef?.schemas ?? []), CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
  })) as typeof TestBed.configureTestingModule;

const providers: Provider[] = [];

export default providers;
