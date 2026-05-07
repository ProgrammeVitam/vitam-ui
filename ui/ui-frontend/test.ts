/// <reference types="vitest/globals" />

import '@angular/compiler';
import '@analogjs/vitest-angular/setup-zone';
import 'zone.js/testing';

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

const providers: Provider[] = [];

export default providers;
