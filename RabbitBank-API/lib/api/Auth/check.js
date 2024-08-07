var express = require('express');
var router = express.Router();
var Response = require('../../Response');
const {tokenCheck} = require("../../../middlewares/validateToken")
const statusCodes = require("../../statusCodes");

router.get("/",[tokenCheck], (req, res) => {          // 사용자가 로그인한 user인지 확인
    var r = new Response();
    let {is_user} = req

    r.status = statusCodes.SUCCESS;
    r.data = {
        "message": is_user
    }

    return res.json(r);
})

module.exports = router;