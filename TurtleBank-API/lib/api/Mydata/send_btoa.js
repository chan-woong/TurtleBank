var express = require('express');
var router = express.Router();
var Model = require('../../../models/index');
var Response = require('../../Response');
const { Sequelize } = require('../../../models_board');
var statusCodes = require('../../statusCodes');
var { encryptResponse, decryptRequest } = require("../../../middlewares/crypt");

router.post('/',function(req, res) {          // B은행에서 출금하는 정보들을 수신

    var r = new Response();
    var amount = req.body.amount;
    var from_account = req.body.from_account;
    var to_account = req.body.to_account;
    var bank_code = req.body.bank_code;
    var sendtime = req.body.sendtime;

    Model.account.findOne({          // select balance from account where account_number = from_account;
        where: {          
            account_number: from_account
        },
        attributes: ["balance"]
    }).then((data) => {
        if (data.balance >= amount) {          // 출금하는 계좌에 출금액 이상의 잔액이 존재하는지 확인
            Model.account.update({          // update account set balance = (balance - amount) where account_number = from_account;
                balance: Sequelize.literal(`balance-${amount}`)          // 계좌의 잔액에서 출금하는 금액 차감         
            }, {
                where: {
                    account_number: from_account
                }
            }).then(() => {

                Model.transactions.create({          // insert into transactions (from_account, to_account, amount, sendtime, from_bankcode, to_bankcode) values (from_account, to_account, amount, sendtime, 333, to_bankcode);
                    from_account: from_account,          // B은행 transactions 테이블에 거래목록 추가
                    to_account: to_account,
                    amount: amount,
                    sendtime: sendtime,
                    from_bankcode: 555,
                    to_bankcode: bank_code
                }).then(() => {
                    r.status = statusCodes.SUCCESS;          // transactions 생성에 성공한 경우
                    r.data = {
                        "message": "송금에 성공했습니다."
                    };

                    return res.json(r);
                })
            }).catch((err) => {
                r.status = statusCodes.SERVER_ERROR;
                r.data = {
                    "message": "송금에 실패했습니다."
                };
                return res.json(r);
            })
        } else {          // 출금하는 계좌에 출금액 이상의 잔액이 존재하지 않는경우
            r.status = statusCodes.BAD_INPUT;
            r.data = {
                "message": "계좌의 잔액이 부족합니다."
            };
            return res.json(r);        
        }
    }).catch((err) => {
        r.status = statusCodes.SERVER_ERROR;
        r.data = {
            "message": "송금에 실패했습니다."
        };
        return res.json(r);
    })
})

module.exports = router;