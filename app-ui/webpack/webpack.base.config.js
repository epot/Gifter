/* global __dirname */
var webpack = require('webpack');
var path = require('path');
console.log(__dirname);
var buildPath = path.resolve(__dirname, '../../../public/bundles/');
var nodeModulesPath = path.resolve(__dirname, 'node_modules');

/**
 * Base configuration object for Webpack
 */
var config = {
    entry: [
        './app/main.ts'
    ],
    output: {
        path: buildPath,
        filename: 'bundle.js',
        sourceMapFilename: 'bundle.map',
        publicPath: '/bundles/'
    },
    externals: {
    },
    module: {
        rules: [
            {
                test: /\.css$/,
                use: ['style-loader','css-loader']
            },
            {
                test: /\.less$/,
                use: ['style-loader', 'css-loader', 'less-loader']
            },
            {
                test: /\.(jpg|png|woff|woff2|eot|ttf|gif|svg)$/,
                use: 'url-loader?limit=100000'
            },
            {
                test: /\.svg$/,
                use: 'url-loader?limit=10000&mimetype=image/svg+xml'
            },
            {
                test: /\.es6$/,
                exclude: /node_modules/,
                loader: 'babel',
                query: {
                  presets: ['es2015']
                }
            },
            {
                test: /\.(html)$/,
                use: {
                    loader: 'html-loader',
                    options: {
                    attrs: [':data-src']
                    }
                }
            },
            {
                test: /\.ts(x?)$/,
                exclude: /node_modules/,
                use: [
                    {
                        loader: 'babel-loader',
                    },
                    {
                        loader: 'ts-loader'
                    }
                ]
            }
        ]
    },
    resolve: {
        extensions: ['.ts','.tsx','.js','.json','.css','.html']
    },
    plugins: [
        new webpack.ContextReplacementPlugin(
            /angular(\\|\/)core(\\|\/)@angular/,
            path.resolve(__dirname, './app')
        ),
        new webpack.ProvidePlugin({
            'fetch': 'imports?this=>global!exports?global.fetch!whatwg-fetch'
        }),
        // Maps jquery identifiers to the jQuery package (because Bootstrap and other dependencies expects it to be a global variable)
        new webpack.ProvidePlugin({   
            jQuery: 'jquery',
            $: 'jquery',
            jquery: 'jquery'
        }),
        new webpack.optimize.CommonsChunkPlugin({ name: 'vendor', filename: 'vendor.bundle.js' })
    ]
};

module.exports = config;

