const { defineConfig } = require('@vue/cli-service')
const MonacoWebpackPlugin = require('monaco-editor-webpack-plugin')

module.exports = defineConfig({
  transpileDependencies: true,

  chainWebpack: (config) => {
    config.plugin('monaco').use(new MonacoWebpackPlugin())
  },
  devServer: {
    proxy: {
      '/project': {
        target: 'http://localhost:9966',
      },
      '/git': {
        target: 'http://localhost:9966',
      }
    }
  }
})
