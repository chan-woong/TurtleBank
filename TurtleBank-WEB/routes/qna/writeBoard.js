var {seoultime} = require("../../middlewares/seoultime");
var express = require('express');
var router = express.Router();
var tokenauth = require('./tokenauth');
const profile = require('../../middlewares/profile');
const axios = require("axios");
var {decryptRequest, decryptEnc, encryptResponse} = require("../../middlewares/crypt");

router.get('/', function (req, res, next) {          // QnA 작성하는 페이지 불러오기
    var cookie = decryptEnc(req.cookies.Token);
    profile(cookie).then((data) => {
        var cookieData = data.data;
        tokenauth.authresult(req, function (aResult) {
            if (aResult == true) {          // aResult가 true면,
                res.render('temp/qna/writeBoard', {select:"qnas",u_data: cookieData.username});          // QnA 글 작성 페이지로 랜더링
            } else {          // aResult가 아니면,
                res.render('temp/qna/alert');
            }
        });
    });
});

router.post('/write', function (req, res, next) {          // /writeBoard에서 /qna/writeBoard/write form을 실행하면,
    var cookie = decryptEnc(req.cookies.Token);
    var {title, contents} = req.body;
    contents = contents.replace(/(?:\r\n|\r|\n)/g, '<br/>');
    profile(cookie).then((data) => {
        var userId = data.data.username;
        const baseData = `{"userId" : "${userId}", "title" : "${title}", "content" : "${contents}", "seoultime" : "${seoultime}"}`;
        axios({          //비밀번호 찾기 위한 api로 req
            method: "post",
            url: api_url + "/api/qna/writeBoard/write",
            data: encryptResponse(baseData)
        }).then((data) => {
            resStatus = decryptRequest(data.data).status;
            resMessage = decryptRequest(data.data).data.message;
    
            if (resStatus.code === 200) { 
                res.redirect('../viewBoard');
            }else{
                res.render('temp/qna/alert');
            }
        });
    });
});

module.exports = router;