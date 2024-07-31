var express = require('express');
var router = express.Router();
var Model = require('../../../models/index');
var Response = require('../../Response');
var statusCodes = require('../../statusCodes');
var { validateUserToken } = require("../../../middlewares/validateToken");
var { encryptResponse,decryptRequest } = require("../../../middlewares/crypt");

/**
 * @path                             - /api/beneficiary/ceiling
 * @middleware                       - Checks admin authorization
 * @return                           - Status
 */

router.post('/', validateUserToken, (req, res) => {          // from /membership
    var r = new Response();
    Model.users.findOne({          // select is_admin from users where account_number = req.account_number;
        where: {
            account_number: req.account_number
        },
        attributes: ["is_admin"]
    }).then((user) => {
        if(user.is_admin) {          // is_admin이 참이면,
            Model.users.findAll({          // select username, membership, id from users;
                attributes: ["username", "membership", "id"]
            }).then((data) => {
                r.status = statusCodes.SUCCESS;
                r.data = data;
                return res.json(encryptResponse(r));          // 성공하면 SUCCESS, data return
            }).catch((err) => {
                r.status = statusCodes.SERVER_ERROR;
                r.data = {
                    "message": err.toString()
                };
                return res.json(encryptResponse(r));
            });
        } else {          // is_admin이 참이 아니면,
            Model.users.findOne({          // select membership from users where account_number = req.account_number;
                where: {
                    account_number: req.account_number
                },
                attributes: ["membership"]
            }).then((data) => {
                r.status = statusCodes.SUCCESS;
                r.data = data;
                return res.json(encryptResponse(r));          // 성공하면 SUCCESS, data return
            }).catch((err) => {
                r.status = statusCodes.SERVER_ERROR;
                r.data = {
                    "message": err.toString()
                };
                return res.json(encryptResponse(r));
            });
        }
    })
});

module.exports = router;