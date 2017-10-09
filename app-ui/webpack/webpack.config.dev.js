/* global __dirname */
const path = require('path');
const webpack = require('webpack');
const { AotPlugin } = require('@ngtools/webpack');

module.exports = exports = Object.create(require('./webpack.base.config.js'));

exports.devtool = 'source-map';
exports.entry = ['webpack/hot/dev-server', 'webpack-dev-server/client?http://localhost:8080'].concat(exports.entry);
exports.plugins = [
    new webpack.ContextReplacementPlugin(
        /angular(\\|\/)core(\\|\/)@angular/,
        path.resolve(__dirname, './app')
    ),
    // Maps jquery identifiers to the jQuery package (because Bootstrap and other dependencies expects it to be a global variable)
    new webpack.ProvidePlugin({
        jQuery: 'jquery',
        $: 'jquery',
        jquery: 'jquery'
    }),
    new webpack.HotModuleReplacementPlugin(),
    new webpack.ProvidePlugin({
        'fetch': 'imports?this=>global!exports?global.fetch!whatwg-fetch'
    }),
    new AotPlugin({
        "mainPath": "app/main.ts",
        "i18nFile": "app/locale/messages.fr.xlf",
        "i18nFormat": "xlf",
        "locale": "fr",
        "replaceExport": false,
        "exclude": [],
        "tsConfigPath": "tsconfig.json"
    })
];