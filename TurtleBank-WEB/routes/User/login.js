var express = require('express');
var router = express.Router();

const {encryptResponse, decryptRequest} = require('../../middlewares/crypt')
const axios = require("axios");
const sha256 = require("js-sha256")


router.get('/', function (req, res) {          // 로그인 페이지 렌더링
    res.render("temp/login", {select: "login"});
});


router.post('/', function (req, res) {          // 로그인 페이지에서 로그인 값 입력
    const {username, password} = req.body;
    const sha256Pass = sha256(password)
    const baseData = `{"username": "${username}", "password": "${sha256Pass}"}`
    const enData = encryptResponse(baseData);

    axios({          // 입력받은 로그인 값들을 검증하기 위한 api에 req
        method: "post",
        url: api_url + "/api/user/login",
        data: enData
    }).then((data) => {
        let result = decryptRequest(data.data);
        if (result.status.code == 200) {          // 로그인에 성공하여 jwt가 생성된 경우
            return res.render("afterlogin", {data: data.data.enc_data});
        } else {         // 로그인에 실패한 경우
            return res.render("afterlogin");
        }
    })
});

module.exports = router;
