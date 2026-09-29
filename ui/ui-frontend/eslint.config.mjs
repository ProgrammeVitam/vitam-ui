import { defineConfig } from '@eslint/config-helpers';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import js from '@eslint/js';
import { FlatCompat } from '@eslint/eslintrc';
import { readFileSync } from 'fs';
import boundaries from 'eslint-plugin-boundaries';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const compat = new FlatCompat({
  baseDirectory: __dirname,
  recommendedConfig: js.configs.recommended,
  allConfig: js.configs.all
});

const angularConfig = JSON.parse(readFileSync(path.join(__dirname, 'angular.json'), 'utf8'));

export default defineConfig([
  {
    plugins: { boundaries },
    settings: {
      'boundaries/elements': [
        { type: 'library', pattern: 'projects/vitamui-library/**/*' },
        { type: 'app', pattern: 'projects/!(vitamui-library)/**/*', capture: ['project'] }
      ],
      // Resolve extensionless TS imports and the 'vitamui-library' path alias
      // (mapped in tsconfig.json), otherwise cross-project imports stay invisible.
      'import/resolver': {
        typescript: {
          project: ['tsconfig.json', 'projects/*/tsconfig.*.json'],
          noWarnOnMultipleProjects: true
        }
      }
    },
    rules: {
      // No cross-project imports: an app may only import itself or the shared library.
      // The library may only import itself (never an app).
      'boundaries/dependencies': ['error', {
        default: 'disallow',
        policies: [
          {
            from: { element: { type: 'app' } },
            allow: {
              // Same project only (captured folder must match).
              // NB: 'vitamui-library' package imports resolve to dist/ (external),
              // so they are unaffected — only relative paths land on the element.
              to: [
                { element: { type: 'app', captured: { project: '{{ from.element.captured.project }}' } } }
              ]
            },
            message: 'Cross-project imports are forbidden: apps can only import their own project or vitamui-library (found "{{ to.element.captured.project }}").'
          },
          {
            from: { element: { type: 'library' } },
            allow: {
              to: [{ element: { type: 'library' } }]
            },
            message: 'vitamui-library must stay dependency-free of apps: it cannot import "{{ to.element.captured.project }}".'
          },
          {
            // Enforce public entry points: any import resolving inside the
            // library element is a relative path into its sources — apps must
            // use 'vitamui-library' / 'vitamui-library/testing' instead.
            from: { element: { type: 'app' } },
            disallow: {
              to: [{ element: { type: 'library' } }]
            },
            message: 'Import vitamui-library through its public entry points ("vitamui-library", "vitamui-library/testing"), not via relative paths.'
          }
        ]
      }]
    }
  },
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
        prefix: (config.prefix || 'app').replace(/-([a-z])/g, (_, c) => c.toUpperCase()),
        style: 'camelCase',
        type: 'attribute'
      }],
      '@angular-eslint/prefer-standalone': ['warn']
    }
  };
}
