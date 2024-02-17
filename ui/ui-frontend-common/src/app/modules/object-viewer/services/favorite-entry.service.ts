import { DisplayObject } from '../models';

export class FavoriteEntryService {
  public favoriteEntry(displayObject: DisplayObject): [key: string, value: unknown] | null {
    const favoriteEntry: [key: string, value: any] = displayObject.favoriteKeys.reduce(
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

    if (favoriteEntry && displayObject.path) {
      return `${displayObject.path}.${favoriteEntry[0]}`;
    }
    if (favoriteEntry) {
      return `${favoriteEntry[0]}`;
    }

    return null;
  }
}