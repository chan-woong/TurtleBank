var {seoultime} = require('../../middlewares/seoultime');
var express = require('express');
var router = express.Router();
var tokenauth = require('./tokenauth');
const profile = require('../../middlewares/profile');
const axios = require("axios");
var {decryptRequest, decryptEnc, encryptResponse} = require("../../middlewares/crypt");

router.get('/', function (req, res, next) {          // QnA 댓글 작성 불러오기
    var cookie = decryptEnc(req.cookies.Token);
    profile(cookie).then((data) => {          // 쿠키 복호화 및 사용자 프로필 정보 가져오기
        var cookieData = data.data;
        tokenauth.admauthresult(req, function (aResult) {
            if (aResult == true) {          // aResult이 true면,
                const baseData = `{"id" : "${req.query.id}"}`;
                axios({
                    method: "post",
                    url: api_url + "/api/qna/addComment",
                    data: encryptResponse(baseData)
                }).then((results) => {
                    resStatus = decryptRequest(results.data).status;
                    resMessage = decryptRequest(results.data).data.message;
                    results_data = [decryptRequest(results.data).data];
                    if (resStatus.code === 200) { 
                        res.render('temp/qna/addcomment', {
                            select: "qnas",
                            u_data: cookieData.username,
                            results: results_data,
                            tempid: req.query.id
                        });
                    }else{
                        res.render('temp/qna/alert');
                    }
                });
            } else {
                res.render('temp/qna/alert');
            }
        });
    })
});

router.post('/edit', function (req, res, next) {          // 댓글 수정 페이지
    const {comment, pid} = req.body;

    const baseData = `{"comment" : "${comment}","id": "${pid}", "updatedAt" : "${seoultime}"}`;
    axios({
        method: "post",
        url: api_url + "/api/qna/addComment/edit",
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

module.exports = router;