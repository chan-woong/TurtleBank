var express = require('express');
var router = express.Router();
var tokenauth = require('./tokenauth');
const profile = require('../../middlewares/profile');
const axios = require("axios");
var {decryptRequest, decryptEnc, encryptResponse} = require("../../middlewares/crypt");

router.get('/', function (req, res, next) {          // 글 세부내용 페이지 불러오기
    var cookie = decryptEnc(req.cookies.Token);
    profile(cookie).then((data) => {
        var cookieData = data.data;

        tokenauth.authresult(req, function (aResult) {
            if (aResult == true) {          // aResult가 true일 때,

                const baseData = `{"id" : "${req.query.id}"}`;
                axios({          //비밀번호 찾기 위한 api로 req
                    method: "post",
                    url: api_url + "/api/qna/getBoard",
                    data: encryptResponse(baseData)
                }).then((data) => {
                    resStatus = decryptRequest(data.data).status;
                    resMessage = decryptRequest(data.data).data.message;
                    results = [decryptRequest(data.data).data];
                    results[0].content = results[0].content.replace(/<br\s*\/?>/gi, '\n');
                    // results.replace(/<br\s*\/?>/gi, '\n')
                    if (resStatus.code === 200) { 
                        res.render('temp/qna/getboard', 
                            {select:"qnas",results: results, u_data: cookieData.username}); 
                    }else{
                        res.render('temp/qna/alert');
                    }
                });
            } else {          // aResult가 true가 아닐 때,
                res.render('temp/qna/alert');
            }
        });
    });
});

module.exports = router;
