var express = require('express');
var axios = require("axios");
var router = express.Router();
var Model = require('../../../models/index');
var Response = require('../../Response');
var statusCodes = require('../../statusCodes');
var { validateUserToken, tokenCheck } = require("../../../middlewares/validateToken");
var { encryptResponse, decryptRequest } = require("../../../middlewares/crypt");
/**
 * Account view route
 * @path - /api/mydata/mydata_sms
 * @middleware
 * @return
 */
function generateRandomVerificationCode() {          // 인증번호 랜덤 값 생성
    const min = 1000;
    const max = 9999;
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

router.get('/', validateUserToken, (req,res)=>{          // 마이데이터 요청하기
    var r = new Response();
    var username = req.username;
    const coolsms = require('coolsms-node-sdk').default;
    const messageService = new coolsms('NCS2ULU0PYWR4DU8', 'LHQVWAJRESNTB8W9SBRJM5LBEIOZPI2D');          // coolsms API 키 값 설정
    const auth_num = generateRandomVerificationCode();
    const auth_num_str = auth_num.toString();

    Model.users.findOne({          // select phone from users where username = username;
        where:{
            username: username

        }, attributes:['phone']
    }).then((data)=>{  
        var phone = data.dataValues.phone;
        axios({          // 마이데이터 요청을 한 유저가 B은행에 존재하는지 확인하는 요청
            method: "post",
            url : different_api + "api/mydata/b_api",
            data : data.dataValues        

        }).then((bdata)=>{
            if (bdata.data.status.code == 200) {          // B은행에 마이데이터 요청을 한 유저가 존재하는 경우
                Model.smsauths.findOne({          // select username, authnum from smsauths where username = username;
                    where: {
                        username: username
                    },
                    attributes: ["username", "authnum"]        
                }).then((smsData) => {
                    if (smsData) {          // smsauths 테이블에 유저의 row가 존재하는 경우, 인증번호 update
                        Model.smsauths.update({
                            authnum: auth_num
                        }, {
                            where: {
                                username: username
                            }
                        })
                    } else {          // smsauths 테이블에 유저의 row가 존재하지 않는 경우, 인증번호 create
                        Model.smsauths.create({
                            username: username,
                            authnum: auth_num
                        })
                    }
                }).catch((err) => {
                    r.status = statusCodes.SERVER_ERROR;
                    r.data = {
                        "message": "다시 시도해주세요."
                    };
                    return res.json(encryptResponse(r));
                });
                
                messageService.sendOne(          // 마이데이터 요청한 user의 핸드폰으로 인증번호 전송
                    {
                    to: phone,
                    from: "01097252505",
                    text: "[인증번호] : " + auth_num_str + "를 입력해주세요."
                    }
                ).then(() => {          // 인증번호 전송이 성공한 경우
                    r.status = statusCodes.SUCCESS;
                    r.data = {
                        "message": "인증번호를 전송하였습니다."
                    };
                    return res.json(encryptResponse(r));
                }).catch((err) => {
                    r.status = statusCodes.SERVER_ERROR;
                    r.data = {
                        "message": "다시 시도해주세요."
                    };
                    return res.json(encryptResponse(r));
                });

            } else {          // B은행에 마이데이터 요청을 한 유저가 존재하지 않는 경우
                r.status = statusCodes.BAD_INPUT;
                r.data = {
                    "message": "다른 은행에 사용자의 계좌가 존재하지 않습니다."
                };
                return res.json(encryptResponse(r));
            }
        }).catch((err)=>{
            r.status = statusCodes.BAD_INPUT;
            r.data = {
                "message": err.toString()
            };
            return res.json(encryptResponse(r));
        })                                    
    }).catch((err)=>{
        r.status = statusCodes.BAD_INPUT;
        r.data = {
            "message": err.toString()
        };
        return res.json(encryptResponse(r));                                
    })
});


router.post('/', [validateUserToken, decryptRequest], (req, res) => {          // user가 보낸 인증번호 수신
    var r = new Response();
    var username = req.username;
    var authnum = req.body.authnum;
 
    
    Model.smsauths.findOne({          // select username, authnum from smsauths where username = username;
        where: {
            username: username
        },
        attributes: ["username", "authnum"]
        }).then((smsData) => {
            if (authnum == smsData.dataValues.authnum) {          // smsauths에 저장된 인증번호와 유저가 보낸 인증번호가 일치하는 경우
                Model.users.update({          // update users set is_mydata = true where username = username;
                    is_mydata: true
                }, {  where: {
                        username: username
                    }
                }). then(() => {
                    r.status = statusCodes.SUCCESS;
                    r.data = {
                        "message": "마이데이터 연동 인증되었습니다."
                    };
                    return res.json(encryptResponse(r));
                })
            } else {          // smsauths에 저장된 인증번호와 유저가 보낸 인증번호가 일치하지 않는 경우
                r.status = statusCodes.BAD_INPUT;
                r.data = {
                    "message": "인증번호가 일치하지 않습니다."
                };
                return res.json(encryptResponse(r));
            }

        }).catch((err) => {
            r.status = statusCodes.SERVER_ERROR;
            r.data = {
                "message": "다시 시도해주세요."
            };
            return res.json(encryptResponse(r));
        });
    });



module.exports = router;