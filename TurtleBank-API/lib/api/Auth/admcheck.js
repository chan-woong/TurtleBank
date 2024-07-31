var express = require('express');
var router = express.Router();
var Response = require('../../Response');
const {admCheck} = require("../../../middlewares/validateToken")
const statusCodes = require("../../statusCodes");

router.get("/",[admCheck], (req, res) => {         
    var r = new Response();
    let {is_admin} = req          // 현재 사용자가 admin인지 확인

    r.status = statusCodes.SUCCESS;
    r.data = {
        "message": is_admin
    }

    return res.json(r);
})

module.exports = router;