const fs = require('fs');
const process = require('process');
const i18nDirectory = 'projects/vitamui-library/src/assets/shared-i18n';
const defaultValue = 'TO_TRANSLATE';
const shouldFix = process.argv.includes('--fix');

function flattenKeys(obj, parent, res = []) {
  for (const key of Object.keys(obj)) {
    const propName = parent ? parent + '.' + key : key;
    if (typeof obj[key] === 'object') {
      flattenKeys(obj[key], propName, res);
    } else {
      res.push(propName);
    }
  }
  return res;
}

function sortKeys(x) {
  if (typeof x !== 'object' || !x) return x;
  if (Array.isArray(x)) return x.map(sortKeys);
  return Object.keys(x)
    .sort()
    .reduce((o, k) => ({ ...o, [k]: sortKeys(x[k]) }), {});
}

const keysPerLang = fs
  .readdirSync(i18nDirectory)
  .filter(f => f.endsWith('.json'))
  .map((f) => `${i18nDirectory}/${f}`)
  .reduce((acc, path) => {
    acc[path] = flattenKeys(JSON.parse(fs.readFileSync(path, 'utf8')));
    return acc;
  }, {});

const allKeys = [...new Set(Object.values(keysPerLang).flatMap((keys) => keys))];

let keysAreMissing = false;
Object.entries(keysPerLang).forEach(([lang, keys]) => {
  const missingKeys = allKeys.filter((k) => !keys.includes(k));
  if (!shouldFix && missingKeys.length) {
    keysAreMissing = true;
    console.error(`Missing keys in ${lang}:\n${missingKeys.map((key) => `  - ${key}`).join('\n')}`);
  }
  if (shouldFix) {
    const o = JSON.parse(fs.readFileSync(lang, 'utf8'));
    missingKeys.forEach((flattenKey) => {
      const keys = flattenKey.split('.');
      let tmp = o;
      for (let i = 0; i < keys.length - 1; i++) {
        const key = keys[i];
        tmp[key] = tmp[key] || {};
        tmp = tmp[key];
      }
      tmp[keys[keys.length - 1]] = defaultValue;
    });
    fs.writeFileSync(lang, JSON.stringify(sortKeys(o), null, 2), { encoding: 'utf8' });
  }
});

process.exit(keysAreMissing && !shouldFix ? 1 : 0);
