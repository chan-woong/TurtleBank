const express = require("express");
const router = express.Router();
const Model = require("../../../models/index");
const ModelBoard = require("../../../models_board/index");
const Response = require("../../Response");
const statusCodes = require("../../statusCodes");
const { encryptResponse } = require("../../../middlewares/crypt");

router.post("/", (req, res) => {
  var r = new Response();
  // 'notice' 테이블에서 데이터를 조회하는 쿼리
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