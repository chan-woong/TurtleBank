var express = require('express');
var router = express.Router();
var axios = require("axios");
var {encryptResponse, decryptRequest} = require("../../middlewares/crypt");
const profile = require('../../middlewares/profile');
const checkCookie = require("../../middlewares/checkCookie")

router.get('/', checkCookie, function (req, res) {          // mydata 불러오기 요청하는 페이지
    const cookie = req.cookies.Token;
    profile(cookie).then(profileData => {
        return res.render("Banking/mydata", {html_data: "<br/>", pending: profileData, select: "mydata"});
    });
});

router.post('/', checkCookie, function (req, res) {          // mydata 페이지에서 mydata 불러오기
    const cookie = req.cookies.Token;
    profile(cookie).then(profileData => {
        axios({          // B은행에 있는 account를 불러오기 위한 api로 request
            method: "post",
            url: api_url + "/api/mydata/req_account",
            headers: {"authorization": "1 " + cookie},
        }).then((data) => {
            
            var account_list = decryptRequest(data.data).data;
            var result="";

            if(account_list.length > 0) {          // B은행에서 받아온 data(user 계좌정보)가 존재하는 경우
                result = "<tr>\n";
                account_list.forEach(account => {
                    if(account.bank_code == 333){          // bank_code = 333인 경우 RABBITBANK
                        var bank_code = "RABBITBANK";
                    }
                    else{          // bank_code = 555인 경우 TURTLEBANK
                        var bank_code = "TURTLEBANK";
                    }
                    if(account.bank_code == 333){
                        var bank_code = "RABBITBANK";
                    }
                    else{
                        var bank_code = "TURTLEBANK";
                    }

                    result += "<td>" + account.account_number + "</td>\n";
                    result += "<td>" + account.balance.toLocaleString() + "</td>\n";
                    result += "<td>" + bank_code + "</td>\n";
                    result += "<td style='width:89px'> <button ";
                    result += "class='btn btn-user btn-block' type='button' onclick='redirectToTransferPage(\"" + account.account_number + "\", \"" + account.balance + "\", \"" + account.bank_code + "\")' style='background-color:#b937a4 !important; color:white !important;'>"
                    result += "송금</button></td>";
                    result += "</tr>\n";

                });
                
            }
            else {          // B은행에서 받아온 data(user 계좌정보)가 존재하지 않는 경우
                result += "<tr>\n"
                result += "<td colspan='3'>계좌가 없습니다.</td>\n"
                result += "</tr>\n"
            }

            return res.render("Banking/mydata", {html_data: result, pending: profileData, select: "mydata"});

        }).catch(function (error) {
            return error;
        });
    });
});

module.exports = router;