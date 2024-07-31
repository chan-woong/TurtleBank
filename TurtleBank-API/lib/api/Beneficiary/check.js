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
router.post('/', [validateUserToken,decryptRequest], (req, res) => {
    var r = new Response();
    let { account_number } = req;
    Model.beneficiaries.findAll({
        where: {
            account_number: account_number,
            approved: true
        },
        attributes: ["beneficiary_account_number",]
    }).then((data) => {
        let arr = data.map((elem) => parseInt(elem.beneficiary_account_number));
                        r.status = statusCodes.SUCCESS;
                        r.data = {
                            "message": "Success",
                            "accountdata": arr
                        }
                        return res.json(encryptResponse(r));
                    }).catch((err) => {
        r.status = statusCodes.SERVER_ERROR;
        r.data = {
            "message": err.toString()
        };
        res.json(encryptResponse(r));
    });
});

module.exports = router;
