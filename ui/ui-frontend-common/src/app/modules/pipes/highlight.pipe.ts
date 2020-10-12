import { Pipe, PipeTransform } from '@angular/core';

import {DomSanitizer} from '@angular/platform-browser';
import { ThemeColorType } from '../utils';

@Pipe({name: 'highlight'})
export class  HighlightPipe implements PipeTransform {
  constructor(private sanitizer: DomSanitizer) {}

  transform(value: any, args: any): any {
    if (!args) {
      return value;
    }

    const originalStringToSearchWitoutAccent = this.withoutAcent(args);
    const searchInWithoutAccent = this.withoutAcent(value);

    const regex = new RegExp(originalStringToSearchWitoutAccent, 'gi');
    let result = '';
    let startIndex = 0;

    while (regex.exec(searchInWithoutAccent) !== null) {
      const matchedStringIndex = regex.lastIndex - args.length;
      // tslint:disable-next-line: max-line-length
      const coloredString = `<span style='color: var(--${ThemeColorType.VITAMUI_PRIMARY});font-weight: bold;'>${value.substring(matchedStringIndex, regex.lastIndex)}</span>`;
      result = result.concat(searchInWithoutAccent.substring(startIndex, matchedStringIndex), coloredString);
      startIndex = regex.lastIndex;
    }

    if (startIndex > 0) {
      result = result.concat(value.substr(startIndex));
    }

    return this.sanitizer.bypassSecurityTrustHtml(result);
  }

  private withoutAcent(val: string): string {
    return val.normalize('NFD').replace(/[\u0300-\u036f]/g, '');
  }
}
