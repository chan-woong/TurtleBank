var express = require('express');
var router = express.Router();

var login = require("./login");
var register = require("./register");
var profile = require("./profile");
var changePassword = require("./changePassword");
var findPass = require("./findPass");
var smsAuth = require("./smsAuth");
var main = require("./main");

router.use('/login', login);
router.use('/register', register);
router.use('/profile', profile);
router.use("/change-password", changePassword);
router.use("/findPass",findPass);
router.use("/smsAuth", smsAuth);
router.use("/main", main);


module.exports = router;
