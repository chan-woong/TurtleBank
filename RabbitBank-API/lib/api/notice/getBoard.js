const express = require("express");
const router = express.Router();
const ModelBoard = require("../../../models_board/index");
const Response = require("../../Response");
const statusCodes = require("../../statusCodes");
const { encryptResponse, decryptRequest } = require("../../../middlewares/crypt");

router.post("/", decryptRequest, (req, res) => {
  var r = new Response();

  let id = req.body.id; // 안드로이드에서 전송된 id 값을 가져옵니다.

  // id를 정수형으로 변환
  const idAsInteger = parseInt(id);

  ModelBoard.notices.findByPk(idAsInteger, {
    attributes: ["id", "userId", "title", "content", "filepath", "createdAt", "updatedAt"]
  })
  .then((data) => {
    if (data) {
      const sanitizedData = {
        ...data.toJSON(),
        userId: data.userId,
        title: data.title,
        createdAt: data.createdAt,
        updatedAt: data.updatedAt,
        filepath: data.filepath,
        content: data.content,
      };

      r.status = statusCodes.SUCCESS;
      r.data = sanitizedData;
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
});

module.exports = router;