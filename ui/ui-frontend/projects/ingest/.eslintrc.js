module.exports = {
  extends: '../../.eslintrc.js',
  ignorePatterns: ['!**/*'],
  overrides: [
    {
      files: ['*.ts'],
      parserOptions: {
        tsconfigRootDir: __dirname + '/../../',
        project: ['projects/ingest/tsconfig.app.json', 'projects/ingest/tsconfig.spec.json'],
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
