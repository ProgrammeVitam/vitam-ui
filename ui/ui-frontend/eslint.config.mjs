import { defineConfig } from '@eslint/config-helpers';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import js from '@eslint/js';
import { FlatCompat } from '@eslint/eslintrc';
import { readFileSync } from 'fs';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const compat = new FlatCompat({
  baseDirectory: __dirname,
  recommendedConfig: js.configs.recommended,
  allConfig: js.configs.all
});

const angularConfig = JSON.parse(readFileSync(path.join(__dirname, 'angular.json'), 'utf8'));

export default defineConfig([
  ...Object.values(angularConfig.projects).map((projectConfig) => computeConfig(projectConfig)),
  {
    files: ['**/*.html'],
    extends: compat.extends('plugin:@angular-eslint/template/recommended'),
    rules: {}
  }
]);

function computeConfig(config) {
  return {
    files: [path.join(config.root, '/**/*.ts')],
    extends: compat.extends(
      'plugin:@angular-eslint/recommended',
      'plugin:@angular-eslint/template/process-inline-templates'
    ),
    languageOptions: {
      ecmaVersion: 5,
      sourceType: 'script',
      parserOptions: {
        project: ['tsconfig.json'],
        createDefaultProgram: true
      }
    },
    rules: {
      '@angular-eslint/component-selector': ['error', {
        prefix: config.prefix || 'app',
        style: 'kebab-case',
        type: 'element'
      }],
      '@angular-eslint/directive-selector': ['error', {
        prefix: config.prefix || 'app',
        style: 'camelCase',
        type: 'attribute'
      }],
      '@angular-eslint/prefer-standalone': ['warn']
    }
  };
}
