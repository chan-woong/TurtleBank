var express = require('express');
var router = express.Router();
const Response = require("../../middlewares/Response");
const {decryptRequest, decryptEnc, encryptResponse} = require("../../middlewares/crypt");
const axios = require("axios");
const sha256 = require("js-sha256")


/* GET users listing. */
router.get('/', function (req, res, next) {          // 인증번호 입력 페이지 렌더링
    var username = req.query.username;
    res.render("temp/smsAuth", {select: "smsAuth", username: username});
});

router.post('/', (req, res) => {          
    const username = req.body.username;
    const authnum = req.body.authnum;
    const new_password = req.body.next_new_password;
    const check_password = req.body.check_password;
    const sha256Pass = sha256(new_password);
    const sha256Newpass = sha256(check_password);
    const baseData = `{"username": "${username}", "authnum" : "${authnum}", "next_new_password" : "${sha256Pass}","check_password" : "${sha256Newpass}"}`;
    let resStatus = ""
    let resMessage = ""

    axios({          // smsAuth를 확인하기 위한 api로 req
        method: "post",
        url: api_url + "/api/User/smsAuth",
        data: encryptResponse(baseData)
    }).then((data) => {
        resStatus = decryptRequest(data.data).status
        resMessage = decryptRequest(data.data).data.message

        if (resStatus.code == 200) {
            return res.send(`<script>alert('비밀번호가 변경되었습니다.');location.href = \"/user/login\";</script>`);
        } else {
            res.render("temp/findPass", {select: "login", message: resMessage})
            // return res.send("<script>alert('인증에 실패하였습니다.');location.href = \"/user/findPass\";</script>");
        }
    })
});

module.exports = router;