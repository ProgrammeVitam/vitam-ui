module.exports = {
  extends: '../../.eslintrc.js',
  ignorePatterns: ['!**/*'],
  overrides: [
    {
      files: ['*.ts'],
      parserOptions: {
        tsconfigRootDir: __dirname + '/../../',
        project: [
          'projects/referential/tsconfig.app.json',
          'projects/referential/tsconfig.spec.json',
          'projects/referential/e2e/tsconfig.json',
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
