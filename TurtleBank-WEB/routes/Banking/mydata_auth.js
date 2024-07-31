var express = require('express');
var router = express.Router();
var axios = require("axios");
var {encryptResponse, decryptRequest} = require("../../middlewares/crypt");
const profile = require('../../middlewares/profile');
const checkCookie = require("../../middlewares/checkCookie")

var html_data_description = "<h3 align='center'> Mydata 서비스는 타은행의 계좌 잔고확인/송금 까지 한번에 해결할 수 있도록 하는 서비스입니다 !</h3>"


router.get('/', checkCookie, function (req, res) {          // 마이데이터(신청 전) 화면 불러오기
    const cookie = req.cookies.Token;
    
    profile(cookie).then(profileData => {
        var is_mydata = profileData.data.is_mydata;
        if(is_mydata){          // 해당 user의 is_mydata = 1 인 경우
            return res.render("Banking/mydata_auth", {html_data: "<br/>", pending: profileData, select: "mydata"});
        }

        else{          // 해당 user의 is_mydata = 0 인 경우
            var result = `
            <div style="text-align:center; width:100%; display:inline-block;">
        <form action="/bank/mydata_auth" method="post">
            <button class="btn btn-user btn-block" type="submit" id="view" value="submit" style="background-color:#b937a4 !important; color:white !important;">마이데이터 요청</button>
        </form>
    </div>
            `
            return res.render("Banking/mydata_auth", {html_data: result, pending: profileData, select: "mydata"});
        }
       
    });
});


router.post('/', checkCookie, function (req, res) {          // 마이데이터(신청 전) 요청하기
    const cookie = req.cookies.Token;
    
    profile(cookie).then(profileData => {
        axios({          // 마이데이터 연동을 위한 API로 request
            method: "get",
            url: api_url + "/api/Mydata/mydata_sms",
            headers: {"authorization": "1 " + cookie}
        }).then((data) => {
            let result = decryptRequest(data.data);
            if(result.status.code==200){          // user의 마이데이터 연동요청 후 인증번호가 보내졌을 경우
                let result = `                
                <form action="/bank/mydata_auth/authnum" method="post" id="authnum">
                <div class="form-group">
                    <input type="number" class="form-control form-control-user" name="authnum" placeholder="인증번호를 입력해주세요" value="">
                        <br>
                </div>
              </form>
              <a onclick="document.getElementById('authnum').submit()" class="btn btn-user btn-block" style="background-color:#b937a4 !important; color:white !important;">
              확인
            </a>
            <br>
            <a href="/bank/mydata_auth" onclick="document.getElementById('register').submit();" class="btn btn-user btn-block" style="background-color:#b937a4 !important; color:white !important;">
              취소
            </a>
            `
                return res.render("Banking/mydata_auth", {html_data: result, pending: profileData, select: "mydata"});
            }
            else{          // user의 마이데이터 연동요청 후 인증번호가 보내지지 않았을 경우
                let result = "오류입니다."
                
                return res.render("Banking/mydata_auth", {html_data: result, pending: profileData, select: "mydata"})
            }
        }).catch(function (error) {

            var html_data = [
                 { username: error, balance: error, account_number: error, bank_code: error }
            ];
            return res.render("Banking/mydata_auth", {html_data: html_data, pending: profileData, select: "mydata"});
        });
    });
});


router.post('/authnum', checkCookie, function (req, res) {          // user에게 인증번호 입력을 받음
    const cookie = req.cookies.Token;
    let authnum = req.body.authnum;
    const en_data = encryptResponse(JSON.stringify({authnum: authnum}));
    profile(cookie).then(profileData => {
        axios({          // user에게 입력받은 인증번호 확인요청
            method: "post",
            url: api_url + "/api/Mydata/mydata_sms",
            headers: {"authorization": "1 " + cookie},
            data: en_data
        }).then((smsdata)=>{
            var result = decryptRequest(smsdata.data);

            if (result.status.code == 200) {          // 인증성공 후 is_mydata가 1로 변환된 경우
                let html_data = `
                <script>
                alert('인증에 성공했습니다');
                window.location.href = "/bank/mydata";
            </script>
              `;
                return res.send(html_data);
            } else {          // 인증실패로 is_mydata가 1로 변환되지 못한 경우

                let html_data = `
              <script>alert('인증에 실패했습니다');</script>
              `;

                return res.render("Banking/mydata_auth", { html_data: html_data, pending: profileData, select: "mydata" });
            }
        }).catch(function (err) {

            var result = "<tr>에러 페이지 입니다.</tr>"
            return res.render("Banking/mydata_auth", { html_data: result, pending: profileData, select: "mydata" });
        });
    });
})
module.exports = router;