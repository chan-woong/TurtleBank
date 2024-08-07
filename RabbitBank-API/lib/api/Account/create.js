var express = require('express');
var router = express.Router();
var Model = require('../../../models/index');
var Response = require('../../Response');
var statusCodes = require('../../statusCodes');
var { validateUserToken, tokenCheck } = require("../../../middlewares/validateToken");
var { encryptResponse } = require("../../../middlewares/crypt");

/**
 * Account view route
 * @path - /api/account/view
 * @middleware
 * @return
 */
router.post('/', validateUserToken, (req, res) => {
    var r = new Response();
    const bank_code = 333;
    let username = req.username;
    let balance = 1000000;
    let account_number = Math.round(Math.random() * 888888 + 111111);          // 신규 계좌번호 랜덤 값으로 생성

    Model.account.findOne({          // select * from account where account_number = account_number;
        where: {
            account_number: account_number
        }
    }).then((data) => {
        if (data) {          // account 테이블에 신규 생성된 계좌번호가 이미 존재할 시
            account_number = Math.round(Math.random() * 888888 + 111111);
            Model.account.create({          // insert into account (username, balance, account_number, bank_code) values (username, balance, account_number, bank_code);
                username: username,
                balance: balance,
                account_number: account_number,
                bank_code: bank_code
            }).then((data) => {          // account 테이블에 계좌가 생성됐을 시
                r.status = statusCodes.SUCCESS;
                r.data = data;
                console.log(r.status);
                return res.json(encryptResponse(r));
            }).catch(() => { // account 테이블에 계좌가 생성되지 않았을 시
                r.status = statusCodes.NOT_AUTHORIZED;
                r.data = {
                    "message": "계좌 생성에 실패했습니다."
                };
                return res.json(encryptResponse(r));
            });
        } else {          // account 테이블에 신규 생성된 계좌번호가 존재하지 않을 시
            Model.account.create({          // insert into account (username, balance, account_number, bank_code) values (username, balance, account_number, bank_code);
                username: username,
                balance: balance,
                account_number: account_number,
                bank_code: bank_code
            }).then((data) => {          // account 테이블에 계좌가 생성됐을 시
                r.status = statusCodes.SUCCESS;
                r.data = data;
                console.log(r.status);
                return res.json(encryptResponse(r));
            }).catch(() => {          // account 테이블에 계좌가 생성되지 않았을 시
                r.status = statusCodes.NOT_AUTHORIZED;
                r.data = {
                    "message": "계좌 생성에 실패했습니다."
                };
                return res.json(encryptResponse(r));
            });
        }
    }).catch(() => {          
        r.status = statusCodes.NOT_AUTHORIZED;
        r.data = {
            "message": "다시 시도해주세요."
        };
        return res.json(encryptResponse(r));
    });
});

module.exports = router;
