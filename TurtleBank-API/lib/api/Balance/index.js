var express = require('express');
var router = express.Router();

var view = require("./view");
var transfer = require("./transfer");
var total = require("./total");
var check_pw = require("./check_pw");

router.use('/total',total);
router.use('/view', view);
router.use('/transfer', transfer);
router.use('/check_pw', check_pw);

module.exports = router;
