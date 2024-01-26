module.exports = {
  extends: '../../.eslintrc.js',
  ignorePatterns: ['!**/*'],
  overrides: [
    {
      files: ['*.ts'],
      parserOptions: {
        tsconfigRootDir: __dirname + '/../../',
        project: ['projects/demo/tsconfig.app.json', 'projects/demo/tsconfig.spec.json', 'projects/demo/e2e/tsconfig.json'],
        createDefaultProgram: true,
      },
      rules: {
        '@angular-eslint/component-selector': [
          'error',
          {
            type: 'element',
            prefix: 'demo',
            style: 'kebab-case',
          },
        ],
        '@angular-eslint/directive-selector': [
          'error',
          {
            type: 'attribute',
            prefix: 'demo',
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
