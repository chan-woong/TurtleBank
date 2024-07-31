var express = require('express');
var router = express.Router();

var login = require("./login");
var signup = require("./signup");
var friend = require("./friend");
var changePass = require("./changePass");
var logout = require("./logout");
var findPass = require("./findPass");
var smsAuth = require("./smsAuth");

router.use(express.static('public'));

router.use('/login', login);
router.use('/signup', signup);
router.use('/friend', friend);
router.use('/changePass', changePass);
router.use('/logout', logout);
router.use('/findPass', findPass);
router.use('/smsAuth', smsAuth);

module.exports = router;