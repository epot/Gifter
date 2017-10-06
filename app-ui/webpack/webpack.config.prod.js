var webpack = require('webpack');

module.exports = exports = Object.create(require('./webpack.base.config.js'));

exports.plugins = [
    new webpack.optimize.UglifyJsPlugin({
        minimize: true,
        compressor: { warnings: false },
        // https://github.com/angular/angular/issues/10618
        mangle: {
            keep_fnames: true
        }
    })
];