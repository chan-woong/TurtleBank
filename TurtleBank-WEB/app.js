var createError = require('http-errors');
var express = require('express');
var path = require('path');
var cookieParser = require('cookie-parser');
var logger = require('morgan');
var {api_url, file_path, server_url, m_host, host} = require('./config');

var indexRouter = require('./routes/index');
var app = express();

const httpProxy = require('http-proxy');
const proxy = httpProxy.createProxyServer({});

global.api_url = api_url;
global.file_path = file_path;
global.server_url = server_url;

global.m_host = m_host;
global.host = host;

const domainMiddleware = (req, res, next) => {
    const host = req.get('host'); // Full host info including port
    req.host = host; // Attach the host to the request object
	switch (host) {
		case host:
			next();
		break;
		case m_host:
			console.log("모바일 도메인 : http://localhost:4000 포워딩");
			proxy.web(req, res, { target: 'http://3.39.214.111:3030' });
		break;
		default:
			next(createError(404));
		break;
	}
};


app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'ejs');

app.use(domainMiddleware);
app.use(logger('dev'));
app.use(express.json());
app.use(express.urlencoded({extended: false}));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));

app.use('/', indexRouter);

app.use(function (req, res, next) {
	next(createError(404));
});

app.use(function (err, req, res, next) {
	res.locals.message = err.message;
	res.locals.error = req.app.get('env') === 'development' ? err : {};

	res.status(err.status || 500);
	res.render('error');
});

module.exports = app;