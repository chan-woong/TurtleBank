var express = require('express');
var router = express.Router();
var Model = require('../../../models/index');
var Response = require('../../Response');
const { Sequelize } = require('../../../models_board');
var statusCodes = require('../../statusCodes'); 

router.post('/', function (req, res) {          // post로 요청받은 data     

    var f_ac = req.body.from_account;
    var t_ac = req.body.to_account;
    var amount = req.body.amount;
    var sendtime = req.body.sendtime;
    var from_bankcode = 333;
    var to_bankcode = req.body.bankcode;
    var r = new Response(); 

    Model.account.findOne({
        where: {
            account_number: t_ac
        },
        attributes: ["balance"]
    }).then((toAccountData) => {
        if (toAccountData) {
            // 출금 계좌 정보 조회
            Model.account.findOne({
                where: {
                    account_number: f_ac
                },
                attributes: ["balance"]
            }).then((fromAccountData) => {
                if (fromAccountData.balance >= amount) {
                    Model.account.update({
                        balance: Sequelize.literal(`balance - ${amount}`)
                    }, {
                        where: {
                            account_number: f_ac
                        }
                    }).then(() => {
                        Model.account.update({
                            balance: Sequelize.literal(`balance + ${amount}`)
                        }, {
                            where: {
                                account_number: t_ac
                            }
                        }).then(() => {
                            Model.transactions.create({
                                from_account: f_ac,
                                to_account: t_ac,
                                amount: amount,
                                sendtime: sendtime,
                                from_bankcode: from_bankcode,
                                to_bankcode: to_bankcode
                            }).then(() => {
                                r.status = statusCodes.SUCCESS;
                                r.data = {
                                    "message": "송금에 성공했습니다"
                                };
                                return res.json(r);
                            });
                        }).catch((err) => {
                            handleServerError(res, r);
                        });
                    });
                } else {
                    r.status = statusCodes.SERVER_ERROR;
                    r.data = {
                        "message": "입력값이 계좌 잔액보다 큽니다."
                    };
                    return res.json(r);
                }
            }).catch((err) => {
                handleServerError(res, r);
            });
        } else {
            // 수신 계좌가 존재하지 않는 경우
            r.status = statusCodes.SERVER_ERROR;
            r.data = {
                "message": "수신 계좌가 존재하지 않습니다."
            };
            return res.json(r);
        }
    }).catch((err) => {
        handleServerError(res, r);
    });
});

// 서버 오류 처리 함수
function handleServerError(res, response) {
    response.status = statusCodes.SERVER_ERROR;
    response.data = {
        "message": "송금에 실패했습니다."
    };
    return res.json(response);
}

module.exports = router;
