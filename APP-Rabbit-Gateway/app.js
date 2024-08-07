const express = require('express');
var morgan = require('morgan'); // logging 모듈
// api gateway 모듈
const { createProxyMiddleware } = require('http-proxy-middleware');

const app = express();
const PORT = 3030; // 포트
//
const INTERNAL_SERVER_URL = 'http://20.0.20.11:4000'; // 내부망(API서버) 주소
const DIFFERENT_SERVER = 'http://m.turtle-bank.com'; // rabbit에서는 m.turtle-bank.com:3030 로 수정

app.use(morgan(':method :url :status :res[content-length] - :response-time ms')) // 로그
app.use( // 다른 웹서버에서 수신되는 mydata를 내부 api 서버로 
  '/resp/api/mydata', //
  createProxyMiddleware({
    target: INTERNAL_SERVER_URL + '/api/mydata', // 
    changeOrigin: false,
  }),
);
app.use( // 송신할 mydata를 내 api 서버에서 다른 웹서버로
  '/api/mydata', //
  createProxyMiddleware({
    target: DIFFERENT_SERVER + '/resp/api/mydata', // 
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

//turtle-bank.com:3030
//m.turtle-bank.com:3030
