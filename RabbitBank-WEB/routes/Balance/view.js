var express = require('express');
var router = express.Router();
const checkCookie = require("../../middlewares/checkCookie")

/* GET users listing. */
router.get('/', checkCookie, function (req, res, next) {
    res.render("Balanceview");
});

module.exports = router;
