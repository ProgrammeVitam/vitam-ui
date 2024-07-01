import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'appendStar',
  standalone: true,
})
export class AppendStarPipe implements PipeTransform {
  transform(value: string, shouldAppend: boolean = true) {
    return `${value}${shouldAppend ? '*' : ''}`;
  }
}
