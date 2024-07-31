var express = require('express');
var router = express.Router();
const axios = require("axios");
const profile = require("../../middlewares/profile")
const {decryptRequest, encryptResponse} = require("../../middlewares/crypt")
const checkCookie = require("../../middlewares/checkCookie")
var {seoultime} = require('../../middlewares/seoultime');


router.get('/', checkCookie, function (req, res, next) {          // 대출 페이지 불러오기
    const cookie = req.cookies.Token;

    profile(cookie).then(pending => {
        const en_data = encryptResponse(JSON.stringify({username: pending.data.username}));
        axios({          // 대출 페이지 불러오기를 위한 api로 req
            method: "post",
            url: api_url + "/api/loan/loan",
            headers: {"authorization": "1 " + cookie},
            data: en_data
        }).then((data) => {
            let result_data = decryptRequest(data.data);
            let statusCode = result_data.status;
            let ac = result_data.data.account_number;
            let la = result_data.data.loan_amount;
            if (statusCode.code == 200) {          // users 테이블에 사용자가 is_loan = true면,
                var laFormatted = la.toLocaleString();
                var html_data = `
                <div class="text-center">
                    <h4 class="h4 text-gray-900 mb-4">대출 현황 및 상환</h4>
                </div>
                <div class="text-center">
                    <a style="color: red;">
                    대출 상환은 기존 계좌에서 출금하는 형식으로 진행됩니다.<br>
                    대출 취소는 대출받은 계좌에 전액이 있어야 가능합니다.
                    </a>
                </div>
                <form id="loan_form" name="loan_form" method="POST">
                    <select class="form-control form-control-user mb-3" aria-label="Large select example" name="selected_account" style="width: 100%;">
                    <option selected>계좌를 선택해 주세요.</option>
                    `;
                    ac.forEach(function (a) {
                        html_data +=`<option value= ${a}>${a}</option>`;
                    })
                    html_data += `</select>
                    <div class="form-control form-control-user mb-3" role="alert">
                        대출 잔액 : ${laFormatted} 원
                    </div>
                    <input type="text" class="form-control form-control-user mb-3" id="repayment_amount" name="repayment_amount" placeholder="상환 금액" style="width : 100%;">
                    <input type="submit" class="btn btn-user btn-block" name="repayment" value="대출 상환" formaction="/bank/loan/repayment" style="background-color:#00f !important; color:white !important;">
                    <input type="submit" class="btn btn-user btn-block" name="cancel" value="대출 취소" formaction="/bank/loan/cancel" style="background-color:#00f !important; color:white !important;">
                </form>  

                `;

            
            return res.render("Banking/loan", { html: html_data, pending: pending, select: "loan" });
        } else if (statusCode.code == 400) {          // user 테이블에 사용자가 is_loan = true면,
            var html_data =  `
            <div class="text-center">
                <h4 class="h4 text-gray-900 mb-4">Security 우대대출</h4>
            </div>
            <table class="table table-bordered" id="dataTable" width="100" cellspacing="0">
                <tr>
                    <td>상품특징</td>
                    <td>
                        <li>보안 직종에 종사하는 직장인들을 위한 맞춤 신용대출 상품 </li>
                    </td>
                </tr>
                <tr>
                    <td>대출신청자격</td>
                    <td>
                        <li>보안 직무(모의해킹 등)를 수행하는 보안 관련 전체 고객</li>                             
                    </td>
                <tr>
                    <td>대출금액</td>
                    <td>
                        <li>보안하느라 지친 당신을 위한 특별 서비스 !</li>
                        <li>대출 금액은 고객님의 멤버십과 상관없이 이자 없이 5000만 원으로 고정됩니다.</li>
                    </td>
                </tr>
            </table>

            <form id="get_debt" action="/bank/loan/get_debt" method="POST" name="get_debt">
            <label> 당신의 등급은 ${pending.data.membership}이며, 대출 가능 금액은 50,000,000원 입니다. <br>
            <label for="acc">대출 받으실 계좌를 선택해주세요</label><br>
            <select class="form-control form-control-user mb-3" aria-label="Large select example" name="account_number" style="width: 100%;">
            <option selected>계좌를 선택해 주세요.</option>
            `;
            ac.forEach(function (a) {
                html_data +=`<option value= ${a}>${a}</option>`;
            })
            html_data += `</select>
                <input type="hidden" id="loan_amount" name="loan_amount" value="50000000"><br>
                <input type="hidden" name="username" id="username" value="${pending.data.username}"/> 
            </form>
            <a onclick="document.getElementById('get_debt').submit()" class="btn btn-user btn-block" id="submitbutton" style="background-color:#00f !important; color:white !important;">
            대출
            </a>
            `;
        
            return res.render("Banking/loan", { html: html_data, pending: pending, select: "loan"});
        }
        }).catch(function (err) {

            var html_data =  "<tr>에러 페이지 입니다.</tr>"
            return res.render("Banking/loan", { html: html_data, pending: pending, select: "loan" });
        });
    })
})

