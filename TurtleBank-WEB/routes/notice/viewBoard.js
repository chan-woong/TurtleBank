var express = require("express");
var router = express.Router();
const profile = require("../../middlewares/profile");
const axios = require("axios");
var {decryptRequest, decryptEnc, encryptResponse} = require("../../middlewares/crypt");


router.get("/", function (req, res, next) {
    if (req.cookies.Token) {          // 쿠키에서 로그인한 정보가 있는 경우
        const cookie = decryptEnc(req.cookies.Token);
        profile(cookie).then((data) => {
            var cookieData = data.data;          // 존재하는 모든 공지사항 글들을 불러오는 쿼리
            axios({
                method: "post",
                url: api_url + "/api/notice/viewBoard",
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
                    res.render('temp/notice/viewBoard');
                }
            });
        });
    } else {          // 쿠키에서 로그인한 정보가 없는 경우
        axios({
            method: "post",
            url: api_url + "/api/notice/viewBoard",
        }).then((data) => {
            resStatus = decryptRequest(data.data).status;
            resMessage = decryptRequest(data.data).data.message;
            results = decryptRequest(data.data).data;
    
            if (resStatus.code === 200) {
                res.render("temp/notice/viewboard", {          
                    // 존재하는 모든 공지사항 글들을 렌더링
                    select: "notices",
                    results: results
                });
            }else{
                res.render('temp/notice/viewBoard');
            }
        });
    }
});

module.exports = router;
