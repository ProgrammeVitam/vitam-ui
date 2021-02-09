// can be completed at need
export enum MinLangString {
    fr = 'fr',
    en = 'en',
}

// can be completed at need
export enum FullLangString {
    FRENCH = 'FRENCH',
    ENGLISH = 'ENGLISH',
}

// can be completed at need
export function getFullLangString(minLang: MinLangString): FullLangString {
    switch (minLang) {
        case MinLangString.fr:
        return FullLangString.FRENCH;
        case MinLangString.en:
        return FullLangString.ENGLISH;
    }
}