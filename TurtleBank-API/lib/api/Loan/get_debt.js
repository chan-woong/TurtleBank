var express = require('express');
var router = express.Router();
var Model = require('../../../models/index');
const Sequelize = require("sequelize");
var Response = require('../../Response');
var statusCodes = require('../../statusCodes');
var { validateUserToken } = require("../../../middlewares/validateToken");
var { encryptResponse, decryptRequest} = require("../../../middlewares/crypt");

/**
 * Beneficiary approve route
 * This endpoint allows to view is_loan of any user
 * @path                             - /api/loan/loan
 * @middleware                       - Checks admin authorization
 * @param loan_amount
 * @param username
 * @return                           - Status
 */
router.post('/', [validateUserToken,decryptRequest], (req, res) => {          // from /loan.js/get_debt
    var r = new Response();
    let username = req.username;
    let loan_amount = req.body.loan_amount;
    let account_number = req.body.account_number;
    let loan_time = req.body.loan_time;

    Model.account.findOne({          // select balance from account where account_number = account_number
        where: {
            account_number: account_number
        },
        attributes: ["balance"]
    }).then((data) => {
        if (data) {          // account 테이블에 account_number가 있으면,
            Model.users.update({          // update users set is_loan = true where username = username;
                is_loan: true
            }, {
                where : {
                    username: username
                }
            }).then(() => {
                Model.loan.create({          // insert into loan (username, loan_amount, loan_time) values (username, loan_amount, loan_time);
                    username: username,
                    loan_amount: loan_amount,
                    loan_time: loan_time
                }).then(() => {
                    Model.account.update({          // update account set balance = balance + loan_amount where account_number = account_number and username = username;
                        balance: Sequelize.literal(`balance + ${loan_amount}`)
                    }, {
                        where: {
                            account_number: account_number,
                            username : username
                        }
                    }).then(() => {
                        r.status = statusCodes.SUCCESS;
                        r.data = {
                            "message": "대출되었습니다."
                        };
                        return res.json(encryptResponse(r));          // 성공 시 SUCCESS return 
                    });
                });
            });
        } else {
            r.status = statusCodes.BAD_INPUT;
            r.data = {
                "message": "계좌를 선택해주세요."
            };
            return res.json(encryptResponse(r));          // 실패 시 BAD_INPUT return
        }
    }).catch((err) => {
        r.status = statusCodes.SERVER_ERROR;
        r.data = {
            "message": "대출에 실패하셨습니다."
        };
        return res.json(encryptResponse(r));
    });
})


module.exports = router;