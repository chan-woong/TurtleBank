var express = require('express');
const Response = require("../../middlewares/Response");
const {decryptRequest, decryptEnc, encryptResponse} = require("../../middlewares/crypt");
const axios = require("axios");
const sha256 = require("js-sha256");

var router = express.Router();


function generateRandomVerificationCode() {          //인증번호 랜덤 생성
    const min = 1000;
    const max = 9999;
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

router.get('/', function (req, res) {
    var username = req.query.username;
    res.render("temp/findPass", {select: "login", username: username});
});

router.post('/', (req, res) => {
    const username = req.body.username;
    const phone = req.body.phone;
    let resStatus = "";
    let resMessage = "";
    const coolsms = require('coolsms-node-sdk').default;
    const messageService = new coolsms('NCS2ULU0PYWR4DU8', 'LHQVWAJRESNTB8W9SBRJM5LBEIOZPI2D');
    const auth_num = generateRandomVerificationCode();
    const auth_num_str = auth_num.toString(); 

    const baseData = `{"username" : "${username}", "phone" : "${phone}", "auth_num_str" : "${auth_num_str}"}`;
    axios({          //비밀번호 찾기 위한 api로 req
        method: "post",
        url: api_url + "/api/User/findPass",
        data: encryptResponse(baseData)
    }).then((data) => {
        resStatus = decryptRequest(data.data).status;
        resMessage = decryptRequest(data.data).data.message;

        if (resStatus.code === 200) {          // 인증번호가 정상적으로 보내지는 경우
            axios({          //비밀번호 찾기 위한 api로 req
                method: "post",
                url: api_url + "/api/User/findPass/setSmsauths",
                data: encryptResponse(baseData)
            }).then((data) => {
                messageService.sendOne(          // 인증번호 전송
                    {
                        to: phone,
                        from: '01099622086',
                        text: "[인증번호] : " + auth_num_str + "를 입력해주세요."
                }
                ).then(res => console.log(res))
                .catch(err => console.error(err));
                return res.send(`<script>alert('인증번호가 발송되었습니다.');location.href = \"/user/smsAuth?username=${username}\";</script>`);          // 문자 인증페이지로 redirect
            });
        } else {
            res.render("temp/findPass", {select: "findPass", message: resMessage})
        }
    })
})

module.exports = router;
