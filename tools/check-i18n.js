// Script to compare keys between translation JSON files across all projects
const fs = require('fs');
const path = require('path');

// Function to flatten a JSON object
function flattenObject(obj, prefix = '') {
  return Object.keys(obj).reduce((acc, k) => {
    const pre = prefix.length ? prefix + '.' : '';
    if (typeof obj[k] === 'object' && obj[k] !== null && !Array.isArray(obj[k])) {
      Object.assign(acc, flattenObject(obj[k], pre + k));
    } else {
      acc[pre + k] = obj[k];
    }
    return acc;
  }, {});
}

// Track if any errors are found
let errorsFound = false;

// Function to compare two JSON files
function compareJsonFiles(frPath, enPath, projectName, dirType) {
  try {
    const frJson = require(frPath);
    const enJson = require(enPath);

    const flatFr = flattenObject(frJson);
    const flatEn = flattenObject(enJson);

    const missingInEn = Object.keys(flatFr).filter(key => !flatEn.hasOwnProperty(key));
    const missingInFr = Object.keys(flatEn).filter(key => !flatFr.hasOwnProperty(key));

    console.log(`\n=== Project: ${projectName} (${dirType}) ===`);

    if (missingInEn.length > 0) {
      console.log(`Keys missing in English file (${enPath}):`);
      missingInEn.forEach(key => console.log(`  - ${key}`));
      errorsFound = true;
    } else {
      console.log('No keys missing in English file.');
    }

    if (missingInFr.length > 0) {
      console.log(`Keys missing in French file (${frPath}):`);
      missingInFr.forEach(key => console.log(`  - ${key}`));
      errorsFound = true;
    } else {
      console.log('No keys missing in French file.');
    }
  } catch (error) {
    console.error(`Error comparing files for ${projectName} (${dirType}):`, error.message);
    errorsFound = true;
  }
}

// Function to check a specific i18n directory
function checkI18nDirectory(i18nDir, projectName, dirType) {
  if (fs.existsSync(i18nDir)) {
    const frPath = path.join(i18nDir, 'fr.json');
    const enPath = path.join(i18nDir, 'en.json');

    // Check if both files exist
    if (fs.existsSync(frPath) && fs.existsSync(enPath)) {
      compareJsonFiles(frPath, enPath, projectName, dirType);
      return true;
    } else {
      console.log(`\n=== Project: ${projectName} (${dirType}) ===`);
      console.log('Incomplete translation files:');
      if (!fs.existsSync(frPath)) console.log('  - fr.json missing');
      if (!fs.existsSync(enPath)) console.log('  - en.json missing');
      errorsFound = true;
      return false;
    }
  }
  return false;
}

// Main function to scan directories
function scanProjects() {
  const projectsDir = path.resolve(__dirname, '../ui/ui-frontend/projects'); // projects/ directory

  // Read all subdirectories in projects/
  const projects = fs.readdirSync(projectsDir, { withFileTypes: true })
    .filter(dirent => dirent.isDirectory())
    .map(dirent => dirent.name);

  console.log(`Analyzing ${projects.length} projects...`);

  // Process each project
  projects.forEach(project => {
    const i18nDir = path.join(projectsDir, project, 'src', 'assets', 'i18n');
    const sharedI18nDir = path.join(projectsDir, project, 'src', 'assets', 'shared-i18n');

    const i18nFound = checkI18nDirectory(i18nDir, project, 'i18n');
    const sharedI18nFound = checkI18nDirectory(sharedI18nDir, project, 'shared-i18n');

    if (!i18nFound && !sharedI18nFound) {
      console.log(`\n=== Project: ${project} ===`);
      console.log('No i18n or shared-i18n directories found.');
    }
  });

  // Exit with code 1 if any errors were found
  if (errorsFound) {
    console.log('\nTranslation issues found! Please fix the missing keys.');
    process.exit(1);
  } else {
    console.log('\nAll translation files are in sync.');
  }
}

// Execute the script
scanProjects();
