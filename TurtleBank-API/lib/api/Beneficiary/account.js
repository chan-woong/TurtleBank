var express = require('express');
var router = express.Router();
var Model = require('../../../models/index');
var Response = require('../../Response');
var statusCodes = require('../../statusCodes');
var { validateUserToken } = require("../../../middlewares/validateToken");
var { encryptResponse,decryptRequest } = require("../../../middlewares/crypt");

/**
 * Beneficiary approve route
 * This endpoint allows to approve beneficiary requests of any user from this endpoint
 * @path                             - /api/beneficiary/approve
 * @middleware                       - Checks admin authorization
 * @param id                         - ID to be approved
 * @return                           - Status
 */
router.post('/', [validateUserToken,decryptRequest], (req, res) => {          // from /trade_send.js
    var r = new Response();
    let username=req.body.username;
    
    Model.account.findAll({          // select account_number from account where username = username;
        where: {
            username:username,
        },
        attributes: ["account_number"]
    }).then((data) => {

        let arr = data.map((elem) => parseInt(elem.account_number));

                        r.status = statusCodes.SUCCESS;
                        r.data = {
                            "message": "Success",
                            "accountdata": arr
                        }
                        return res.json(encryptResponse(r));          // 데이터 존재하면 SUCCESS return
                    }).catch((err) => {
        r.status = statusCodes.SERVER_ERROR;
        r.data = {
            "message": err.toString()
        };
        res.json(encryptResponse(r));
    });
});

module.exports = router;
