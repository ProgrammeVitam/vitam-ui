const svgToMiniDataURI = require('mini-svg-data-uri');
const MomentLocalesPlugin = require('moment-locales-webpack-plugin');

module.exports = {
    module: {
      rules: [
        {
            test: /\.(woff2?|eot|ttf|otf)(\?.*)?$/,
            loader: 'file-loader'
        },
        {
          test: /\.(png|jpg|gif)$/i,
          use: [
            {
              loader: 'url-loader',
              options: {
                limit: false,
              },
            },
          ],
        },
        {
          test: /\.svg$/i,
          use: [
            {
              loader: 'url-loader',
              options: {
                generator: (content) => svgToMiniDataURI(content.toString()),
              },
            },
          ],
        },
      ],
    },
    plugins: [
      new MomentLocalesPlugin({
        localesToKeep: ['fr']
      })
    ]
  };