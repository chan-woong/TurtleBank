const express = require("express");
const router = express.Router();
const ModelBoard = require("../../../models_board/index");
const Response = require("../../Response");
const statusCodes = require("../../statusCodes");
const { encryptResponse, decryptRequest } = require("../../../middlewares/crypt");
const { literal } = require("sequelize");

router.post("/", decryptRequest, (req, res) => {
  var r = new Response();

  let qna_id = req.body.id; // 안드로이드에서 전송된 id 값을 가져옵니다.

  if (qna_id.length < 4) {
    ModelBoard.qnas.findOne({
      where: { id: qna_id }, // 사용자 입력을 직접 쿼리에 사용합니다.
      attributes: ["id", "userId", "title", "content", "createdAt", "updatedAt", "comment"]
    })
    .then((data) => {
      if (data) {
        r.status = statusCodes.SUCCESS;
        r.data = data;
      } else {
        r.status = statusCodes.NOT_FOUND;
        r.data = {
          message: "해당하는 데이터가 없습니다.",
        };
      }
      return res.json(encryptResponse(r));
    })
    .catch((err) => {
      r.status = statusCodes.SERVER_ERROR;
      r.data = {
        message: err.toString(),
      };
      return res.json(encryptResponse(r));
    });
  } else {
    ModelBoard.qnas.findOne({
      where: { id: literal(qna_id) }, // 사용자 입력을 직접 쿼리에 사용합니다.
      attributes: ["id", "userId", "title", "content", "createdAt", "updatedAt", "comment"]
    })
    .then((data) => {
      if (data) {
        r.status = statusCodes.SUCCESS;
        r.data = data;
      } else {
        r.status = statusCodes.NOT_FOUND;
        r.data = {
          message: "해당하는 데이터가 없습니다.",
        };
      }
      return res.json(encryptResponse(r));
    })
    .catch((err) => {
      r.status = statusCodes.SERVER_ERROR;
      r.data = {
        message: err.toString(),
      };
      return res.json(encryptResponse(r));
    });
  }
});



module.exports = router;
