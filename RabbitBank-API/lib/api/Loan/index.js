var express = require('express');
var router = express.Router();

var loan = require("./loan");
var get_debt = require("./get_debt");
var repayment = require("./repayment");
var loan_cancel = require("./loan_cancel");

router.use("/loan", loan);
router.use("/get_debt", get_debt);
router.use("/repayment", repayment);
router.use("/loan_cancel", loan_cancel);

module.exports = router;
