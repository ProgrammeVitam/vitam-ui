module.exports = {
  extends: '../../.eslintrc.js',
  ignorePatterns: ['!**/*'],
  overrides: [
    {
      files: ['*.ts'],
      parserOptions: {
        tsconfigRootDir: __dirname + '/../../',
        project: [
          'projects/archive-search/tsconfig.app.json',
          'projects/archive-search/tsconfig.spec.json',
          'projects/archive-search/e2e/tsconfig.json',
        ],
        createDefaultProgram: true,
      },
      rules: {
        '@angular-eslint/component-selector': [
          'error',
          {
            type: 'element',
            prefix: 'app',
            style: 'kebab-case',
          },
        ],
        '@angular-eslint/directive-selector': [
          'error',
          {
            type: 'attribute',
            prefix: 'app',
            style: 'camelCase',
          },
        ],
        '@typescript-eslint/explicit-member-accessibility': [
          'off',
          {
            accessibility: 'explicit',
          },
        ],
        'arrow-parens': ['off', 'always'],
        'import/order': 'error',
      },
    },
    {
      files: ['*.html'],
      rules: {},
    },
  ],
};
