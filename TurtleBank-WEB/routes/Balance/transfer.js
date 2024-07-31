var express = require('express');
var router = express.Router();
var axios = require("axios");
var {encryptResponse, decryptRequest, decryptEnc} = require("../../middlewares/crypt");
const checkCookie = require("../../middlewares/checkCookie")

/* GET users listing. */
router.get('/', checkCookie, function (req, res, next) {
    res.render("Balance/transfer");
});


router.post('/', checkCookie, function (req, res, next) {
    const cookie = req.cookies.Token

    let json_data = {};

    json_data['from_account'] = parseInt(req.body.from_account);
    json_data['to_account'] = parseInt(req.body.to_account);   //데이터가 숫자로 들어가야 동작함
    json_data['amount'] = parseInt(req.body.amount);

    const en_data = encryptResponse(JSON.stringify(json_data));// 객체를 문자열로 반환 후 암호화

    axios({
        method: "post",
        url: api_url + "/api/balance/transfer",
        headers: {"authorization": "1 " + cookie},
        data: en_data
    }).then((data) => {
        console.log(decryptRequest(data.data));
    });

    // res.header('authorization',token);
    return res.redirect("/transactions/view");
});


module.exports = router;
