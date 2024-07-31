var express = require('express');
var router = express.Router();
const axios = require("axios");
const profile = require("../../middlewares/profile")
const {decryptRequest, encryptResponse} = require("../../middlewares/crypt")
const checkCookie = require("../../middlewares/checkCookie")
var {seoultime} = require('../../middlewares/seoultime');
const statusCodes = require('../../middlewares/statusCodes');


router.get("/", checkCookie, async (req, res) => {          // 마이데이터 불러오기 계좌 송금 선택 시   
    const cookie = req.cookies.Token;
    const accountNumber = req.query.account_number;
    
    profile(cookie).then((data) => {
       
           var html_data = `<label>출금 계좌 : </label><input type="text" class="form-control form-control-user" autocomplete="off" id="drop_from" name="from_account" placeholder="${accountNumber}" list="dropdown_from" value="${accountNumber}" readonly><br>`
               
           html_data += `<select name="bank_code" class="form-control form-control-user" id="bank_code">`
           html_data += `<option value="555">TURTLEBANK</option>`
           html_data += `<option value="333">RABBITBANK</option></select><br>`
           html_data += `<input type="text" class="form-control form-control-user" id="to_account" name="to_account" placeholder="대상 계좌번호"><br>`
           html_data += `<input type="text" class="form-control form-control-user" id="amount" name="amount" placeholder="금액">`

           res.render("Banking/otherbank_send", {pending: data, html: html_data, select: "otherbank_send"});
        });
    });



router.post("/post", checkCookie, function (req, res) {          // 마이데이터에서 송금하기 
    const cookie = req.cookies.Token;
    let json_data = {};
    let result = {};
    json_data['from_account'] = parseInt(req.body.from_account);
    json_data['to_account'] = parseInt(req.body.to_account);   
    json_data['bank_code'] = parseInt(req.body.bank_code);
    json_data['amount'] = parseInt(req.body.amount);
    json_data['sendtime'] = seoultime;
    const en_data = encryptResponse(JSON.stringify(json_data));


    if(req.body.bank_code == '555') {          // 송금받는 계좌의 bank_code가 555일 때 (TURTLEBANK)
        axios({          // B은행에서 A은행으로의 송금을 위한 API req
            method: "post",
            url: api_url + "/api/mydata/b_to_a",          // 송금하는 계좌 : B은행, 송금받는 계좌 : A은행
            headers: {"authorization": "1 " + cookie},
            data: en_data
        }).then((data) => {
            
            result = decryptRequest(data.data);
            statusCode = result.status.code;
            message = result.data.message;
            if(statusCode != 200) {          // 송금 거래가 정상적으로 이루어지지 않은 경우
                
                res.send(`<script>
                alert("${message}");
                location.href=\"/bank/mydata\";
                </script>`);
            } else {          // 송금 거래가 정상적으로 이루어진 경우
                res.send(`<script>
                alert("${message}");
                location.href=\"/bank/mydata\";
                </script>`);
            }
        });
    }

    else if(req.body.bank_code == '333') {          // 송금받는 계좌의 bank_code가 333일 때 (RABBITBANK)
        axios({          // B은행에서 B은행으로의 송금을 위한 API req
            method: "post",
            url: api_url + "/api/mydata/b_to_b",          // 송금하는 계좌 : B은행, 송금받는 계좌 : B은행
            headers: {"authorization": "1 " + cookie},
            data: en_data
        }).then((data) => {
            result = decryptRequest(data.data);
            statusCode = result.data.status;
            message = result.data.message;
            if(statusCode != 200) {
                res.send(`<script>
                alert("${message}");
                location.href=\"/bank/mydata\";
                </script>`);
            } else {
                res.send(`<script>
                alert("${message}");
                location.href=\"/bank/mydata\";
                </script>`);
            }
        });
    }
});


module.exports = router;
