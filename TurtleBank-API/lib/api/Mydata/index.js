
var express = require('express');
var router = express.Router();

var view = require("./view");
var mydata_sms = require("./mydata_sms");
var mydata_sms_check = require("./mydata_sms_check");
var b_api = require("./b_api");

router.use('/view', view);
router.use('/mydata_sms', mydata_sms);
router.use('/b_api', b_api);
var req_account = require("./req_account");
var res_account = require("./res_account");
var send_btoa = require("./send_btoa");
var send_btob = require("./send_btob");
var b_to_b = require("./b_to_b");
var b_to_a = require("./b_to_a");


router.use('/view', view);
router.use('/req_account', req_account);
router.use('/res_account', res_account);
router.use('/send_btoa', send_btoa);
router.use('/b_to_b', b_to_b);
router.use('/send_btob', send_btob);
router.use('/b_to_a', b_to_a);
router.use('/b_api', b_api);
router.use('/mydata_sms_check', mydata_sms_check);


module.exports = router;
