const express = require("express");
const app = express();
const index = require('./routes/index');
var logger = require('morgan');


global.file_path = "/home/ubuntu/AWS-TurtleBank/file/";
global.different_api = 'http://m.rabbit-bank.com/';

app.use(express.json());

app.use('/api', index);
app.use(logger('dev'));

module.exports = app;
