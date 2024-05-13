import { Pipe, PipeTransform } from '@angular/core';
import { Unit } from '../models/units/unit.interface';

/**
 * Extracts the Title (or Description) from a Unit.
 * It defaults to "Title" (or "Description") attributes if present.
 * Otherwise, it'll get the French translation if present. By default, the Title_.fr, otherwise in non-lower-case (ex: Title_.FR or Title_.Fr or Title_.fR) if present.
 * Otherwise, it'll get the English translation if present. By default, the Title_.en, otherwise in non-lower-case (ex: Title_.EN or Title_.En or Title_.eN) if present.
 * Otherwise, it'll get any available translation.
 * Otherwise, empty string.
 */
@Pipe({ name: 'unitI18n' })
export class UnitI18nPipe implements PipeTransform {
  transform(unit: Unit, attribute: 'Title' | 'Description') {
    return getUnitI18nAttribute(unit, attribute);
  }
}

export function getUnitI18nAttribute(unit: Unit, attribute: 'Title' | 'Description') {
  if (unit[attribute]) {
    return unit[attribute];
  }
  const i18nAttribute = `${attribute}_`;
  const unitElement = unit[i18nAttribute];
  if (unitElement) {
    const keys = Object.keys(unitElement);
    for (const lang of ['fr', 'en']) {
      if (unitElement[lang]) {
        return unitElement[lang];
      }
      const langKey = keys.filter((key) => lang === key.toLowerCase()).pop();
      if (langKey) {
        return unitElement[langKey];
      }
    }
    if (keys.length) {
      return unitElement[keys[0]];
    }
  }
  return '';
}
