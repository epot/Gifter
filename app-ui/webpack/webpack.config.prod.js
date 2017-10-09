var webpack = require('webpack');
const path = require('path');

module.exports = exports = Object.create(require('./webpack.base.config.js'));

exports.plugins = [
    // Maps jquery identifiers to the jQuery package (because Bootstrap and other dependencies expects it to be a global variable)
    new webpack.ProvidePlugin({
        jQuery: 'jquery',
        $: 'jquery',
        jquery: 'jquery'
    }),
    new webpack.optimize.UglifyJsPlugin({
        minimize: true,
        compressor: { warnings: false },
        // https://github.com/angular/angular/issues/10618
        mangle: {
            keep_fnames: true
        }
    })
];