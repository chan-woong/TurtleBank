var express = require('express');
var router = express.Router();
var Model = require('../../../models/index');
const Sequelize = require("sequelize");
var Response = require('../../Response');
var statusCodes = require('../../statusCodes');
var { validateUserToken } = require("../../../middlewares/validateToken");
var { encryptResponse, decryptRequest } = require("../../../middlewares/crypt");
const axios = require('axios');

/**
 * Balance transfer route
 * @path                 - /api/balance/transfer
 * @middleware
 * @param to_account     - Amount to be transferred to this account
 * @param amount         - Amount to be transferred
 * @param hAccountPW
 * @return               - Status
 */
router.post('/', [validateUserToken,decryptRequest], (req, res) => {          // B은행 계좌에서 A은행 계좌로 송금하는 경우
    var r = new Response();
    let from_account = req.body.from_account;
    let to_account = req.body.to_account;
    let bank_code = req.body.bank_code;
    let amount = req.body.amount;
    let sendtime = req.body.sendtime;
    let username = req.username;
    let hAccountPW = req.body.hAccountPW;

    Model.account.findOne({          // select username from account where account_number = from_account
        where: {
            account_number: from_account
        }, attributes: ["username"]

    }).then((data) => {
        if(username != data.username){          // 파라미터로 받은 이름과, from_account의 이름이 다르면
            r.status = statusCodes.BAD_INPUT;
            r.data = {
                "message": "출금계좌가 고객님의 계좌가 아닙니다."
            };
            return res.json(encryptResponse(r));          // BAD_INPUT return
        }
        

        Model.users.findOne({          // select membership from users where username = data.username;
            where: {
                username: data.username
            },attributes:["accountPW"]

        }).then((data) => {

            if(hAccountPW != data.accountPW){
                r.status = statusCodes.BAD_INPUT;
                r.data = {

                    "message": "계좌 비밀번호가 다릅니다."
                };
                return res.json(encryptResponse(r));          // BAD_INPUT return
            }
        })
    })

    if (amount < 0) {          // 출금 금액이 0원보다 적은 경우
        r.status = statusCodes.BAD_INPUT;
        r.data = {
            "message": "입력 값을 0원 이상 입력해주세요."
        };
        return res.json(encryptResponse(r));
    } else {
        // to_account가 데이터베이스에 존재하는지 확인
        Model.account.findOne({
            where: {
                account_number: to_account
            }
        }).then((toAccountData) => {
            if (toAccountData) {
                axios({
                    method: "post",
                url: different_api + "api/mydata/send_btoa",
                    data: { from_account: from_account, amount: amount, bank_code: bank_code, to_account: to_account, sendtime: sendtime, username: username }
                }).then((data) => {
                if (data.status == 200) {
                    
                        Model.account.update({
                            balance: Sequelize.literal(`balance + ${amount}`)
                        }, {
                            where: {
                                account_number: to_account
                            }
                        }).then(() => {
                        
                            Model.transactions.create({
                                from_account: from_account,
                                to_account: to_account,
                                amount: amount,
                                sendtime: sendtime,
                                from_bankcode: 333,
                                to_bankcode: bank_code
                            }).then(() => {

                                r.status = statusCodes.SUCCESS;
                                r.data = {
                                    "message": "송금에 성공했습니다."
                                };
                                return res.json(encryptResponse(r));
                            });
                        }).catch((err) => {
                            r.status = statusCodes.SERVER_ERROR;
                            r.data = {
                                "message": "송금에 실패했습니다."
                             };
                            return res.json(encryptResponse(r));
                        });
                    } else {
                        r.status = statusCodes.BAD_INPUT;
                        r.data = {
                            "message": "계좌의 잔액이 부족합니다."
                        };
                        return res.json(encryptResponse(r));
                    }
                }).catch((err) => {
                    r.status = statusCodes.SERVER_ERROR;
            r.data = {
                "message": "송금에 실패했습니다."
            };
            return res.json(encryptResponse(r));
                });
            } else {
                // to_account가 데이터베이스에 존재하지 않는 경우
                r.status = statusCodes.BAD_INPUT;
                r.data = {
                    "message": "수신 계좌가 존재하지 않습니다."
                };
                return res.json(encryptResponse(r));
            }
        }).catch((err) => {
            r.status = statusCodes.SERVER_ERROR;
            r.data = {
                "message": "송금에 실패했습니다."
            };
            return res.json(encryptResponse(r));
        });
    }
})

module.exports = router;