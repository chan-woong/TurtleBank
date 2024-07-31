var express = require("express");
var router = express.Router();

var list = require("./trade_list");
var send = require("./trade_send");
var friend_list = require("./friend_list");
var user_register = require("./user_register");
var admin = require("./admin");
var membership = require("./membership");
var loan = require("./loan");
var account_list = require("./account_list"); //계좌목록 추가
var mydata = require("./mydata"); //마이데이터
var mydata_auth = require("./mydata_auth"); //마이데이터 auth 테스트
var otherbank_send = require("./otherbank_send");
var mydata_info = require("./mydata_info");


router.use(express.static("public"));
router.use("/admin", admin);
router.use("/user_register", user_register);
router.use("/friend_list", friend_list);
router.use("/list", list);
router.use("/send", send);
router.use("/membership", membership);
router.use("/loan", loan);
router.use("/account_list",account_list); //계좌목록 추가
router.use("/mydata",mydata); //마이데이터 추가
router.use("/mydata_auth",mydata_auth); //마이데이터 추가
router.use("/otherbank_send", otherbank_send);
router.use("/mydata_info", mydata_info);


module.exports = router;
