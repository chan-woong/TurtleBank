var express = require("express");
var router = express.Router();
const profile = require("../../middlewares/profile");
const axios = require("axios");
var {decryptRequest, decryptEnc, encryptResponse} = require("../../middlewares/crypt");

router.post("/", function (req, res, next) {
    if (req.cookies.Token) {          // 쿠키에서 로그인한 정보가 있는지 확인(Token)
        var cookie = decryptEnc(req.cookies.Token);
        const baseData = `{"searchtext" : "${req.body.searchTitle == null? "" : req.body.searchTitle}"}`;
        profile(cookie).then((data) => {
            var cookieData = data.data;          // db에서 select 문을 활용하여 검색한 제목이 포함되는 정보가 존재하는지 확인.
            axios({
                method: "post",
                url: api_url + "/api/notice/searchBoard",
                data: encryptResponse(baseData)
            }).then((data) => {
                resStatus = decryptRequest(data.data).status;
                resMessage = decryptRequest(data.data).data.message;
                results = decryptRequest(data.data).data;
                if (resStatus.code === 200) { 
                    res.render("temp/notice/viewboard", {          
                        // 존재하는 모든 공지사항 글들을 렌더링
                        select: "notices",
                        results: results,
                        u_data: cookieData.username
                    });
                }else{
                    res.render('temp/notice/alert');
                }
            });
        });
    } else {          // db에서 select 문을 활용하여 검색한 제목이 포함되는 정보가 존재하는지 확인.
        axios({
            method: "post",
            url: api_url + "/api/notice/searchBoard",
            data: encryptResponse(baseData)
        }).then((data) => {
            resStatus = decryptRequest(data.data).status;
            resMessage = decryptRequest(data.data).data.message;
            results = decryptRequest(data.data).data;

            if (resStatus.code === 200) { 
                res.render('temp/notice/viewboard',{
                    select: "notices",
                    results: results
                });
            }else{
                res.render('temp/notice/alert');
            }
        });
    }
});

module.exports = router;
