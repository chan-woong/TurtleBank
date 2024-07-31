var express = require('express');
var router = express.Router();
var Model = require('../../../models/index');
var Response = require('../../Response');
var statusCodes = require('../../statusCodes');
var { encryptResponse, decryptRequest } = require("../../../middlewares/crypt");
const jwt = require("jsonwebtoken");

/**
 * Login route
 * This endpoint allows the user to login
 * @path                             - /api/user/login
 * @middleware                       - Checks admin authorization
 * @param username                   - Username to login
 * @param password                   - Password to login
 * @return                           - JWT token
 */
router.post('/', decryptRequest, (req, res) => {
    var r = new Response();
    let username = req.body.username;
    let password = req.body.password;

    Model.users.findOne({          // select * from users where username = username, password = password;
        where: {
            username: username,
            password: password
        }
    }).then((data) => {
        if(data) {          // 로그인 입력값이 옳바른 경우
            const accessToken = jwt.sign({          // jwt 토큰 생성 (payload : is_admin, username)
                username: data.username,
                is_admin: data.is_admin
            }, "secret",{expiresIn: "30m"});          // 비밀키 : secret, 유효시간 : 30분
            r.status = statusCodes.SUCCESS;
            r.data = {
                "accessToken": accessToken
            };
            return res.json(encryptResponse(r));
        } else {          // 로그인 입력값이 옳바르지 않는 경우
            r.status = statusCodes.BAD_INPUT;
            r.data = {
                "message": "Incorrect username or password"
            }
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