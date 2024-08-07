const express = require("express");
const router = express.Router();
const ModelBoard = require("../../../models_board/index");
const Response = require("../../Response");
const statusCodes = require("../../statusCodes");
var { encryptResponse, decryptRequest } = require("../../../middlewares/crypt");

router.post('/', decryptRequest, (req, res) => {
    var r = new Response();
    let id = req.body.id;

    ModelBoard.qnas.findOne({
        where: { id: id },
    }).then((data) => {
        console.log("data:"+data);
        if (data) {
            r.status = statusCodes.SUCCESS;
            r.data = data;
            return res.json(encryptResponse(r));
        } else {
            r.status = statusCodes.NOT_FOUND;
            r.data = {
            message: "해당하는 데이터가 없습니다.",
            };
            return res.json(encryptResponse(r));
        }
    })
    .catch((err) => {
        r.status = statusCodes.SERVER_ERROR;
        r.data = {
            message: err.toString(),
        };
        return res.json(encryptResponse(r));
    });
});

router.post('/edit', decryptRequest, (req, res) => {
    var r = new Response();
    let {comment, id, updatedAt} = req.body;

    ModelBoard.qnas.update({
        comment: comment,
        updatedAt: updatedAt,
    },{
        where: { id: id},
    }).then((data) => {
        console.log("data:"+data);
        if (data) {
            r.status = statusCodes.SUCCESS;
            r.data = data;
            return res.json(encryptResponse(r));
        } else {
            r.status = statusCodes.NOT_FOUND;
            r.data = {
            message: "해당하는 데이터가 없습니다.",
            };
            return res.json(encryptResponse(r));
        }
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