var express = require('express');
var router = express.Router();

var main = require("./main");
var watchVideo = require("./watchVideo")

router.use("/", main)
router.use("/watchVideo", watchVideo)

module.exports = router;