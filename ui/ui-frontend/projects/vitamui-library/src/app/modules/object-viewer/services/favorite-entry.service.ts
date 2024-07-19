import { Injectable } from '@angular/core';
import { DisplayObject } from '../models';

@Injectable({
  providedIn: 'root',
})
export class FavoriteEntryService {
  public favoriteEntry(displayObject: DisplayObject): [key: string, value: unknown] | null {
    const favoriteEntry: [key: string, value: any] = displayObject?.displayRule?.ui?.favoriteKeys?.reduce(
      (entry, key) => {
        const value = displayObject.value[key];

        return value ? [key, value] : entry;
      },
      null as [key: string, value: any],
    );

    return favoriteEntry;
  }

  public favoritePath(displayObject: DisplayObject): string | null {
    const favoriteEntry = this.favoriteEntry(displayObject);

    if (favoriteEntry && displayObject?.displayRule?.ui?.label) {
      return `${displayObject.children.find((object) => object.key === favoriteEntry[0]).displayRule.ui.label}`;
    }
    if (favoriteEntry && displayObject.path) {
      return `${displayObject.path}.${favoriteEntry[0]}`;
    }
    if (favoriteEntry) {
      return `${favoriteEntry[0]}`;
    }

    return null;
  }
}
