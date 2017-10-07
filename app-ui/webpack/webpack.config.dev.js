/* global __dirname */
const path = require('path');
const webpack = require('webpack');

module.exports = exports = Object.create(require('./webpack.base.config.js'));

exports.devtool = 'source-map';
exports.entry = ['webpack/hot/dev-server', 'webpack-dev-server/client?http://localhost:8080'].concat(exports.entry);
exports.plugins = [
    new webpack.ContextReplacementPlugin(
        /angular(\\|\/)core(\\|\/)@angular/,
        path.resolve(__dirname, './app')
    ),
    new webpack.HotModuleReplacementPlugin(),
    // Maps jquery identifiers to the jQuery package (because Bootstrap and other dependencies expects it to be a global variable)
    new webpack.ProvidePlugin({
        jQuery: 'jquery',
        $: 'jquery',
        jquery: 'jquery'
    }),
    new webpack.ProvidePlugin({
        'fetch': 'imports?this=>global!exports?global.fetch!whatwg-fetch'
    })
];