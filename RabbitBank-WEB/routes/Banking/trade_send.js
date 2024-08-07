var express = require('express');
var router = express.Router();
const axios = require("axios");
const profile = require("../../middlewares/profile");
const { decryptRequest, encryptResponse } = require("../../middlewares/crypt");
const checkCookie = require("../../middlewares/checkCookie");
var { seoultime } = require('../../middlewares/seoultime');
const sha256 = require("js-sha256");

router.get("/", checkCookie, async (req, res) => {
    const cookie = req.cookies.Token;
    profile(cookie).then((data) => {
        const en_data = encryptResponse(JSON.stringify({ username: data.data.username }));
        axios({
            method: "post",
            url: api_url + "/api/beneficiary/account",
            headers: { "authorization": "1 " + cookie },
            data: en_data
        }).then((data2) => {
            var d = decryptRequest((data2.data));
            var results = d.data.accountdata;
            var html_data = `
                <input type="text" class="form-control form-control-user" autocomplete="off" id="drop_from" name="from_account" placeholder="보내는 계좌번호" list="dropdown_from">
                <datalist id="dropdown_from">`;
            results.forEach(function (a) {
                html_data += `<option value="${a}"></option>`;
            });

            html_data += `</datalist><br>`;

            html_data += `<input type="text" class="form-control form-control-user mb-3" id="to_account" name="to_account" placeholder="대상 계좌번호" > `;
            res.render("Banking/trade_send", { pending: data, html: html_data, select: "send" });
        }).catch(err => {
            console.error("Error fetching beneficiary account data:", err);
            res.status(500).send("Failed to fetch beneficiary account data");
        });
    }).catch(err => {
        console.error("Error fetching user profile data:", err);
        res.status(500).send("Failed to fetch user profile data");
    });
});

router.post("/post", checkCookie, function (req, res, next) {
    const cookie = req.cookies.Token;
    profile(cookie).then((data) => {
        const { from_account, to_account, amount, accountPW } = req.body;

        // Validate account numbers
        if (!isValidAccountNumber(from_account) || !isValidAccountNumber(to_account)) {
            return res.status(400).send(`<script>
                alert("계좌번호는 6자리 숫자여야 합니다.");
                location.href="/bank/send";
            </script>`);
        }

        let json_data = {
            from_account: parseInt(from_account),
            to_account: parseInt(to_account),
            amount: parseInt(amount),
            sendtime: seoultime,
            accountPW: sha256(accountPW),
            username: data.data.username,
            membership: data.data.membership,
            is_admin: data.data.is_admin
        };

        const en_data = encryptResponse(JSON.stringify(json_data));

        axios({
            method: "post",
            url: api_url + "/api/balance/check_pw",
            headers: { "authorization": "1 " + cookie },
            data: en_data
        }).then((data) => {
            const result = decryptRequest(data.data);
            const statusCode = result.status.code;
            const message = result.data.message;

            if (statusCode == 200) {
                axios({
                    method: "post",
                    url: api_url + "/api/balance/transfer",
                    headers: { "authorization": "1 " + cookie },
                    data: en_data
                }).then((data) => {
                    const result = decryptRequest(data.data);
                    const statusCode = result.status.code;
                    const message = result.data.message;
                    if (statusCode != 200) {
                        return res.status(400).send(`<script>
                            alert("${message}");
                            location.href="/bank/send";
                        </script>`);
                    } else {
                        return res.status(200).send(`<script>
                            alert("${message}");
                            location.href="/bank/send";
                        </script>`);
                    }
                }).catch(err => {
                    console.error("Error transferring balance:", err);
                    return res.status(500).send(`<script>
                        alert("송금 중 오류가 발생했습니다.");
                        location.href="/bank/send";
                    </script>`);
                });
            } else {
                return res.status(400).send(`<script>
                    alert("${message}");
                    location.href="/bank/send";
                </script>`);
            }
        }).catch(err => {
            console.error("Error checking password:", err);
            return res.status(500).send(`<script>
                alert("계정 비밀번호 확인 중 오류가 발생했습니다.");
                location.href="/bank/send";
            </script>`);
        });
    }).catch(err => {
        console.error("Error fetching user profile data:", err);
        return res.status(500).send(`<script>
            alert("사용자 정보를 불러오는 중 오류가 발생했습니다.");
            location.href="/bank/send";
        </script>`);
    });
});

function isValidAccountNumber(accountNumber) {
    // Check if accountNumber is a 6-digit number
    return /^\d{6}$/.test(accountNumber);
}

module.exports = router;