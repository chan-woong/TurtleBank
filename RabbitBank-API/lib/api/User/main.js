var express = require('express');
var router = express.Router();
const ModelBoard = require("../../../models_board/index");
var Response = require('../../Response');
var statusCodes = require('../../statusCodes');
var { encryptResponse, decryptRequest } = require("../../../middlewares/crypt");


router.post('/' ,(req, res)=> {
    var r = new Response();
    ModelBoard.notices.findAll({
        attributes: ["id", "userId", "title", "updatedAt"],
    })
    .then((data) => {
        r.status = statusCodes.SUCCESS;
        r.data = data;
        return res.json(encryptResponse(r));
    })
    .catch((err) => {
        r.status = statusCodes.SERVER_ERROR;
        r.data = {
            message: err.toString(),
        };
        return res.json(encryptResponse(r));
    });
});

module.exports = router;
