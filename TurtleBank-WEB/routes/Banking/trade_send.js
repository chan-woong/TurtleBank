var express = require('express');
var router = express.Router();
const axios = require("axios");
const profile = require("../../middlewares/profile")
const {decryptRequest, encryptResponse} = require("../../middlewares/crypt")
const checkCookie = require("../../middlewares/checkCookie")
var time = require('../../middlewares/seoultime');
const sha256 = require("js-sha256")

router.get("/", checkCookie, async (req, res) => {          // 송금 기본 페이지 불러오기
    const cookie = req.cookies.Token;
    profile(cookie).then((data) => {
        const en_data = encryptResponse(JSON.stringify({username:data.data.username}));
        axios({          // 송금 페이지를 위한 api로 req 
            method: "post",
            url: api_url + "/api/beneficiary/account",
            headers: {"authorization": "1 " + cookie},
            data:en_data
        }).then((data2) => {
            var d = decryptRequest((data2.data));
            var results = d.data.accountdata;
            var html_data = `
                <select class="form-control form-control-user mb-3" name="from_account" aria-label="Large select example" style="width: 100%;">
                    <option selected>출금 계좌</option>`;
            results.forEach(function (a) {
                html_data += `<option value="${a}">${a}</option>`;
            });


            html_data += `</datalist><br>`;

            html_data += `<input type="text" class="form-control form-control-user mb-3"id="to_account" name="to_account" placeholder="받는 계좌" > `
            res.render("Banking/trade_send", {pending: data, html: html_data, select: "send"});
        });
    });
});

router.post("/post", checkCookie, function (req, res, next) {          // 송금 요청
    const cookie = req.cookies.Token;
    profile(cookie).then((data) => {
        let json_data = {};
        let result = {};
        const { from_account, to_account, amount, accountPW } = req.body;
        
        json_data['from_account'] = parseInt(from_account);
        json_data['to_account'] = parseInt(to_account);   //데이터가 숫자로 들어가야 동작함
        json_data['amount'] = parseInt(amount);
        json_data['sendtime'] = time.seoultime;
        json_data['accountPW'] = sha256(accountPW);
        json_data['username'] = data.data.username;
        json_data['membership'] = data.data.membership;
        json_data['is_admin'] = data.data.is_admin;
        
        const en_data = encryptResponse(JSON.stringify(json_data));// 객체를 문자열로 반환 후 암호화

        axios({          // 송금을 위한 api로 req
            method: "post",
            url: api_url + "/api/balance/check_pw",
            headers: {"authorization": "1 " + cookie},
            data: en_data
        }).then((data) => {
            result = decryptRequest(data.data);
            statusCode = result.status.code;
            message = result.status.message;
            if(statusCode == 200) {          // 성공하면, 성공 메시지
                axios({          // 송금을 위한 api로 req
                    method: "post",
                    url: api_url + "/api/balance/transfer",
                    headers: {"authorization": "1 " + cookie},
                    data: en_data
                }).then((data) => {
                    result = decryptRequest(data.data);
                    statusCode = result.data.status;
                    message = result.data.message;
                    if(statusCode != 200) {          // 성공하면, 성공 메시지
                        console.error(message);
                        res.send(`<script>
                        alert("${message}");
                        location.href=\"/bank/send\";
                        </script>`);
                    } else {          // 실패하면, 실패 메시지
                        res.send(`<script>
                        alert("${message}");
                        location.href=\"/bank/send\";
                        </script>`);
                    }
                });
            } else {          // 실패하면, 실패 메시지
                res.send(`<script>
                alert("${message}");
                location.href=\"/bank/send\";
                </script>`);
            }
        });
    });
});

router.all('/check', checkCookie, function (req, res, next) {
    const cookie = req.cookies.Token;
    profile(cookie).then((data) => {
        const to_account = req.query.to;
        let json_data = {"to_account":to_account};
        axios({
            method: "post", 
            url: api_url + "/api/balance/transfer/check_account",
            headers: {"authorization": "1 " + cookie},
            data: encryptResponse(JSON.stringify(json_data))
        }).then((data) => {
            resStatus = decryptRequest(data.data).status;
            resMessage = decryptRequest(data.data).data.message;
            results = decryptRequest(data.data).data;
            if (resStatus.code === 200) {
                // res.redirect('../viewBoard');
                res.send({"username" : results.username});
            }else{
                // res.render('temp/qna/alert');
                res.status(404).end();
            }
        }).catch(err => {
            res.status(404).end();
        });
    });
});

module.exports = router;