var express = require('express');
var router = express.Router();
var Model = require('../../../models/index');
const Sequelize = require("sequelize");
var Response = require('../../Response');
var statusCodes = require('../../statusCodes');
var { validateUserToken } = require("../../../middlewares/validateToken");
var { encryptResponse,decryptRequest } = require("../../../middlewares/crypt");
/**
 * Beneficiary approve route
 * This endpoint allows to view is_loan of any user
 * @path                             - /api/loan/loan
 * @middleware                       - Checks admin authorization
 * @return                           - Status
 */
router.post('/', [validateUserToken,decryptRequest], (req, res) => {          // from /loan.js/repayment
    var r = new Response();
    let account_number = req.body.selected_account;
    let repayment_amount = req.body.repayment_amount;
    let username = req.username;

    if (repayment_amount <= 0) {          // 입력한 상환 금액이 0보다 작거나 같으면 BAD_INPUT return
        r.status = statusCodes.BAD_INPUT;
        r.data = {
            "message": "대출 상환금을 0원 이상 입력해주세요."
        };
        return res.json(encryptResponse(r));
    }

    Model.account.findOne({          // select balance from account where account_number = account_number;
        where: {
            account_number: account_number
        },
        attributes:["balance"]
    }).then((accountdata) => {
        if (accountdata.balance >= repayment_amount) {          // 계좌 잔액이 상환 금액보다 크면,
            Model.loan.findOne({          // select loan_amount from loan where username = username
                where: {
                    username: username
                },
                attributes:["loan_amount"]
            }).then((loanData) => {
                if (loanData.loan_amount > repayment_amount) {          // 대출 잔액이 입력한 상환 금액보다 크면
                    Model.loan.update({          // update loan set loan_amount = loan_amount - repayment_amount where and username = username;
                        loan_amount: Sequelize.literal(`loan_amount - ${repayment_amount}`)
                    }, {
                        where: {
                            username : username
                        }
                    }).then(() => {
                        Model.account.update({          // update account set balance = balance - repayment_amount where account_number = account_number;
                            balance: Sequelize.literal(`balance - ${repayment_amount}`)
                        }, {
                            where: {
                                account_number: account_number
                            }
                        }).then(() => {
                            r.status = statusCodes.SUCCESS;
                            r.data = {
                                "message": "대출금의 일부를 상환하셨습니다. "
                            };
                            return res.json(encryptResponse(r));          // 성공하면 부분 상환 성공 return
                        })
                    })
                } else if (loanData.loan_amount == repayment_amount) {          // 대출 잔액과 상환금액이 같으면
                    Model.users.update({          // update user set is_loan = true where username = username;
                        is_loan: false
                    }, {
                        where: {
                            username: username
                        }
                    }).then(() => {
                        Model.loan.destroy({          // delete  from loan where username = username;
                            where: {
                                username: username
                            }
                        }).then(() => {
                            Model.account.update({          // update account set balance = balance - repayment_amount where account_number = account_number;
                                balance: Sequelize.literal(`balance - ${repayment_amount}`)
                            }, {
                                where: {
                                    account_number: account_number
                                }
                            }).then(() => { 
                            r.status = statusCodes.SUCCESS;
                            r.data = {
                                "message": "대출금을 모두 상환하셨습니다."
                            };
                            return res.json(encryptResponse(r));          // 성공 시 대출 전체 상환 성공 return
                        })
                    })
                })
                } else {          // 대출 잔액이 입력 상환금액보다 작으면,
                r.status = statusCodes.BAD_INPUT;
                r.data = {
                    "message": "잔여 대출금 이하의 상환금을 입력해주세요."
                };
                return res.json(encryptResponse(r));          // BAD_INPUT return
                } 
            })

        } else if (accountdata.balance < repayment_amount) {          // 입력한 상환 금액보다 계좌 잔액이 적으면
            r.status = statusCodes.BAD_INPUT;
            r.data = {
                "message": "계좌에 잔액이 부족합니다."
            };
            return res.json(encryptResponse(r));          // BAD_INPUT return
        } 
    }).catch((err) => {
        r.status = statusCodes.SERVER_ERROR;
        r.data = {
            "message": "계좌를 선택해주세요."
        };
        return res.json(encryptResponse(r));
    });
})

module.exports = router;