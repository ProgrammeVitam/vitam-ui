import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'plural',
  standalone: true,
})
export class PluralPipe implements PipeTransform {
  transform(value: string, count: number) {
    if (value.charAt(value.length - 1) === 's') {
      return value;
    }
    if (count < 2) {
      return value;
    }

    return `${value}s`;
  }
}
