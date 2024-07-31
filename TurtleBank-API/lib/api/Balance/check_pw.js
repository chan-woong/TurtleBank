var express = require('express');
var router = express.Router();
var Model = require('../../../models/index');
var Response = require('../../Response');
var statusCodes = require('../../statusCodes');
var { validateUserToken } = require("../../../middlewares/validateToken");
var { encryptResponse, decryptRequest } = require("../../../middlewares/crypt");

router.post('/', [validateUserToken,decryptRequest], (req, res) => {
    var r = new Response();
    var accountPW = req.body.accountPW;
    var username = req.body.username;
    var membership = req.body.membership;
    var is_admin = req.body.is_admin;

    Model.users
    .findOne({
        where: {
            username: username,
            membership: membership,
            is_admin: is_admin,
        },
    }).then((result) => {
        if (result !== null && result.accountPW == accountPW) {
            r.status = statusCodes.SUCCESS;
            r.data = {
            data : result,
            message: "송금이 정상적으로 처리되었습니다.",
            };
        } else {
            r.status = statusCodes.NOT_FOUND;
            r.data = {
            message: "이체 PW가 일치하지 않습니다.",
            };
        }
        return res.json(encryptResponse(r));
    })
    .catch((err) => {
        r.status = statusCodes.SERVER_ERROR;
        r.data = {
            data : req.body,
            message: err.toString(),
        };
        return res.json(encryptResponse(r));
    });
});    


module.exports = router;