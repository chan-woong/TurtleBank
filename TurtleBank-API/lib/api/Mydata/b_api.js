var express = require('express');
var router = express.Router();
var Model = require('../../../models/index');
var Response = require('../../Response');
var statusCodes = require('../../statusCodes');
var { encryptResponse, decryptRequest } = require("../../../middlewares/crypt");

router.post('/',(req, res) => {          // A은행에서 마이데이터를 요청한 user가 B은행에 존재하는지 확인
    var r = new Response();
    const phone = req.body.phone;
    
    Model.users.findOne({          // select username, phone from users where phone = phone;
        where: {
            phone: phone
        },  
        attributes: ["username", "phone"]
    }).then((data) => {
        if (data) {          // A은행에서 마이데이터를 요청한 user가 B은행에 존재하는 경우
            r.status = statusCodes.SUCCESS;
            r.data = {
                "message": "유저가 존재합니다."
            };
            return res.json(r);


        } else {          // A은행에서 마이데이터를 요청한 user가 B은행에 존재하지 않는 경우
            r.status = statusCodes.BAD_INPUT;
            r.data = {
                "message": "다른 은행에 사용자의 계좌가 존재하지 않습니다."
            };
            return res.json(r);

        }
    }).catch((err) => {
            r.status = statusCodes.SERVER_ERROR;
            r.data = {
                "message": "다시 시도해주세요."
            };
            return res.json(r);
        });
    })

module.exports = router;
