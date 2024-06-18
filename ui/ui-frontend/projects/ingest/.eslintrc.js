module.exports = {
  extends: '../../.eslintrc.json',
  ignorePatterns: ['!**/*'],
  overrides: [
    {
      files: ['*.ts'],
      parserOptions: {
        tsconfigRootDir: __dirname,
        project: ['tsconfig.eslint.json'],
      },
      rules: {
        '@angular-eslint/directive-selector': [
          'error',
          {
            type: 'attribute',
            style: 'camelCase',
          },
        ],
        '@angular-eslint/component-selector': [
          'error',
          {
            type: 'element',
            style: 'kebab-case',
          },
        ],
      },
    },
    {
      files: ['*.html'],
      rules: {},
    },
  ],
};
