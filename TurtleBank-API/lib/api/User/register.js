var express = require('express');
var router = express.Router();
var Model = require('../../../models/index');
var Response = require('../../Response');
var statusCodes = require('../../statusCodes');
var {encryptResponse, decryptRequest} = require("../../../middlewares/crypt");

/**
 * Registration route
 * This endpoint allows the user to register
 * Additionally this also creates a new account for this user
 * @path                             - /api/user/register
 * @middleware                       - Checks admin authorization
 * @param username                   - Username to login
 * @param password                   - Password to login
 * @param email
 * @param accountPW                        - email
 * @return                           - Status
 */
router.post('/', decryptRequest, (req, res) => {         
    var r = new Response();
    let username = req.body.username;
    let password = req.body.password;
    let email = req.body.email;
    let phone = req.body.phone;
    let sendtime = req.body.sendtime
    let account_number = Math.round(Math.random() * 888888 + 111111); // 초기에 제공되는 계좌번호 랜덤 값 생성
    let hAccountPW = req.body.hAccountPW;   

    Model.users.findAll({         // select * from users where username = username;
        where: {
            username: username
        }
    }).then((data) => {
        if (data == "") {         // users 테이블에 입력된 username이 존재하지 않는 경우
            Model.users.findOne({         // select * from users where account_number = account_nubmer;
                account_number: account_number
            }).then((data) => {
                if (data) {         
                    account_number = Math.round(Math.random() * 888888 + 111111);
                }

                Model.users.create({         // insert into users (username, password, email, phone, account_number, membership) values (username, password, email, phone, account_number, "SILVER");
                    username: username,
                    password: password,
                    email: email,
                    phone: phone,
                    account_number: account_number,
                    accountPW: hAccountPW,
                    membership: "SILVER"
                }).then(() => {
                    Model.transactions.create({         // insert into transactions (from_account, to_account, amount, sendtime, from_bankcode, to_bankcode) values (from_account, to_account, amount, sendtime, from_bankcode, to_bankcode);
                        from_account: 999999,
                        to_account: account_number,
                        amount: 1000000,
                        sendtime: sendtime,
                        from_bankcode:555,          //B은행에서 바꿔줘야 하는 값
                        to_bankcode:555
                    }).then(() => {
                        //          처음 생성된 계좌 account 테이블에 넣기
                        Model.account.create({          // insert into account (account_number, bank_code, username, balance) values (account_number, bank_code, username, balance);
                            account_number: account_number,
                            bank_code: 555,
                            username: username,
                            balance: 1000000
                        }).catch((err) => {
                            r.status = statusCodes.SERVER_ERROR;
                            r.data = {
                                data : err,
                                "message": "다시 시도해주세요."
                            };
                            res.json(encryptResponse(r));
                        });

                        //현재 사용하지 않는 수취인 관련 테이블 이거 없애면 회원가입이 안됨.
                        Model.beneficiaries.create({
                            account_number: account_number,
                            beneficiary_account_number: 999999,
                            approved: 1
                        }).then(() => {

                            Model.beneficiaries.create({
                                account_number: 999999,
                                beneficiary_account_number: account_number,
                                approved: 1
                            }).then(() => {
                                r.status = statusCodes.SUCCESS;
                                r.data = {
                                    "message": "회원가입에 성공하셨습니다."
                                }
                                res.json(encryptResponse(r));
                            }).catch((err) => {
                                r.status = statusCodes.SERVER_ERROR;
                                r.data = {
                                    "message": "다시 시도해주세요."
                                };
                                res.json(encryptResponse(r));
                            });
                        }).catch((err) => {
                            r.status = statusCodes.SERVER_ERROR;
                            r.data = {
                                "message": "다시 시도해주세요."
                            };
                            res.json(encryptResponse(r));
                        });
                    });
                }).catch((err) => {
                    r.status = statusCodes.SERVER_ERROR;
                    r.data = {
                        "message": "다시 시도해주세요."
                    };
                    res.json(encryptResponse(r));
                });
            });
        } else {          // 입력한 username이 이미 존재하는 경우
            r.status = statusCodes.BAD_INPUT;
            r.data = {
                "message": "해당 아이디는 이미 존재합니다."
            };
            return res.json(encryptResponse(r));
        }
    }).catch((err) => {
        r.status = statusCodes.SERVER_ERROR;
        r.data = {
            "message": err.toString()
        };
        return res.json(encryptResponse(r));
    });
});

module.exports = router;