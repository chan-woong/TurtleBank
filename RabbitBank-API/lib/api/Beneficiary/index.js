var express = require('express');
var router = express.Router();

var add = require("./add");
var approve = require("./approve");
var pending = require("./pending");
var view = require("./view");
var delete2 = require("./delete");
var check = require("./check");
var ceiling = require("./ceiling");
var account = require("./account");


router.use("/add", add);
router.use("/approve", approve);
router.use("/pending", pending);
router.use("/view", view); // Give options on FE to see pending list as well
router.use("/delete", delete2);
router.use("/check", check);
router.use("/ceiling", ceiling);
router.use("/account", account);


module.exports = router;
