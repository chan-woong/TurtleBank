var express = require('express');
var router = express.Router();

var deleteBoard = require("./deleteBoard");
var editBoard = require("./editBoard");
var getBoard = require("./getBoard");
var viewBoard = require("./viewBoard");
var writeBoard = require("./writeBoard");
var searchBoard = require("./searchBoard");
var admcheck = require("./admcheck");
var upload = require("./upload");
var download = require("./download");
var mobileupload = require("./mobileupload");

router.use('/mobileupload', mobileupload);
router.use('/deleteBoard', deleteBoard);
router.use('/editBoard', editBoard);
router.use('/getBoard', getBoard);
router.use('/viewBoard', viewBoard);
router.use('/writeBoard', writeBoard);
router.use('/searchBoard', searchBoard);
router.use('/admcheck', admcheck);
router.use('/upload', upload);
router.use('/download', download);


module.exports = router;
