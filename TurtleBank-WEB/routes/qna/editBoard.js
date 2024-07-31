var {seoultime} = require("../../middlewares/seoultime");
var express = require('express');
var router = express.Router();
var tokenauth = require('./tokenauth');
const profile = require('../../middlewares/profile');
const axios = require("axios");
var {decryptRequest, decryptEnc, encryptResponse} = require("../../middlewares/crypt");

router.get('/', function (req, res, next) {          // QnA글 수정 페이지 불러오기
    var cookie = decryptEnc(req.cookies.Token);
    profile(cookie).then((data) => {
        var cookieData = data.data;
        tokenauth.authresult(req, function (aResult) {
            if (aResult == true) {
                const baseData = `{"id" : "${req.query.id}"}`;
                axios({
                    method: "post",
                    url: api_url + "/api/qna/editBoard",
                    data: encryptResponse(baseData)
                }).then((data) => {
                    resStatus = decryptRequest(data.data).status;
                    resMessage = decryptRequest(data.data).data.message;
                    results = [decryptRequest(data.data).data];
                    if(resStatus.code === 200) {
                        res.render('temp/qna/editBoard', {          // 글 수정페이지로 랜더링
                            select: "qnas",
                            u_data: cookieData.username,
                            results: results,
                            tempid: req.query.id
                        });
                    }
                    else{
                        res.render('temp/qna/alert');
                    }
                });
            } else {
                res.render('temp/qna/alert');
            }
        });
    })
});

router.post('/edit', function (req, res, next) {          // editBoard에서 /edit form 실행 시
    var {title, contents, pid} = req.body;
    
    contents = contents.replace(/(?:\r\n|\r|\n)/g, '<br/>');
    const baseData = `{"title" : "${title}","contents" : "${contents}","id" : "${pid}","updatedAt" : "${seoultime}"}`;
    axios({          //비밀번호 찾기 위한 api로 req
        method: "post",
        url: api_url + "/api/qna/editBoard/edit",
        data: encryptResponse(baseData)
    }).then((data) => {
        resStatus = decryptRequest(data.data).status;
        resMessage = decryptRequest(data.data).data.message;
        if (resStatus.code === 200) {
            res.redirect('../viewBoard');
        } else {
            res.render('temp/qna/alert');
        }
    });

});

module.exports = router;