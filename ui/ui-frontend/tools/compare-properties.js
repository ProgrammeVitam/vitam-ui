const fs = require('fs');

const refFile = process.argv[2];
const checkFile = process.argv[3];

if (!refFile || !checkFile) {
    console.log("Usage: node compare-properties.js <reference_file> <check_file>");
    process.exit(1);
}

function parseProperties(filePath) {
    try {
        const content = fs.readFileSync(filePath, 'utf8');
        const lines = content.split(/\r?\n/);
        const keys = new Set();

        lines.forEach(line => {
            const l = line.trim();
            if (!l || l.startsWith('#') || l.startsWith('!')) return;

            const index = l.indexOf('=');
            if (index > 0) {
                let key = l.substring(0, index).trim();
                // Normalization for comparison
                // 1. Remove 'outMessg.' prefix
                // 2. Unescape dots '\.'
                key = key.replace(/^outMessg\./, '').replace(/\\\./g, '.');
                keys.add(key);
            }
        });
        return keys;
    } catch (e) {
        console.error(`Error reading file ${filePath}: ${e.message}`);
        process.exit(1);
    }
}

console.log(`Comparing:\n  Ref : ${refFile}\n  Check: ${checkFile}\n`);

const refKeys = parseProperties(refFile);
const checkKeys = parseProperties(checkFile);

const missingKeys = [...refKeys].filter(k => !checkKeys.has(k));
const extraKeys = [...checkKeys].filter(k => !refKeys.has(k));

if (missingKeys.length > 0) {
    console.log(`❌ ${missingKeys.length} missing keys in the check file:`);
    missingKeys.sort().forEach(k => console.log(`  - ${k}`));
} else {
    console.log(`✅ No missing keys found compared to the reference.`);
}

if (extraKeys.length > 0) {
    console.log(`\nℹ️  ${extraKeys.length} additional keys in the check file:`);
    // Only show first 10 if there are many
    extraKeys.sort().slice(0, 10).forEach(k => console.log(`  - ${k}`));
    if (extraKeys.length > 10) console.log(`  ... (+ ${extraKeys.length - 10} more)`);
}
