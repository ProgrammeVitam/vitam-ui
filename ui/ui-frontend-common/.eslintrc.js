module.exports = {
  'root': true,
  'ignorePatterns': [
    'projects/**/*'
  ],
  'overrides': [
    {
      'files': [
        '*.ts'
      ],
      'parserOptions': {
        'project': [
          'tsconfig.json',
          'e2e/tsconfig.json'
        ],
        'createDefaultProgram': true
      },
      'extends': [
        'plugin:@angular-eslint/ng-cli-compat',
        'plugin:@angular-eslint/ng-cli-compat--formatting-add-on',
        'plugin:@angular-eslint/template/process-inline-templates'
      ],
      'rules': {
        '@angular-eslint/component-selector': [
          'error',
          {
            'type': 'element',
            'prefix': 'vitamui-common',
            'style': 'kebab-case'
          }
        ],
        '@angular-eslint/directive-selector': [
          'error',
          {
            'type': 'attribute',
            'prefix': 'vitamuiCommon',
            'style': 'camelCase'
          }
        ],
        '@typescript-eslint/explicit-member-accessibility': [
          'off',
          {
            'accessibility': 'explicit'
          }
        ],
        'arrow-parens': [
          'off',
          'always'
        ],
        'import/order': 'error'
      }
    },
    {
      'files': [
        '*.html'
      ],
      'extends': [
        'plugin:@angular-eslint/template/recommended'
      ],
      'rules': {}
    }
  ]
};
