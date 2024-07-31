//이조 계좌목록 js파일 추가.
var express = require('express');
var router = express.Router();
var axios = require("axios");
var {encryptResponse, decryptRequest} = require("../../middlewares/crypt");
const profile = require('../../middlewares/profile');
const checkCookie = require("../../middlewares/checkCookie")



router.get('/', checkCookie, function (req, res) {          // account_list 페이지에서 get 요청
    const cookie = req.cookies.Token;

    profile(cookie).then(profileData => {
        axios({          // account_list를 출력해주는 API로 req
            method: "post",
            url: api_url + "/api/account/view",
            headers: {"authorization": "1 " + cookie}
        }).then((data) => {          

            let result = decryptRequest(data.data).data;          // account_list 페이지에 계좌번호, 은행코드, 계좌금액 데이터를 넘겨줌

            for (var i=0; i< result.length; i++){               // 잔고 100단위 ,찍기 
                result[i].balance = result[i].balance.toLocaleString();
            }
            
            return res.render("Banking/account_list", {html: " " , html_data: result, pending: profileData, select: "account_list"});
        
        }).catch(function (error) {

            var html_data = "<tr>에러</tr>";

            return res.render("Banking/account_list", {html: " ", html_data: html_data, pending: profileData, select: "account_list"});
        });
    });
});


router.post('/create', checkCookie, function (req, res) {          // account_list 페이지에서 신규계좌 생성 시
    const cookie = req.cookies.Token;
    
    profile(cookie).then(profileData => {
        axios({          // 신규계좌 생성하는 API로 req
            method: "post",
            url: api_url + "/api/account/create",
            headers: {"authorization": "1 " + cookie}
        }).then((data) => {          // 신규계좌가 성공적으로 생성된 경우
            
            var result = decryptRequest(data.data);
            //console.log("@@@@@@@@@@@@@@@@@@@@@@@@@",result);
            if(result.status.code == 200){
            var html_alert= `
                <script>
                alert("신규계좌가 생성되었습니다."); 
                window.location.href = "/bank/account_list";
                </script>
                `
            return res.send(html_alert);
            }else {          // 신규계좌가 성공적으로 생성지 못한 경우
                var html_alert= `
                <script>
                alert("다시 시도해주세요."); 
                window.location.href = "/bank/account_list";
                </script>
                `
            return res.send(html_alert);
            }
        }).catch(function (error) {
            var html_data = [
                { balance: error, account_number: error, bank_code: error }
            ];

            return res.render("Banking/account_list", {html : " " ,html_data: html_data, pending: profileData, select: "account_list"});
        });
    });
});

module.exports = router;