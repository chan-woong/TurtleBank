var express = require('express');
var router = express.Router();

var deleteBoard = require("./deleteBoard");
var editBoard = require("./editBoard");
var getBoard = require("./getBoard");
var viewBoard = require("./viewBoard");
var writeBoard = require("./writeBoard");
var admusrcheckBoard = require("./admusrcheck");
var admcheckBoard = require("./admcheck");
var searchBoard = require("./searchBoard");
var addComment = require("./addComment");

router.use('/deleteBoard', deleteBoard);
router.use('/editBoard', editBoard);
router.use('/getBoard', getBoard);
router.use('/viewBoard', viewBoard);
router.use('/writeBoard', writeBoard);
router.use('/admusrcheck', admusrcheckBoard);
router.use('/admcheck', admcheckBoard);
router.use('/searchBoard', searchBoard);
router.use('/addComment', addComment);

module.exports = router;
