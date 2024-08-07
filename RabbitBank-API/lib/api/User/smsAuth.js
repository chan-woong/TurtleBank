var express = require('express');
var router = express.Router();
var Model = require('../../../models/index');
var Response = require('../../Response');
var statusCodes = require('../../statusCodes');
var { encryptResponse, decryptRequest } = require("../../../middlewares/crypt");

/**
 * findPass route
 * This endpoint allows the user to login
 * @path                             - /api/user/smsAuth
 * @middleware                       - Checks admin authorization
 * @param username                   - username to smsAuth
 * @param phone                      - phone to smsAuth
 * @param authnum                    - authnum to smsAuth
 * @return                           - JWT token
 */

router.post('/', decryptRequest, (req, res) => {
    const r = new Response();
    let username = req.body.username;
    let authnum = req.body.authnum;          // 새로 입력받은 password와 인증번호
    let authnumInt = parseInt(authnum, 10);
    let next_new_password = req.body.next_new_password;
    let check_password = req.body.check_password;

    Model.smsauths.findOne({          // select username, authnum from smsauths where username = username;
        where: {
            username : username
        },
        attributes: ["username","authnum"]
    }).then((user) => {
        if (user) {
            if(user.authnum === authnumInt){          // smsauths테이블의 값과 user의 입력 값이 같은 경우
                Model.users.findOne({
                    where: {
                        username : username
                    },
                    attributes: ["username","password"]
                }).then((data) => {
                    if (data) {          
                        if(data.password === next_new_password){          // 새로 변경하는 비밀번호와 원래 비밀번호가 같은 경우
                            r.status = statusCodes.BAD_INPUT;
                            r.data = {
                                "message": "현재 비밀번호로는 변경할 수 없습니다."
                            };
                            return res.json(encryptResponse(r));
                        }
                        else{          // 새로 변경하는 비밀번호가 원래 비밀번호가 다른 경우
                            if(next_new_password != check_password){          // 새로 변경하는 비밀번호와 새 비밀번호 확인 값이 다른 경우
                                r.status = statusCodes.BAD_INPUT;
                                r.data = {
                                    "message": "비밀번호가 다릅니다."
                                };
                                return res.json(encryptResponse(r));
                            }else{          // 새 비밀번호 값과 새 비밀번호 확인 값이 같은 경우
                                Model.users.update({          // update users set password = next_new_password where username = user.username;
                                    password: next_new_password
                                }, {
                                    where: {
                                        username: user.username
                                    }
                                }).then(() => {          // 비밀번호가 성공적으로 변경
                                    r.status = statusCodes.SUCCESS;
                                    r.data = {
                                        "message": "비밀번호가 성공적으로 변경되었습니다."
                                    };
                                    return res.json(encryptResponse(r));
                                })
                            }
                        }
                    }
                })
            }else{          // 입력한 인증번호 값과 smsauths 인증번호 값이 다른 경우
                r.status = statusCodes.BAD_INPUT;
                r.data = {
                    "message": "인증번호가 다릅니다."
                };
                return res.json(encryptResponse(r));
            }
        } else {          // user가 존재하지 않는 경우
            r.status = statusCodes.BAD_INPUT;
            r.data = {
                "message": "no user"
            };
            return res.json(encryptResponse(r));
        }
    }).catch((err) => {
        console.error(err);
        r.status = statusCodes.SERVER_ERROR;
        r.data = {
            "message": "error"
        };
        return res.json(encryptResponse(r));
    });
});

module.exports = router;