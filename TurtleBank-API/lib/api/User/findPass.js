var express = require('express');
var router = express.Router();
var Model = require('../../../models/index');
var Response = require('../../Response');
var statusCodes = require('../../statusCodes');
var { encryptResponse, decryptRequest } = require("../../../middlewares/crypt");
var fs = require("fs");

/**
 * findPass route
 * This endpoint allows the user to login
 * @path                             - /api/user/findPass
 * @middleware                       - Checks admin authorization
 * @param username                   - username to findPass
 * @param phone                      - phone to findPass
 * @return                           - JWT token
 */

router.post('/', decryptRequest, (req, res) => {
    var r = new Response();
    let username = req.body.username;
    let phone = req.body.phone;

    Model.users.findOne({          // select username, phone from users where username = username;
        where: {
            username: username
        },
        attributes: ["username","phone"]
    }).then((user) => {           
        if (user) {          // 해당 user가 존재하는 경우
            if(username == "admin"){          // 해당 user가 admin인 경우 비밀번호 변경 불가
                r.status = statusCodes.BAD_INPUT;
                r.data = {
                    "message": "해당 계정은 접근이 불가함"
                };
                return res.json(encryptResponse(r));
            }
            if (user.username == username) {          // username이 일치하는 경우
                r.status = statusCodes.SUCCESS;
                    r.data = {
                        "message": "인증번호가 발송되었습니다."
                    };
                    return res.json(encryptResponse(r));
            } else {          // username이 일치하지 않는 경우
                r.status = statusCodes.BAD_INPUT;
                r.data = {
                    "message": "아이디를 확인해주세요"
                };
                return res.json(encryptResponse(r));
            }
        } else {          // 해당 user가 존재하지 않는 경우
            r.status = statusCodes.BAD_INPUT;
            r.data = {
                "message": "아이디를 확인해주세요"
            };
            return res.json(encryptResponse(r));
        }
    }).catch((err) => {
        r.status = statusCodes.SERVER_ERROR;
        r.data = {
            "message": err.toString()
        };
        res.json(encryptResponse(r));
    });
});

router.post('/setSmsauths', decryptRequest, (req, res) => {
    var r = new Response();
    let username = req.body.username;
    let phone = req.body.phone;
    let auth_num_str = req.body.auth_num_str;
    Model.smsauths.findOne({
        where: {
            username: username
        }
    }).then((data) => {
        if(data !== null){
            Model.smsauths.update({
                authnum: auth_num_str
            }, {
                where: {
                    username: username
                }
            }).then(() => {res.send();});
        }
        else{
            Model.smsauths.create({
                username: username,
                authnum: auth_num_str
            }).then(() => {res.send();});
        }
    });
});

module.exports = router;
