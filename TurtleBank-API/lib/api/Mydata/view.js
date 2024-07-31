var express = require('express');
var router = express.Router();
var Model = require('../../../models/index');
var Response = require('../../Response');
var statusCodes = require('../../statusCodes');
var { validateUserToken } = require("../../../middlewares/validateToken");
var { encryptResponse,decryptRequest } = require("../../../middlewares/crypt");

/**
 * Account view route
 * @path                 - /api/mydata/view
 * @middleware
 * @return               
 */
router.post('/', [validateUserToken,decryptRequest], (req, res) => {
    var r = new Response();

    Model.account.findAll({          // select balance, account_number, bank_code from account where username = req.username;
        where: {
            username: req.username
        },
        attributes: ["balance", "account_number", "bank_code"]
    }).then((data) => {
        if(data.length > 0) {          // account 테이블에 username의 계좌들이 존재하는 경우
            r.status = statusCodes.SUCCESS;
            r.data = data;
            return res.json(encryptResponse(r));
        } else {          // account 테이블에 username의 계좌들이 존재하지 않는 경우
            r.status = statusCodes.BAD_INPUT;
            r.data = {
                "message": "계좌가 존재하지 않습니다."
            }
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

module.exports = router;