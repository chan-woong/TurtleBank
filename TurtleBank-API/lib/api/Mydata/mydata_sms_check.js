var express = require('express');
var axios = require("axios");
var router = express.Router();
var Model = require('../../../models/index');
var Response = require('../../Response');
var statusCodes = require('../../statusCodes');
var { validateUserToken, tokenCheck } = require("../../../middlewares/validateToken");
var { encryptResponse, decryptRequest } = require("../../../middlewares/crypt");
const { underscoredIf } = require('sequelize/lib/utils');
/**
 * Account view route
 * @path - /api/mydata/mydata_sms
 * @middleware
 * @return
 */

router.post('/', 
//	[validateUserToken, decryptRequest],
	validateUserToken,
	(req, res) => {          // user가 보낸 인증번호 수신
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
