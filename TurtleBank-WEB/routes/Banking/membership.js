var express = require('express');
var router = express.Router();
const axios = require("axios");
const profile = require('../../middlewares/profile');
const {decryptRequest} = require("../../middlewares/crypt")
const checkCookie = require("../../middlewares/checkCookie");
const IpCheck = require('../../middlewares/IpCheck');


HTML_PNG = `<img src="../img/membership.png" style="width:100%;">`

router.get('/', checkCookie, function (req, res) {          // 멤버십 페이지 불러오기
    const cookie = req.cookies.Token

    profile(cookie).then(pending => {
        axios({          // 멤버십 페이지 불러오기를 위한 api req
            method: "post",
            url: api_url + "/api/beneficiary/ceiling",
            headers: {"authorization": "1 " + cookie}
        }).then((data) => {
            let html = ""
            const resStatus = decryptRequest(data.data).status;
            const resData = decryptRequest(data.data).data;

            if(Array.isArray(resData))          // 데이터가 배열이면, 관리자 멤버십 관리 페이지 출력
            {
                html +=
                    "<h2 align='center'>환영합니다, 관리자님!</h2>\n" +
                    "<thead>\n" +
                    "   <tr>\n" +
                    "      <th>사용자명</th>\n" +
                    "      <th>현재 멤버십</th>\n" +
                    "      <th colspan='3'>권한 변경</th>\n" +
                    "   </tr>\n" +
                    "</thead>\n" + HTML_PNG
                
                const printData = resData.slice(1,)
                printData.forEach(x => {
                    html += 
                    `<tbody>
                        <tr>
                            <td>${x.username}</td>`
                    html += `<td><b>${x.membership}</b></td>`
                    html += 
                        `<td><a href="/bank/membership/silver?id=${x.id}" class="btn btn-secondary btn-user btn-block">SILVER</td>
                        <td><a href="/bank/membership/gold?id=${x.id}" class="btn btn-warning btn-user btn-block">GOLD</td>
                        <td><a href="/bank/membership/platinum?id=${x.id}" class="btn btn-info btn-user btn-block">PLATINUM</td>
                    </tr>
                </tbody>`

                })
            } else if (resStatus.code === 200) {          // 상태가 200이면,
                if(resData.membership === "ADMIN") {          // 멤버십이 ADMIN이면,
                    html += "<h2>이 사이트에는 멤버십을 관리할 유저가 없습니다!</h2>"
                }
                else {          // 멤버십이 ADMIN이 아니면,
                    html += `<h2 align='center'>회원님의 멤버십 등급은 ${resData.membership}등급입니다.</h2>`
                    html += HTML_PNG
                }
            } else {
                html += "<h2>오류입니다.</h2>"
            }
            res.render("Banking/membership", {html: html, pending: pending, select: "membership"})
        })
    })
});

router.get('/silver', [checkCookie, IpCheck], function (req, res, next) {
    const id = req.query.id;
    const cookie = req.cookies.Token;
    profile(cookie).then((pending) => {
        if (pending.data.username === "admin") {
            axios({          // user가 존재하는지 확인하는 api로 req
                method: "post",
                url: api_url + "/api/User/profile/S",
                data:{id:id},
                headers: {"authorization": "1 " + cookie}
            })
            .then(()=> res.redirect("/bank/membership"))
            .catch(error => {
                console.error('API 요청 실패:', error.message);
                    })
                    }
        else {
            res.send(`<script>
            alert("관리자가 아닙니다");
            location.href=\"/bank/membership\";
            </script>`);
        }
    })
});


router.get('/gold', [checkCookie, IpCheck], function (req, res, next) {          // gold 일 때 화면 불러오기
    const id = req.query.id;
    const cookie = req.cookies.Token;
    profile(cookie).then((pending) => {
        if (pending.data.username == "admin") {
            axios({          // user가 존재하는지 확인하는 api로 req
                method: "post",
                url: api_url + "/api/User/profile/G",
                data:{id:id},
                headers: {"authorization": "1 " + cookie}
            })
            .then(()=> res.redirect("/bank/membership"))
            .catch(error => {
                console.error('API 요청 실패:', error.message);
                    })
                
        } else {
            res.send(`<script>
            alert("관리자가 아닙니다");
            location.href=\"/bank/membership\";
            </script>`);
        }
    })
});

router.get('/platinum', [checkCookie, IpCheck], function (req, res, next) {          // platinum 일 때 화면 불러오기
    const id = req.query.id;
    const cookie = req.cookies.Token;
    profile(cookie).then((pending) => {
        if (pending.data.username == "admin") {
            axios({          // user가 존재하는지 확인하는 api로 req
                method: "post",
                url: api_url + "/api/User/profile/P",
                data:{id:id},
                headers: {"authorization": "1 " + cookie}
            })
            .then(()=> res.redirect("/bank/membership"))
            .catch(error => {
                console.error('API 요청 실패:', error.message);
                    })
                
        } else {
            res.send(`<script>
            alert("관리자가 아닙니다");
            location.href=\"/bank/membership\";
            </script>`);
        }
    })
});

module.exports = router;