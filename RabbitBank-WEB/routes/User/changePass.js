var express = require('express');
const axios = require("axios");
const {decryptRequest, decryptEnc, encryptResponse} = require("../../middlewares/crypt");
const Response = require("../../middlewares/Response");
const sha256 = require("js-sha256")
var router = express.Router();
const checkCookie = require("../../middlewares/checkCookie")

router.get("/", (req, res) => {
    if (!req.cookies.Token) {          // user가 로그인하지 않은 경우
        res.redirect("/")
    } else {          // user가 로그인한 경우
        const cookie = decryptEnc(req.cookies.Token);
        axios({          // user가 존재하는지 확인하는 api로 req
            method: "post",
            url: api_url + "/api/User/profile",
            headers: {"authorization": "1 " + cookie}
        }).then((data) => {          // 해당 user에 대한 정보가 존재하는 경우
            const r = new Response();
            const resStatus = decryptRequest(data.data).status;
            const resData = decryptRequest(data.data).data;

            r.status = resStatus
            r.data = resData

            res.render("temp/changePass", {select: "login", u_data: r.data.username});          // 비밀번호 변경페이지로 렌더링
        });
    }
})

router.post("/", checkCookie, (req, res) => {
    const {password, new_password, confirm_password} = req.body
    const sha256Pass = sha256(password)          // 입력받은 비밀번호를 sha256으로 해싱
    if (new_password == confirm_password){      // 입력받은 새 비밀번호를 한번 더 입력받아 검증
        const sha256Newpass = sha256(new_password)          //  검증결과 양호일 경우 sha256으로 해싱
        const req_data = `{"password" : "${sha256Pass}","new_password" : "${sha256Newpass}"}`
        const cookie = req.cookies.Token;
        let resStatus = ""
        let resMessage = ""

        axios({          // 비밀번호 변경하는 api로 req
            method: "post",
            url: api_url + "/api/User/change-password",
            headers: {"authorization": "1 " + cookie},
            data: encryptResponse(req_data)         
        }).then((data) => {
            resStatus = decryptRequest(data.data).status
            resMessage = decryptRequest(data.data).data.message
            if (resStatus.code === 200) {          // 비밀번호가 변경된 경우
                return res.send("<script>alert('비밀번호가 변경되었습니다.');location.href = \"/user/login\";</script>");
            } else {          // 비밀번호가 변경되지 않은 경우
                res.render("temp/changePass", {select: "login", message: resMessage})
            }
        });
    }
    else{   // 새 비밀번호 검증에 실패한 경우
        return res.send("<script>alert('비밀번호 확인 실패. 다시 시도해주세요.');location.href = \"/user/changePass\";</script>");
    }
    
})

module.exports = router;