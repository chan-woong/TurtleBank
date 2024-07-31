var {seoultime} = require("../../middlewares/seoultime");
var express = require("express");
var router = express.Router();
var tokenauth = require("./tokenauth");
const profile = require("../../middlewares/profile");
const checkCookie = require("../../middlewares/checkCookie");
const multer = require("multer");
const axios = require("axios");
var {decryptRequest, decryptEnc, encryptResponse} = require("../../middlewares/crypt");
const FormData = require('form-data');
const request = require('request');
const fs = require("fs");

router.get("/", function (req, res, next) {
    if (req.cookies.Token) {          // 유저가 로그인을 한 경우
        var cookie = decryptEnc(req.cookies.Token);
        profile(cookie).then((data) => {
            var cookieData = data.data;
            tokenauth.admauthresult(req, function (aResult) {
                if (aResult == true) {          // 현재 로그인 한 유저가 admin인 경우
                    res.render("temp/notice/writeBoard", {select: "notices", u_data: cookieData.username});
                } else {          // 현재 로그인 한 유저가 admin이 아닌 경우
                    res.render("temp/notice/alert");
                }
            });
        });
    } else {        // 유저가 로그인을 하지 않은 경우
        res.reSYNCder("temp/notice/alert");
    }
});

//파일 API 서버에만 전송
const upload = multer({storage: multer.memoryStorage()});

router.post("/write",
    checkCookie,
    upload.single("imgimg"), // 파일 업로드 설정
    function (req, res, next) {
        var { title, contents } = req.body;
        contents = contents.replace(/(?:\r\n|\r|\n)/g, '<br/>');
        // 클라이언트의 쿠키를 사용하여 사용자 프로필 정보를 가져옵니다.
        const cookie = req.cookies.Token;
        profile(cookie).then((data) => {
            var userId = data.data.username;
            if (req.file) {
                const form = new FormData();
                form.append(req.body.fid, req.file.buffer, Buffer.from(req.file.originalname, 'ascii').toString('utf8' ));
                
                // 데이터를 다른 서버로 전송
                request.post({
                    url: api_url+'/api/notice/upload', // 변경된 API 주소
                    headers: form.getHeaders(),
                    body: form
                }, 
                function (error, response, body) {
                    if (error) {
                        throw error;
                    }
                    const filePath = req.file.path;
                    fs.unlink(filePath, (err) => {
                        if (err) {
                            console.error("파일 삭제 중 오류 발생: " + err);
                        } else {
                            console.log("파일이 성공적으로 삭제되었습니다.");
                        }
                    });
                });
            }
            const baseData = `{"title" : "${title}","contents" : "${contents}","userId" : "${userId}","file_name" : "${req.file ? req.file.originalname : "null"}","seoultime" : "${seoultime}"}`;
            axios({
                method: "post",
                url: api_url + "/api/notice/writeBoard/write",
                data: encryptResponse(baseData)
            }).then((data) => {
                resStatus = decryptRequest(data.data).status;
                resMessage = decryptRequest(data.data).data.message;
                if (resStatus.code === 200) { 
                    res.redirect('../viewBoard');
                }else{
                    res.redirect("../viewBoard");
                }
            });
        });
    }
);
module.exports = router;
