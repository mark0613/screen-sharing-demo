/* eslint-disable import/no-commonjs */

module.exports = {
    extends: [
        'airbnb-base',
    ],
    ignorePatterns: [],
    env: {
        browser: true,
        es2021: true,
    },
    overrides: [
        // override "simple-import-sort" config
        {
            files: ['*.js', '*.ts'],
            rules: {
                'simple-import-sort/imports': [
                    'error',
                    {
                        groups: [
                            ['^@?\\w'], // libraries
                            ['^\\u0000'], // side effect imports
                            ['^(@|components)(/.*|$)'], // internal packages
                            ['^\\.\\.(?!/?$)', '^\\.\\./?$'], // `../*`
                            ['^\\./(?=.*/)(?!/?$)', '^\\.(?!/?$)', '^\\./?$'], // `./*`
                        ],
                    },
                ],
            },
        },
    ],
    parserOptions: {
        ecmaVersion: 'latest',
        sourceType: 'module',
    },
    plugins: [
        'simple-import-sort',
    ],
    rules: {
        // base
        'brace-style': ['error', 'stroustrup'],
        'linebreak-style': 'off',
        'import/extensions': 'off',
        'import/prefer-default-export': 'off',
        'import/no-commonjs': 'error',
        'import/no-extraneous-dependencies': ['error', { devDependencies: true }],
        indent: ['error', 4, { SwitchCase: 1 }],
        'no-param-reassign': [2, { props: false }],
        'no-underscore-dangle': ['error', { allow: ['__dirname', '__filename'] }],
        'no-use-before-define': 'off',
        'object-curly-newline': [
            'error',
            {
                ImportDeclaration: { multiline: true, minProperties: 4 },
                ExportDeclaration: { multiline: true, minProperties: 4 },
            },
        ],
        'prefer-destructuring': ['error', { object: true, array: false }],

        // import sort
        'simple-import-sort/imports': 'error',
        'simple-import-sort/exports': 'error',
    },
};
