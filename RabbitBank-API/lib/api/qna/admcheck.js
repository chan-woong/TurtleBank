const express = require("express");
const router = express.Router();
const Model = require("../../../models/index");
const Response = require("../../Response");
const statusCodes = require("../../statusCodes");
const { encryptResponse, decryptRequest } = require("../../../middlewares/crypt");

router.post("/", decryptRequest, (req, res) => {
  var r = new Response();

  let username = req.body.username;

  // 'users' 테이블에서 is_admin 값을 조회하는 쿼리
  Model.users.findOne({
    attributes: ["is_admin"],
    where: { username: username }
  })
  .then((userData) => {
    if (!userData) {
      r.status = statusCodes.NOT_AUTHORIZED;
      r.data = {
        message: "User not authorized"
      };
      return res.json(encryptResponse(r));
    }

    // is_admin 값을 응답에 포함하여 보내줍니다.
    r.status = statusCodes.SUCCESS;
    r.data = {
      is_admin: userData.is_admin
    };
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