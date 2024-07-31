var express = require('express');
var router = express.Router();
const axios = require("axios");
const profile = require("../../middlewares/profile")
const {decryptRequest, encryptResponse, decryptEnc} = require("../../middlewares/crypt")
const checkCookie = require("../../middlewares/checkCookie")
var {seoultime, simpletime} = require('../../middlewares/seoultime');

var html_data = ` <input type="date" id="start" name="tripstart"
value="${simpletime}"
min="2023-01-01" max="${simpletime}">&nbsp;&nbsp;
검색종료일&nbsp;&nbsp;<input type="date" id="end" name="tripend"
value="${simpletime}"
min="2023-01-01" max="${simpletime}">&nbsp;&nbsp;
<input type ="submit" value ="검색">
</form>
<tr><th>송금은행</th><th>송금계좌</th><th>수취은행</th><th>수취계좌</th><th>금액</th><th>시간</th></tr></thead>`;

router.get("/", checkCookie, async (req, res) => {          // 입출금 내역 초기 페이지 불러오기
    const cookie = req.cookies.Token;
    profile(cookie).then((data) => {
            axios({          // 입출금 내역 요청을 위한 api로 req
                method: "post",
                url: api_url + "/api/transactions/view/search",
                headers: {
                    "authorization": "1 " + cookie
                },
                data: {          // 초기 검색을 위한 기간
                    tripstart: '1998-02-20 00:00:00',
                    tripend: simpletime
                },
            }).then((data2) => {
                if(data2){          // 날짜 검색 api에서 data2가 있으면,
                var get_html = null
                get_html = html_data;
                de_data = decryptRequest(data2.data);
                row = de_data.data.result;
                    row.forEach(function (i) {
                        var temp = i.sendtime ;
                        if(i.from_bankcode == 333){          // bankcode == 333 : 소드뱅크, bankcode == 555 : 쉴드뱅크
                            var from_bankcode = "TURTLEBANK";
                        }
                        else{
                            var from_bankcode = "RABBITBANK";
                        }
                        if(i.to_bankcode == 333){          // bankcode == 333 : 소드뱅크, bankcode == 555 : 쉴드뱅크
                            var to_bankcode = "TURTLEBANK";
                        }
                        else{
                            var to_bankcode = "RABBITBANK";
                        }
                        get_html += "<tr><td>" + from_bankcode + "</td><td>" + i.from_account + "</td><td>" + to_bankcode + "</td><td>" + i.to_account + "</td><td>" + i.amount.toLocaleString() + "</td><td>" + (i.sendtime).substring(0, temp.length - 5).replace('T', ' '); + "</td></tr>" ;
                        });
                    }
                    return res.render("Banking/trade_list", {pending: data, html: get_html, select: "list"});
            }).catch(function (error) {
                return res.render("Banking/trade_list", {pending: data, html: html_data, select: "list"});
            });
        });
    });

router.post("/", checkCookie, async (req, res) => {          // 입출금 내역 요청
    const cookie = req.cookies.Token;
    var bt= req.body.tripstart;
    var be= req.body.tripend;
   
    profile(cookie).then((data) => {
        axios({          // 입출금 내역 요청을 위한 api로 req
            method: "post",
            url: api_url + "/api/transactions/view/search", // URL 수정 해야 됨
            headers: {
                "authorization": "1 " + cookie
            },
            data: { // 요청한 기간
                tripstart: bt,
                tripend: be
            },
        }).then((data2) => {
            var post_html = null
            post_html = html_data;
            de_data = decryptRequest(data2.data);
            row = de_data.data.result;
                row.forEach(function (i) {
                    var temp = i.sendtime ;
                    if(i.from_bankcode == 333){          // bankcode == 333 : 소드뱅크, bankcode == 555 : 쉴드뱅크
                        var from_bankcode = "TURTLEBANK";
                    }
                    else{
                        var from_bankcode = "RABBITBANK";
                    }
                    if(i.to_bankcode == 333){          // bankcode == 333 : 소드뱅크, bankcode == 555 : 쉴드뱅크
                        var to_bankcode = "TURTLEBANK";
                    }
                    else{
                        var to_bankcode = "RABBITBANK";
                    }
                post_html += "<tr><td>" + from_bankcode + "</td><td>" + i.from_account + "</td><td>" + to_bankcode + "</td><td>" + i.to_account + "</td><td>" + i.amount.toLocaleString() + "</td><td>" + (i.sendtime).substring(0, temp.length - 5).replace('T', ' '); + "</td></tr>" ;
                });
                
            return res.render("Banking/trade_list", {pending: data, html: post_html, select: "list"});

        }).catch(function (error) {
            return res.render("Banking/trade_list", {pending: data, html: html_data, select: "list"});
        });
    });
});
    
module.exports = router;