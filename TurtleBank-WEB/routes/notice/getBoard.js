var express = require('express');
var router = express.Router();
const profile = require('../../middlewares/profile');
var {decryptRequest, decryptEnc, encryptResponse} = require("../../middlewares/crypt");
const axios = require("axios");

router.get('/', function (req, res, next) {
    const baseData = `{"id" : "${req.query.id}"}`;
    if(req.cookies.Token){          // 쿠키에서 로그인한 정보가 있는지 확인(Token)
        var cookie = decryptEnc(req.cookies.Token);
        profile(cookie).then((data) => {
            var cookieData = data.data;          // db에서 선택한 글의 id로 글의 정보를 select
            axios({
                method: "post",
                url: api_url + "/api/notice/getBoard",
                data: encryptResponse(baseData)
            }).then((data) => {
                resStatus = decryptRequest(data.data).status;
                resMessage = decryptRequest(data.data).data.message;
                results = [decryptRequest(data.data).data];
                if (resStatus.code === 200) { 
                    var path = results[0].filepath          // filepath 재지정
                    var fpp =  results[0].filepath.replace('public', '');
                    
                    res.render('temp/notice/getboard', {select:"notices",results: results, fpp:fpp, u_data: cookieData.username, path:path});
                }else{
                    res.redirect("../viewBoard");
                }
            });
        });
    }else{          // db에서 선택한 글의 id로 글의 정보를 select
        axios({
            method: "post",
            url: api_url + "/api/notice/getBoard",
            data: encryptResponse(baseData)
        }).then((data) => {
            resStatus = decryptRequest(data.data).status;
            resMessage = decryptRequest(data.data).data.message;
            results = [decryptRequest(data.data).data];
            if (resStatus.code === 200) { 
                var path = results[0].filepath          // filepath 재지정
                var fpp =  results[0].filepath.replace('public', '');
                
                res.render('temp/notice/getboard', {select:"notices",results: results, fpp:fpp, path:path});
            }else{
                res.redirect("../viewBoard");
            }
        });
    }
});

module.exports = router;
