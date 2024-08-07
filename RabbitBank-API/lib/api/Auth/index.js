var express = require('express');
var router = express.Router();

var check = require("./check");
var admcheck = require("./admcheck");

router.use('/check', check);
router.use('/admcheck', admcheck);

module.exports = router;
