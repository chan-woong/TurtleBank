var express = require('express');
var router = express.Router();

var view = require("./view");
var create = require("./create");


router.use('/view', view);
router.use('/create',create);


module.exports = router;

