import fs from 'fs';
import { join } from 'path';
import _ from 'lodash';
import glob from 'glob';

const languages = ['fr', 'en'];

const projectsPath = `../../../../`;

languages.forEach((language) => {
  const jsonFiles = [`./${language}.json`, ...glob(join(projectsPath, `/**/i18n/${language}.json`), { sync: true })];

  const merged = _.merge(...jsonFiles.map((jsonFile) => JSON.parse(fs.readFileSync(jsonFile, { encoding: 'utf8' }))));
  fs.writeFileSync(`./${language}_merged.json`, JSON.stringify(merged, null, 2), { encoding: 'utf8' });
});
