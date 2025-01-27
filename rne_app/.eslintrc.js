// https://docs.expo.dev/guides/using-eslint/
module.exports = {
    extends: [
        'airbnb',
        'expo',
        'plugin:react/recommended',
        'plugin:react-native/all',
        'plugin:@typescript-eslint/recommended',
    ],
    ignorePatterns: ['/dist/*', '/app-example/*'],
    env: {
        browser: true,
        es2021: true,
    },
    overrides: [
        // override "simple-import-sort" config
        {
            files: ['*.js', '*.jsx', '*.ts', '*.tsx'],
            rules: {
                'simple-import-sort/imports': [
                    'error',
                    {
                        groups: [
                            ['^react$', '^react-native$'], // react, react-native
                            ['^expo$', '^expo-.*$'], // expo, expo-*
                            ['^@?\\w'], // libraries
                            ['^\\u0000'], // side effect imports
                            ['^(@|components)(/.*|$)'], // internal packages
                            ['^\\.\\.(?!/?$)', '^\\.\\./?$'], // `../*`
                            ['^\\./(?=.*/)(?!/?$)', '^\\.(?!/?$)', '^\\./?$'], // `./*`
                            ['^.+\\.?(css)$'], // css
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
        'react',
        'react-native',
        '@typescript-eslint',
        'simple-import-sort',
    ],
    rules: {
        // base
        'brace-style': ['error', 'stroustrup'],
        'linebreak-style': 'off',
        'import/extensions': 'off',
        'import/prefer-default-export': 'off',
        'import/no-extraneous-dependencies': ['error', { devDependencies: true }],
        indent: ['error', 4, { SwitchCase: 1 }],
        'no-param-reassign': [2, { props: false }],
        'no-underscore-dangle': ['error', { allow: ['__dirname'] }],
        'no-use-before-define': 'off',
        'object-curly-newline': [
            'error',
            {
                ImportDeclaration: { multiline: true, minProperties: 4 },
                ExportDeclaration: { multiline: true, minProperties: 4 },
            },
        ],
        'prefer-destructuring': ['error', { object: true, array: false }],

        // jsx & react
        'react/function-component-definition': [2, { namedComponents: 'arrow-function' }],
        'react/jsx-filename-extension': [1, { extensions: ['.tsx', '.jsx'] }],
        'react/jsx-indent': ['error', 4],
        'react/jsx-indent-props': ['error', 4],
        'react/jsx-one-expression-per-line': 'off',
        'react/prop-types': 'off',
        'react/react-in-jsx-scope': 'off',
        'react/style-prop-object': 'off',

        // react-native
        'react-native/no-unused-styles': 'warn',
        'react-native/split-platform-components': 'warn',
        'react-native/no-inline-styles': 'off',
        'react-native/no-color-literals': 'off',
        'react-native/no-raw-text': ['error', { skip: ['ThemedText'] }],

        // typescript
        '@typescript-eslint/no-unused-vars': ['error', { argsIgnorePattern: '^_' }],
        '@typescript-eslint/explicit-function-return-type': 'off',
        '@typescript-eslint/no-explicit-any': 'warn',

        // import sort
        'simple-import-sort/imports': 'error',
        'simple-import-sort/exports': 'error',
    },
};
