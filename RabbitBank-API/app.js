const express = require("express");
const app = express();
const index = require('./routes/index');
var logger = require('morgan');


// global.file_path = "../file/";
global.file_path = "/home/ubuntu/AWS-RabbitBank/file/";
global.different_api = 'http://m.turtle-bank.com';

app.use(express.json());

app.use('/api', index);
app.use(logger('dev'));

module.exports = app;
