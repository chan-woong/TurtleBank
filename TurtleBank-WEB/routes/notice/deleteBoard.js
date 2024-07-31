var express = require('express');
var router = express.Router();
var tokenauth = require('./tokenauth');
const axios = require("axios");
var {decryptRequest, decryptEnc, encryptResponse} = require("../../middlewares/crypt");

router.get('/', function (req, res, next) {          
    if (req.cookies.Token) {          // user가 로그인 한 경우
        tokenauth.admauthresult(req, function (aResult) {
            if (aResult == true) {          // user가 admin인 경우
                // 업로드된 파일에 대한 정보를 select 진행
                const baseData = `{"id" : "${req.query.id}"}`;
                axios({
                    method: "post",
                    url: api_url + "/api/notice/deleteBoard",
                    data: encryptResponse(baseData)
                }).then((data) => {
                    resStatus = decryptRequest(data.data).status;
                    resMessage = decryptRequest(data.data).data.message;
            
                    if (resStatus.code === 200) { 
                        res.redirect('viewBoard');
                    }else{
                        res.render('temp/notice/alert');
                    }
                });
            } else {          // user가 로그인 하지 않은 경우
                res.render('temp/notice/alert');
            }
        });
    } else {          // user가 로그인 하지 않은 경우
        res.render('temp/notice/alert');
    }
});

module.exports = router;