router.post("/get_debt", checkCookie, function (req, res, next) {          // 대출 요청
    const cookie = req.cookies.Token;
    let username = req.body.username;
    let loan_amount = req.body.loan_amount;
    let account_number = req.body.account_number;
    let loan_time = seoultime;

    const en_data = encryptResponse(JSON.stringify({account_number:account_number,username: username, loan_amount: loan_amount, loan_time: loan_time}));
    axios({          // 대출 신청을 위한 api로 req
        method: "post",
        url: api_url + "/api/loan/get_debt",
        headers: {"authorization": "1 " + cookie},
        data: en_data
    }).then((data) => {
        result = decryptRequest(data.data);
        statusCode = result.data.status;
        message = result.data.message;

        if(statusCode != 200) {          // 성공하면, 성공 메시지
            res.send(`<script>
            alert("${message}");
            location.href=\"/bank/loan\";
            </script>`);
        } else {          // 실패하면, 실패 메시지
            res.send(`<script>
            alert("${message}");
            location.href=\"/bank/loan\";
            </script>`);
        }
    });
});

router.post('/repayment', checkCookie, function (req, res, next) {          // 대출 상환
    const cookie = req.cookies.Token;

    profile(cookie).then(pending => {
        let selected_account = req.body.selected_account;
        let repayment_amount = req.body.repayment_amount;
        const en_data = encryptResponse(JSON.stringify({selected_account: selected_account, repayment_amount: repayment_amount, username: pending.data.username}));
        axios({          // 대출 상환을 위한 api로 req
            method: "post",
            url: api_url + "/api/loan/repayment",
            headers: {"authorization": "1 " + cookie},
            data: en_data
        }).then((data) => {
            result = decryptRequest(data.data);
            statusCode = result.data.status;
            message = result.data.message;

            if(statusCode != 200) {          // 성공하면, 성공 메시지
                res.send(`<script>
                alert("${message}");
                location.href=\"/bank/loan\";
                </script>`);
            } else {          // 실패하면, 실패 메시지
                res.send(`<script>
                alert("${message}");
                location.href=\"/bank/loan\";
                </script>`);
            }
        });
    });
});

router.post('/cancel', checkCookie, function (req, res, next) {          // 대출 취소
    const cookie = req.cookies.Token;
    let selected_account = req.body.selected_account;
    profile(cookie).then(pending => {
        const en_data = encryptResponse(JSON.stringify({username: pending.data.username, selected_account: selected_account}));
        axios({          // 대출 취소를 위한 api로 req
            method: "post",
            url: api_url + "/api/loan/loan_cancel",
            headers: {"authorization": "1 " + cookie},
            data: en_data
        }).then((data) =>{
            result = decryptRequest(data.data);
            statusCode = result.data.status;
            message = result.data.message;

            if(statusCode != 200) {          // 성공하면, 성공 메시지
                res.send(`<script>
                alert("${message}");
                location.href=\"/bank/loan\";
                </script>`);
            } else {          // 실패하면, 실패 메시지
                res.send(`<script>
                alert("${message}");
                location.href=\"/bank/loan\";
                </script>`);
            }
        })
    });
});

module.exports = router;