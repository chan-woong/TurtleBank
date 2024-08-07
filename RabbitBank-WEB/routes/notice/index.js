var express = require('express');
var router = express.Router();

var deleteBoard = require("./deleteBoard");
var editBoard = require("./editBoard");
var getBoard = require("./getBoard");
var viewBoard = require("./viewBoard");
var writeBoard = require("./writeBoard");
var searchBoard = require("./searchBoard");
var download = require("./download");

router.use('/deleteBoard', deleteBoard);
router.use('/editBoard', editBoard);
router.use('/getBoard', getBoard);
router.use('/viewBoard', viewBoard);
router.use('/writeBoard', writeBoard);
router.use('/searchBoard', searchBoard)
router.use('/download', download);


module.exports = router;
