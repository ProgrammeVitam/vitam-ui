/// <reference types="vitest/globals" />

import 'zone.js/testing';
import { provideZonelessChangeDetection } from '@angular/core';

export default [
  provideZonelessChangeDetection(),
];
