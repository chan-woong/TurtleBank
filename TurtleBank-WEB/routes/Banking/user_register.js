var express = require('express');
var router = express.Router();
var axios = require("axios");
const profile = require("../../middlewares/profile")
const {encryptResponse} = require("../../middlewares/crypt");
const checkCookie = require("../../middlewares/checkCookie")

router.get("/", checkCookie, async (req, res) => {
    const cookie = req.cookies.Token;
    profile(cookie).then(data => {
        res.render("Banking/user_register.ejs", {pending: data, select: "user_register"})
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
        // 데이터 안씀
    })

    return res.redirect("/bank/friend_list");
})


module.exports = router;