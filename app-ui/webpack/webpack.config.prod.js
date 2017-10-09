var webpack = require('webpack');
const path = require('path');
const { AotPlugin } = require('@ngtools/webpack');

module.exports = exports = Object.create(require('./webpack.base.config.js'));

exports.plugins = [
    new webpack.optimize.UglifyJsPlugin({
        minimize: true,
        compressor: { warnings: false },
        // https://github.com/angular/angular/issues/10618
        mangle: {
            keep_fnames: true
        }
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