const express = require("express");
const router = express.Router();
const Model = require("../../../models/index");
const ModelBoard = require("../../../models_board/index");
const Response = require("../../Response");
const statusCodes = require("../../statusCodes");
const { encryptResponse, decryptRequest } = require("../../../middlewares/crypt");

router.post("/", decryptRequest, async (req, res) => {
  var r = new Response();

  let username = req.body.username;
  let id = req.body.id;

  try {
    // 1. username으로 Model.users 테이블 조회하여 is_admin 값을 가져옵니다.
    const userData = await Model.users.findOne({
      attributes: ["is_admin"],
      where: { username: username }
    });

    if (!userData) {
      r.status = statusCodes.NOT_AUTHORIZED;
      r.data = { message: "User not authorized" };
      return res.json(encryptResponse(r));
    }

    // 2. id와 username으로 ModelBoard.qnas를 조회하여 id, userId와 비교하여 결과를 확인합니다.
    const qnaData = await ModelBoard.qnas.findOne({
      where: { id, userId: username }
    });

    // check_user 변수를 true 또는 false로 설정합니다.
    const check_user = Boolean(qnaData);

    // 응답에 is_admin과 check_user를 포함하여 보내줍니다.
    r.status = statusCodes.SUCCESS;
    r.data = {
      is_admin: userData.is_admin,
      check_user: check_user
    };
    return res.json(encryptResponse(r));
  } catch (err) {
    r.status = statusCodes.SERVER_ERROR;
    r.data = { message: err.toString() };
    return res.json(encryptResponse(r));
  }
});

module.exports = router;