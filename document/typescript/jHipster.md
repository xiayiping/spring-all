# web pack

This `webpack.common.js` file is part of a **JHipster** application, used for configuring Webpack, a module bundler, to build and optimize the front-end application. It defines settings and plugins that are common for both development and production environments. Below is a detailed analysis of the file:

---

## **Key Features of the File**
1. **Environment Detection**:
   - The file detects whether the current build is for development (`options.env === 'development'`) or production mode.
   - This is used to apply environment-specific configurations, such as logging levels or optimizations.

2. **Caching**:
   - Webpack's **filesystem caching** is enabled to speed up builds by storing processed modules and configuration details in the cache directory (`../target/webpack`).
   - The `buildDependencies` option ensures the cache is invalidated when key files (e.g., `webpack.dev.js`, `environment.js`) change.

3. **TypeScript Support**:
   - The `ts-loader` is used to process TypeScript files, with `thread-loader` enabled for faster builds by utilizing multiple CPU cores.
   - The `transpileOnly: true` option skips type checking during the build process to improve performance. Type checking is instead delegated to the `ForkTsCheckerWebpackPlugin`.

4. **Module Resolution**:
   - Extensions like `.js`, `.jsx`, `.ts`, `.tsx`, and `.json` are automatically resolved when importing files.
   - TypeScript path aliases (defined in `tsconfig.json`) are mapped to Webpack aliases using `utils.mapTypescriptAliasToWebpackAlias()`.

5. **Rules for File Processing**:
   - A rule is defined for processing `.tsx?` files (TypeScript or TypeScript React files) using `ts-loader`.
   - A commented-out rule for `source-map-loader` is mentioned, which is currently disabled due to an issue with the `@reduxjs/toolkit` package (related to [Issue #16116](https://github.com/jhipster/generator-jhipster/issues/16116)).

6. **Plugins**:
   A variety of Webpack plugins are used for different purposes:

   ### **Core Plugins**
   - **`EnvironmentPlugin`**:
     - Sets environment variables like `LOG_LEVEL` dynamically based on the build mode.
     - These variables are injected into the application code.

   - **`DefinePlugin`**:
     - Injects constants into the application, such as:
       - `I18N_HASH`: A hash of the i18n files used for cache-busting.
       - `DEVELOPMENT`: Whether the build is in development mode.
       - `VERSION` and `SERVER_API_URL`: App version and API base URL.

   - **`ESLintPlugin`**:
     - Adds linting for TypeScript files during the build process.

   - **`ForkTsCheckerWebpackPlugin`**:
     - Runs type checking and linting in a separate process to avoid slowing down the main build.

   - **`CopyWebpackPlugin`**:
     - Copies static assets into the output directory. The following files are copied:
       - Swagger UI files from the `swagger-ui-dist` package, with `index.html` excluded.
       - A minified Axios library (`axios.min.js`).
       - Swagger UI customizations and other static assets like `robots.txt`, `favicon.ico`, and `manifest.webapp`.

   - **`HtmlWebpackPlugin`**:
     - Generates the `index.html` file and injects bundled JavaScript and CSS into the `<body>` tag.

   - **`MergeJsonWebpackPlugin`**:
     - Combines i18n JSON files for different languages into single JSON files for each language (`en.json`, `zh-cn.json`, etc.).

   ### **JHipster Needles**
   - **jhipster-needle-add-assets-to-webpack**:
     - A placeholder for adding or removing third-party assets.
   - **jhipster-needle-i18n-language-webpack**:
     - A placeholder for adding or removing languages in the i18n system.
   - **jhipster-needle-add-webpack-config**:
     - A placeholder for adding custom Webpack configuration.

---

## **Detailed Analysis of Key Sections**

### **1. TypeScript Loader Rule**
```javascript
const getTsLoaderRule = () => {
  return [
    {
      loader: 'thread-loader',
      options: {
        workers: require('os').cpus().length - 1,
      },
    },
    {
      loader: 'ts-loader',
      options: {
        transpileOnly: true,
        happyPackMode: true,
      },
    },
  ];
};
```
- **Thread Loader**:
  - Improves build performance by parallelizing the processing of files across CPU cores.
- **TS Loader**:
  - Processes TypeScript files and transpiles them to JavaScript.
  - `transpileOnly: true` skips type checking for faster builds, which is handled later by `ForkTsCheckerWebpackPlugin`.

---

### **2. File Caching**
```javascript
cache: {
  type: 'filesystem',
  cacheDirectory: path.resolve(__dirname, '../target/webpack'),
  buildDependencies: {
    config: [
      __filename,
      path.resolve(__dirname, `webpack.${development ? 'dev' : 'prod'}.js`),
      ...
    ],
  },
}
```
- Enables persistent caching to speed up rebuilds.
- Invalidation occurs when key configuration files change.

---

### **3. CopyWebpackPlugin**
```javascript
new CopyWebpackPlugin({
  patterns: [
    {
      context: require('swagger-ui-dist').getAbsoluteFSPath(),
      from: '*.{js,css,html,png}',
      to: 'swagger-ui/',
      globOptions: { ignore: ['**/index.html'] },
    },
    {
      from: path.join(path.dirname(require.resolve('axios/package.json')), 'dist/axios.min.js'),
      to: 'swagger-ui/',
    },
    { from: './src/main/webapp/swagger-ui/', to: 'swagger-ui/' },
    { from: './src/main/webapp/content/', to: 'content/' },
    { from: './src/main/webapp/favicon.ico', to: 'favicon.ico' },
    ...
  ],
}),
```
- Copies Swagger UI files from the `swagger-ui-dist` package and custom assets into the output directory.
- Excludes `index.html` to avoid overwriting the main app's `index.html`.

---

### **4. MergeJsonWebpackPlugin**
```javascript
new MergeJsonWebpackPlugin({
  output: {
    groupBy: [
      { pattern: './src/main/webapp/i18n/en/*.json', fileName: './i18n/en.json' },
      { pattern: './src/main/webapp/i18n/zh-cn/*.json', fileName: './i18n/zh-cn.json' },
      { pattern: './src/main/webapp/i18n/zh-tw/*.json', fileName: './i18n/zh-tw.json' },
      // jhipster-needle-i18n-language-webpack
    ],
  },
}),
```
- Combines all JSON files for each language into a single JSON file for easy consumption by the front-end.

---

### **5. Disabled Source-Map Loader**
```javascript
/*
Disabled due to https://github.com/jhipster/generator-jhipster/issues/16116
Can be enabled with @reduxjs/toolkit@>1.6.1
{
  enforce: 'pre',
  test: /\.jsx?$/,
  loader: 'source-map-loader'
}
*/
```
- The `source-map-loader` is disabled due to an issue with the `@reduxjs/toolkit` package (see [Issue #16116](https://github.com/jhipster/generator-jhipster/issues/16116)).
- This can be re-enabled when upgrading `@reduxjs/toolkit` to a version above `1.6.1`.

---

## **Summary**
This `webpack.common.js` file is highly optimized and tailored for use in a JHipster application. It:
- Supports TypeScript with multi-threaded builds.
- Enables persistent caching for faster rebuilds.
- Handles static assets and i18n file merging.
- Integrates Swagger UI into the app.
- Provides hooks (`needles`) for JHipster-specific customizations.

The configuration is modular, environment-aware, and extensible, making it ideal for both development and production builds.