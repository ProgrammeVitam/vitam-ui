import { Pipe, PipeTransform } from '@angular/core';

import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import DOMPurify from 'dompurify';

@Pipe({ name: 'highlight' })
export class HighlightPipe implements PipeTransform {
  constructor(private sanitizer: DomSanitizer) {}

  transform(value?: string, args?: string): SafeHtml | string {
    if (!args) {
      return DOMPurify.sanitize(value);
    }

    const originalStringToSearchWithoutAccent = this.withoutAccent(args);
    const searchInWithoutAccent = this.withoutAccent(value);

    const regex = new RegExp(originalStringToSearchWithoutAccent, 'gi');
    let result = '';
    let startIndex = 0;

    while (regex.exec(searchInWithoutAccent) !== null) {
      const matchedStringIndex = regex.lastIndex - args.length;
      result = result.concat(
        value.substring(startIndex, matchedStringIndex),
        `<span class="highlight-pipe">${value.substring(matchedStringIndex, regex.lastIndex)}</span>`,
      );
      startIndex = regex.lastIndex;
    }

    return startIndex > 0
      ? this.sanitizer.bypassSecurityTrustHtml(DOMPurify.sanitize(result.concat(value.substring(startIndex))))
      : DOMPurify.sanitize(value);
  }

  private withoutAccent(val?: string): string {
    return val?.normalize('NFD')?.replace(/[\u0300-\u036f]/g, '');
  }
}
