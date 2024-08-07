var express = require('express');
var router = express.Router();
var axios = require("axios");
var {encryptResponse, decryptRequest} = require("../../middlewares/crypt");
const profile = require('../../middlewares/profile');
const checkCookie = require("../../middlewares/checkCookie")


// 사용하지 않는 코드

router.get('/', checkCookie, function (req, res) {
    const cookie = req.cookies.Token;

    profile(cookie).then(pending => {

        axios({           
            method: "post",
            url: api_url + "/api/beneficiary/view", 
            headers: {"authorization": "1 " + cookie}
        }).then((data) => {
            let result = decryptRequest(data.data).data;

            var html_data = "<table><tr><th>친구계좌</th><th>승인여부";

            result.forEach(function (a) {
                html_data += "<tr><td>" + a.beneficiary_account_number + "</td><td>" + a.approved + "</td></tr>";
            })

            html_data += "</table>";
            return res.render("Banking/friend", {html: html_data, pending: pending});
        }).catch(function (error) {
            return res.render("Banking/friend");
        });

    })


})


router.post('/', checkCookie, function (req, res, next) {
    const cookie = req.cookies.Token;
    let {beneficiary_account_number} = req.body;
    const baseData = `{"account_number": ${beneficiary_account_number}}`
    const enData = encryptResponse(baseData);

    axios({
        method: "post",
        url: api_url + "/api/Beneficiary/add", // URL 수정 해야 됨
        headers: {"authorization": "1 " + cookie},
        data: enData
    }).then((data) => {
    });
    return res.redirect("friend");
})

module.exports = router;
