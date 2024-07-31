var express = require('express');
var router = express.Router();
var tokenauth = require('./tokenauth');
const axios = require("axios");
var {decryptRequest, decryptEnc, encryptResponse} = require("../../middlewares/crypt");

router.get('/', function (req, res, next) {          // 삭제 페이지 불러오기

    tokenauth.authresult(req, function (aResult) {
        if (aResult === true) {          // aResult가 true면
            const baseData = `{"id" : "${req.query.id}"}`;
            axios({
                method: "post",
                url: api_url + "/api/qna/deleteBoard",
                data: encryptResponse(baseData)
            }).then((data) => {
                resStatus = decryptRequest(data.data).status;
                resMessage = decryptRequest(data.data).data.message;
                results = [decryptRequest(data.data).data];
                if (resStatus.code === 200) { 
                    res.redirect('viewBoard');
                }else{
                    res.render('temp/qna/alert');
                }
            });
        } else {
            res.render('temp/qna/alert');
        }
    });
});

module.exports = router;
