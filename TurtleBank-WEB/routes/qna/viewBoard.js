var express = require("express");
var router = express.Router();
var tokenauth = require("./tokenauth");
var {decryptRequest, decryptEnc, encryptResponse} = require("../../middlewares/crypt");
const profile = require("../../middlewares/profile");
const axios = require("axios");

router.get("/", function (req, res, next) {          // QnA 메인페이지 불러오기
    if (!req.cookies.Token) return res.render("temp/qna/alert");          // 토큰 확인
    tokenauth.authresult(req, function (aResult) {
        if (aResult == true) {          // aResult이 true면,
            const cookie = decryptEnc(req.cookies.Token);
            profile(cookie).then((data) => {          // 쿠키 복호화 및 사용자 프로필 정보 가져오기
                if (data.data.is_admin) {          // 만약 admin이면,
                    var u_data = data.data.username;
                    axios({
                        method: "post",
                        url: api_url + "/api/qna/viewBoard/all",
                    }).then((data) => {
                        resStatus = decryptRequest(data.data).status;
                        resMessage = decryptRequest(data.data).data.message;
                        results = decryptRequest(data.data).data;
                        if (resStatus.code === 200) { 
                            res.render("temp/qna/viewboard", {
                                select: "qnas",
                                u_data: u_data,
                                results: results,
                            });
                        }else{
                            res.render('temp/qna/alert');
                        }
                    });
                } else {          // admin이 아니면,
                    var userId = data.data.username;
                    const baseData = `{"userId" : "${userId}"}`;
                    axios({
                        method: "post",
                        url: api_url + "/api/qna/viewBoard/user",
                        data: encryptResponse(baseData),
                    }).then((data) => {
                        resStatus = decryptRequest(data.data).status;
                        resMessage = decryptRequest(data.data).data.message;
                        results = decryptRequest(data.data).data;
                        if (resStatus.code === 200) { 
                            res.render("temp/qna/viewboard", {
                                select: "qnas",
                                u_data: userId,
                                results: results,
                            });
                        }else{
                            res.render('temp/qna/alert');
                        }
                    });
                }
            });
        } else {
            res.render("temp/qna/alert");
        }
    });
});

module.exports = router;
