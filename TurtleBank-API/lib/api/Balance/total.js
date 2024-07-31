var express = require('express');
var router = express.Router();
var Model = require('../../../models/index');
var Response = require('../../Response');
var statusCodes = require('../../statusCodes');
var { validateUserToken, tokenCheck } = require("../../../middlewares/validateToken");
var { encryptResponse, decryptRequest } = require("../../../middlewares/crypt");

/**
 * Account view route
 * @path                 - /api/balance/total
 * @middleware
 * @return               
 */
router.post('/', validateUserToken, (req, res) => {          
    var r = new Response();
    Model.account.findAll({
        where: {
            username: req.username
        },
        attributes: [
            [Model.sequelize.fn('sum', Model.sequelize.col('balance')), 'total_balance'],
        ],
        raw: true
    }).then((data) => {
        if(data.length > 0) {          // account테이블에 username이 존재한다면
            r.status = statusCodes.SUCCESS;
            r.data = data;
            return res.json(encryptResponse(r));
        } else {          // account테이블에 username이 존재하지 않는다면
            r.status = statusCodes.NOT_AUTHORIZED;
            r.data = {
                "message": "Not authorized"
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