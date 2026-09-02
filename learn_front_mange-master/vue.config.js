module.exports = {
	transpileDependencies: true,
	publicPath:'/',
	outputDir: 'dist', // 输出文件目录：在npm run build时，生成文件的目录名称
  	assetsDir: 'assets', // 放置生成的静态资源 (js、css、img、fonts) 的 (相对于 outputDir 的) 目录
	devServer: {
		host: '127.0.0.1',
		port: 3000, // 端口
		open: true,
		headers: {
			'Cache-Control': 'no-cache',
		},
		proxy: {
			'/img': {
				target: 'http://localhost:8080',
				changeOrigin: true,
				logLevel: 'debug',
			},
			'/video': {
				target: 'http://localhost:8080',
				changeOrigin: true,
			},
			'/file': {
				target: 'http://localhost:8080',
				changeOrigin: true,
			},
		},
	},
	/* 修改html标题 */
	pages: {
		index: {
			entry: 'src/main.js',
			template: 'public/index.ejs',
			title: '智学云途 · 管理端'
		}
	},
}
