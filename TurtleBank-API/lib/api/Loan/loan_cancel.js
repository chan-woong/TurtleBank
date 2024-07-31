var express = require('express');
var router = express.Router();
var Model = require('../../../models/index');
var Response = require('../../Response');
const Sequelize = require("sequelize");
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
router.post('/', [validateUserToken,decryptRequest], (req, res) => {               // from /loan.js/loan_cancel
    var r = new Response();
    let account_number = req.body.selected_account;
    let username = req.username;
    Model.account.findOne({          // select * from account where account_number = account_number
        where: {
            account_number: account_number
        },
        attributes: ["balance"]
    }).then((accountdata) => {

        Model.loan.findOne({          // select loan_time from loan where username = username;
            where: {
                username: username
            },
            attributes: ["loan_time", "loan_amount"]
        }).then((loanData) => {
            var loan_amount = loanData.dataValues.loan_amount;
            if (accountdata.balance >= loan_amount) {

                var loan_time = (loanData.dataValues.loan_time.getTime());
                
                var nowtime = ((new Date).getTime());
                if (nowtime <= loan_time + 86400000) {          // 지금 시간이 대출 시간 + 1일보다 작거나 같으면
                    Model.users.update({          // update users set is_loan = false where username = username;
                        is_loan: false
                    }, {
                        where: {
                            username: username
                        }
                    }).then(() => {

                        Model.account.update({          // update account sert balance = balance - default_loan_amount where account_number = account_number
                            balance: Sequelize.literal(`balance - ${loan_amount}`)
                        }, {
                            where: {
                                account_number: account_number
                            }
                        }).then(() => {

                            Model.loan.destroy({          // delet from loan where username = username
                                where: {
                                    username: username
                                }
                            }).then(() => {

                                r.status = statusCodes.SUCCESS;
                                r.data = {
                                    "message": "대출 취소가 완료되었습니다."
                                };
                                return res.json(encryptResponse(r));          // 성공 시 SUCCESS return
                            })
                        })
                    })
                } else {          // 지금 시간이 대출 + 1일보다 크면

                    r.status = statusCodes.BAD_INPUT;
                    r.data = {
                        "message": "대출 취소기간이 지났습니다."
                    };
                    return res.json(encryptResponse(r));
                }
            } else {          // 선택 계좌의 잔고가 50,000,000 보다 작으면
                r.status = statusCodes.BAD_INPUT;
                r.data = {
                    "message": "계좌에 잔액이 부족합니다."
                };
                return res.json(encryptResponse(r));          // BAD_INPUT return
            }


        })


    }).catch((err) => {

        r.status = statusCodes.SERVER_ERROR;
        r.data = {
            "message": "계좌를 선택해주세요."
        };
        return res.json(encryptResponse(r));
    });
})


module.exports = router;