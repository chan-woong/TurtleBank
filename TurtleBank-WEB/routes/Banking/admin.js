var express = require('express');
var router = express.Router();
const axios = require("axios");
const Response = require("../../middlewares/Response")
const {decryptRequest, encryptResponse} = require("../../middlewares/crypt")
const profile = require("../../middlewares/profile");
const checkCookie = require("../../middlewares/checkCookie")
const IpCheck = require("../../middlewares/IpCheck")

/* GET users listing. */
router.get('/', [checkCookie, IpCheck], function (req, res, next) {
    const cookie = req.cookies.Token

    profile(cookie).then(pending => {
        axios({
            method: "post",
            url: api_url + "/api/beneficiary/pending",
            headers: {"authorization": "1 " + cookie}
        }).then((data) => {
            let html = ""
            const resStatus = decryptRequest(data.data).status;
            const resData = decryptRequest(data.data).data;

            if (resStatus.code === 200) {
                if (resData.length === 0) {
                    html += "<h2>승인할 목록이 없습니다.</h2>"
                } else {
                    html +=
                        "                        <thead>\n" +
                        "                        <tr>\n" +
                        "                            <th>id</th>\n" +
                        "                            <th>account_number</th>\n" +
                        "                            <th>beneficiary_account_number</th>\n" +
                        "                            <th>approve</th>\n" +
                        "                        </tr>\n" +
                        "                        </thead>\n"
                    resData.forEach(x => {
                        html += `<tbody>
                            <tr>
                                <td>${x.id}</td>
                                <td>${x.account_number}</td>
                                <td>${x.beneficiary_account_number}</td>
                                <td><a href="/bank/admin/approve?id=${x.id}" class="btn btn-primary btn-user btn-block">
                승인
            </a></td>
                            </tr>
                            </tbody>`
                    })
                }
            } else {
                html += "<h2>관리자가 아닙니다.</h2>"
            }

            res.render("Banking/admin", {html: html, pending: pending, select: "admin"})
        })
    })
});

router.get('/approve', [checkCookie, IpCheck], function (req, res, next) {
    const cookie = req.cookies.Token
    const id = req.query.id;
    const baseData = `{"id": "${id}"}`
    const enData = encryptResponse(baseData);
    axios({
        method: "post",
        url: api_url + "/api/beneficiary/approve",
        headers: {"authorization": "1 " + cookie},
        data: enData
    }).then((data) => {
        return res.redirect("/bank/admin")
    })
});

module.exports = router;
