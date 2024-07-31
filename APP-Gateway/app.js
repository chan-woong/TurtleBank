const express = require('express');
var morgan = require('morgan'); // logging 모듈
// api gateway 모듈
const { createProxyMiddleware } = require('http-proxy-middleware');

const app = express();
const PORT = 3030; // 포트

const INTERNAL_SERVER_URL = 'http://10.0.20.224:3000'; // 내부망(API서버) 주소

app.use(morgan(':method :url :status :res[content-length] - :response-time ms')) // 로그
app.use( // 다른 웹서버에서 수신되는 mydata를 내부 api 서버로 
  '/resp/api/mydata', //
  createProxyMiddleware({
    target: INTERNAL_SERVER_URL + '/api/mydata', // 
    changeOrigin: false,
  }),
);
app.use(
  '/', // 전 범위
  createProxyMiddleware({
    target: INTERNAL_SERVER_URL, // 전 범위
    changeOrigin: false,
  }),
);
// ex) http://127.0.0.1:3030/api/user/login => http://192.168.56.1:3000/api/user/login
app.listen(PORT, () => {
  console.log(`API Gateway is running on port ${PORT}`);
});
