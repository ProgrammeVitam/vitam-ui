import { Pipe, PipeTransform } from '@angular/core';

/**
 * If the value is a Date instance, returns a String expressed in timezone Z.
 * If the value is a String, appends a Z (UTC) timezone if it has no timezone already.
 * This pipe is useful for "dates" that are instances of String and with no defined timezone. When Vitam API sends that kind of date, it should be interpreted in the UTC timezone.
 */
@Pipe({
  name: 'appendZIfNoTimezone',
})
export class AppendZIfNoTimezonePipe implements PipeTransform {
  transform(value?: string | Date | undefined): string | null | undefined {
    if (!value) return value as string;
    if (value instanceof Date) return value.toISOString();
    const hasTimezone = /Z$|[+-]\d{2}:\d{2}$|GMT[+-]\d{4}$/.test(value);
    return hasTimezone ? value : value + 'Z';
  }
}
