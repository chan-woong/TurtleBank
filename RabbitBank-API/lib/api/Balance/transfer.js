var express = require('express');
var router = express.Router();
var Model = require('../../../models/index');
const Sequelize = require("sequelize");
var Response = require('../../Response');
var statusCodes = require('../../statusCodes');
var { validateUserToken } = require("../../../middlewares/validateToken");
var { encryptResponse, decryptRequest } = require("../../../middlewares/crypt");

/**
 * Balance transfer route
 * @path                 - /api/balance/transfer
 * @middleware
 * @param to_account     - Amount to be transferred to this account
 * @param amount         - Amount to be transferred
 * @return               - Status
 */
router.post('/', [validateUserToken, decryptRequest], (req, res) => {          // from /trade_send.js/post
    var r = new Response();
    let from_account = req.body.from_account;
    let to_account = req.body.to_account;
    let amount = req.body.amount;
    let sendtime = req.body.sendtime;
    let username = req.username;
    let accountPW = req.body.accountPW == null ? req.body.hAccountPW : req.body.accountPW;
    if (amount <= 0) {          // 입력값이 0보다 작거나 같으면,
        r.status = statusCodes.BAD_INPUT;
        r.data = {
            "message": "입력 값을 0원 이상 입력해주세요."
        };
        return res.json(encryptResponse(r));          // BAD_INPUT return
    }
    
    Model.account.findOne({          // select username from account where account_number = from_account
        where: {
            account_number: from_account
        }, attributes: ["username"]

    }).then((data) => {
        if(username!=data.username){          // 파라미터로 받은 이름과, from_account의 이름이 다르면
            r.status = statusCodes.BAD_INPUT;
            r.data = {
                "message": "출금계좌가 고객님의 계좌가 아닙니다."
            };
            return res.json(encryptResponse(r));          // BAD_INPUT return
        }

        Model.users.findOne({          // select membership from users where username = data.username;
            where: {
                username: data.username
            },attributes:["membership", "accountPW"]

        }).then((data) => {
            if(accountPW != data.accountPW){
                r.status = statusCodes.BAD_INPUT;
                r.data = {
                    data : {
                        accountPW1: accountPW,
                        accountPW2 : data.accountPW
                    },
                    "message": "계좌 비밀번호가 다릅니다."
                };
                return res.json(encryptResponse(r));          // BAD_INPUT return
            }
            if(data.membership === "SILVER") {          // 멤버십이 실버면,
                if(amount > 1000000) {          // 입력값이 1000000보다 크거나 같으면,
                    r.status = statusCodes.BAD_INPUT;
                    r.data = {
                        "message": "송금 한도 초과입니다."
                    };
                    return res.json(encryptResponse(r));          // BAD_INPUT return
                }
                else{          // 입력값이 1000000 보다 작으면
                    Model.account.findOne({          // select * from account where account_number = to_account
                        where:{
                            account_number: to_account
                        },
                    }).then((data)=>{
                        if(data){          // account 테이블에 받는계좌가 있으면,
                            Model.account.findOne({          // select balace, bank_code from account where account_number = from_account;
                                where: {
                                    account_number: from_account
                                },
                                attributes: ["balance", "bank_code"],
                            }).then((data2) => {
                                if (data2.balance >= amount) {          // 입력값보다 보내는 계좌의 잔액이 더 크면,
                                    Model.transactions.create({          // insert into transactions (from_bankcode, from_account, to_bankcode, to_account, amount, sendtime) values ('data2.bank_code', 'from_account', 'data.bank_code', 'to_account', amount, 'sendtime');
                                        from_bankcode: data2.bank_code,
                                        from_account: from_account,
                                        to_bankcode: data.bank_code,
                                        to_account: to_account,
                                        amount: amount,
                                        sendtime: sendtime
                                    }).then(() => {
                                        Model.account.update({          // update account set balace = balance - amount where account_number = from_account;
                                            balance: Sequelize.literal(`balance - ${amount}`)
                                        }, {
                                            where: {
                                                account_number: from_account
                                            }
                                        }).then(() => {
                                            Model.account.update({          // update account set balance = balance + amount where account_number = to_account;
                                                balance: Sequelize.literal(`balance + ${amount}`)
                                            }, {
                                                where: {
                                                    account_number: to_account
                                                }
                                            }).then(() => {
                                                r.status = statusCodes.SUCCESS;
                                                r.data = {
                                                    "message": "송금에 성공했습니다."
                                                };
                                                return res.json(encryptResponse(r));          // 성공하면 SUCCESS return
                                            });
                                        });
                                    });
                                } else {          // 입력값보다 보내는 계좌의 잔액이 더 작으면,
                                    r.status = statusCodes.BAD_INPUT;
                                    r.data = {
                                        "message": "계좌의 잔액이 송금액보다 적습니다."
                                    };
                                    return res.json(encryptResponse(r));          // BAD_INPUT return
                                }
                            }).catch((err) => {
                                r.status = statusCodes.SERVER_ERROR;
                                r.data = {
                                    "message": "송금에 실패했습니다."
                                };
                                console.log(r.data);
                                return res.json(encryptResponse(r));
                            })
                        }
                        else {          // account 테이블에 받는 계좌가 없으면,
                            r.status = statusCodes.BAD_INPUT;
                            r.data = {
                                "message": "입금계좌가 존재하지 않습니다."
                            };
                            return res.json(encryptResponse(r));          // BAD_INPUT return
                    }
                });
                
                }
            }

            else if(data.membership === "GOLD"){          // 멤버십이 골드면,
                if(amount > 10000000){          // 입력값이 10000000보다 크거나 같으면,
                    r.status = statusCodes.BAD_INPUT;
                    r.data = {
                        "message": "송금 한도 초과입니다."
                    }
                    return res.json(encryptResponse(r));          // BAD_INPUT return
                }
                else{          // 입력값이 10000000 보다 작으면
                    Model.account.findOne({          // select * from account where account_number = to_account
                        where:{
                            account_number: to_account
                        },
                    }).then((data)=>{
                        if(data){          // account 테이블에 받는계좌가 있으면,
                            Model.account.findOne({          // select balace, bank_code from account where account_number = from_account;
                                where: {
                                    account_number: from_account
                                },
                                attributes: ["balance", "bank_code"],
                            }).then((data2) => {
                                if (data2.balance >= amount) {          // 입력값보다 보내는 계좌의 잔액이 더 크면,
                                    Model.transactions.create({          // insert into transactions (from_bankcode, from_account, to_bankcode, to_account, amount, sendtime) values ('data2.bank_code', 'from_account', 'data.bank_code', 'to_account', amount, 'sendtime');
                                        from_bankcode: data2.bank_code,
                                        from_account: from_account,
                                        to_bankcode: data.bank_code,
                                        to_account: to_account,
                                        amount: amount,
                                        sendtime: sendtime
                                    }).then(() => {
                                        Model.account.update({          // update account set balace = balance - amount where account_number = from_account;
                                            balance: Sequelize.literal(`balance - ${amount}`)
                                        }, {
                                            where: {
                                                account_number: from_account
                                            }
                                        }).then(() => {
                                            Model.account.update({          // update account set balance = balance + amount where account_number = to_account;
                                                balance: Sequelize.literal(`balance + ${amount}`)
                                            }, {
                                                where: {
                                                    account_number: to_account
                                                }
                                            }).then(() => {
                                                r.status = statusCodes.SUCCESS;
                                                r.data = {
                                                    "message": "송금에 성공했습니다."
                                                };
                                                return res.json(encryptResponse(r));          // 성공하면 SUCCESS return
                                            });
                                        });
                                    });
                                } else {          // 입력값보다 보내는 계좌의 잔액이 더 작으면,
                                    r.status = statusCodes.BAD_INPUT;
                                    r.data = {
                                        "message": "계좌의 잔액이 송금액보다 적습니다."
                                    };
                                    return res.json(encryptResponse(r));          // BAD_INPUT return
                                }
                            }).catch((err) => {
                                r.status = statusCodes.SERVER_ERROR;
                                r.data = {
                                    "message": "송금에 실패했습니다."
                                };
                                console.log(r.data);
                                return res.json(encryptResponse(r));
                            })
                        }
                        else {          // account 테이블에 받는 계좌가 없으면,
                            r.status = statusCodes.BAD_INPUT;
                            r.data = {
                                "message": "입금계좌가 존재하지 않습니다."
                            };
                            return res.json(encryptResponse(r));          // BAD_INPUT return
                    }
                });
                
                }
            }

            else if(data.membership === "PLATINUM"){          // 멤버십이 플래티넘이면,
                if(amount > 100000000){          // 입력값이 100000000보다 크거나 같으면,
                    r.status = statusCodes.BAD_INPUT;
                    r.data = {
                        "message": "송금 한도 초과입니다."
                    }
                    return res.json(encryptResponse(r));          // BAD_INPUT return
                }    
                else{          // 입력값이 100000000 보다 작으면
                    Model.account.findOne({          // select * from account where account_number = to_account
                        where:{
                            account_number: to_account
                        },
                    }).then((data)=>{
                        if(data){          // account 테이블에 받는계좌가 있으면,
                            Model.account.findOne({          // select balace, bank_code from account where account_number = from_account;
                                where: {
                                    account_number: from_account
                                },
                                attributes: ["balance", "bank_code"],
                            }).then((data2) => {
                                if (data2.balance >= amount) {          // 입력값보다 보내는 계좌의 잔액이 더 크면,
                                    Model.transactions.create({          // insert into transactions (from_bankcode, from_account, to_bankcode, to_account, amount, sendtime) values ('data2.bank_code', 'from_account', 'data.bank_code', 'to_account', amount, 'sendtime');
                                        from_bankcode: data2.bank_code,
                                        from_account: from_account,
                                        to_bankcode: data.bank_code,
                                        to_account: to_account,
                                        amount: amount,
                                        sendtime: sendtime
                                    }).then(() => {
                                        Model.account.update({          // update account set balace = balance - amount where account_number = from_account;
                                            balance: Sequelize.literal(`balance - ${amount}`)
                                        }, {
                                            where: {
                                                account_number: from_account
                                            }
                                        }).then(() => {
                                            Model.account.update({          // update account set balance = balance + amount where account_number = to_account;
                                                balance: Sequelize.literal(`balance + ${amount}`)
                                            }, {
                                                where: {
                                                    account_number: to_account
                                                }
                                            }).then(() => {
                                                r.status = statusCodes.SUCCESS;
                                                r.data = {
                                                    "message": "송금에 성공했습니다."
                                                };
                                                return res.json(encryptResponse(r));          // 성공하면 SUCCESS return
                                            });
                                        });
                                    });
                                } else {          // 입력값보다 보내는 계좌의 잔액이 더 작으면,
                                    r.status = statusCodes.BAD_INPUT;
                                    r.data = {
                                        "message": "계좌의 잔액이 송금액보다 적습니다."
                                    };
                                    return res.json(encryptResponse(r));          // BAD_INPUT return
                                }
                            }).catch((err) => {
                                r.status = statusCodes.SERVER_ERROR;
                                r.data = {
                                    "message": "송금에 실패했습니다."
                                };
                                console.log(r.data);
                                return res.json(encryptResponse(r));
                            })
                        }
                        else {          // account 테이블에 받는 계좌가 없으면,
                            r.status = statusCodes.BAD_INPUT;
                            r.data = {
                                "message": "입금계좌가 존재하지 않습니다."
                            };
                            return res.json(encryptResponse(r));          // BAD_INPUT return
                    }
                });
                
                }
            }
            else{          // admin 등 다른 멤버십이면,                          
                Model.account.findOne({          // select * from account where account_number = to_account
                    where:{
                        account_number: to_account
                    },
                }).then((data)=>{
                    if(data){          // account 테이블에 받는계좌가 있으면,
                        Model.account.findOne({          // select balace, bank_code from account where account_number = from_account;
                            where: {
                                account_number: from_account
                            },
                            attributes: ["balance", "bank_code"],
                        }).then((data2) => {
                            if (data2.balance >= amount) {          // 입력값보다 보내는 계좌의 잔액이 더 크면,
                                Model.transactions.create({          // insert into transactions (from_bankcode, from_account, to_bankcode, to_account, amount, sendtime) values ('data2.bank_code', 'from_account', 'data.bank_code', 'to_account', amount, 'sendtime');
                                    from_bankcode: data2.bank_code,
                                    from_account: from_account,
                                    to_bankcode: data.bank_code,
                                    to_account: to_account,
                                    amount: amount,
                                    sendtime: sendtime
                                }).then(() => {
                                    Model.account.update({          // update account set balace = balance - amount where account_number = from_account;
                                        balance: Sequelize.literal(`balance - ${amount}`)
                                    }, {
                                        where: {
                                            account_number: from_account
                                        }
                                    }).then(() => {
                                        Model.account.update({          // update account set balance = balance + amount where account_number = to_account;
                                            balance: Sequelize.literal(`balance + ${amount}`)
                                        }, {
                                            where: {
                                                account_number: to_account
                                            }
                                        }).then(() => {
                                            r.status = statusCodes.SUCCESS;
                                            r.data = {
                                                "message": "송금에 성공했습니다."
                                            };
                                            return res.json(encryptResponse(r));          // 성공하면 SUCCESS return
                                        });
                                    });
                                });
                            } else {          // 입력값보다 보내는 계좌의 잔액이 더 작으면,
                                r.status = statusCodes.BAD_INPUT;
                                r.data = {
                                    "message": "송금에 실패했습니다."
                                };
                                return res.json(encryptResponse(r));          // BAD_INPUT return
                            }
                        }).catch((err) => {
                            r.status = statusCodes.SERVER_ERROR;
                            r.data = {
                                "message": "송금에 실패했습니다."
                            };
                            console.log(r.data);
                            return res.json(encryptResponse(r));
                        })
                    }
                    else {          // account 테이블에 받는 계좌가 없으면,
                        r.status = statusCodes.BAD_INPUT;
                        r.data = {
                            "message": "송금에 실패했습니다."
                        };
                        return res.json(encryptResponse(r));
                }
            });
            
        }
    }).catch((err)=>{
        r.status = statusCodes.BAD_INPUT;
        r.data = {
            "message": "출금 계좌가 존재하지 않습니다."
        };
        return res.json(encryptResponse(r));
    })
}); //Membership을 이용한 송금 한도 통과.
    
});

module.exports = router;