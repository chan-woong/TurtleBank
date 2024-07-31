const express = require("express");
const router = express.Router();
const ModelBoard = require("../../../models_board/index");
const Response = require("../../Response");
const statusCodes = require("../../statusCodes");
const { encryptResponse, decryptRequest } = require("../../../middlewares/crypt");

router.post("/", decryptRequest, (req, res) => {
  var r = new Response();

  let id = req.body.id;

  ModelBoard.qnas.destroy({
      where: {
        id: id,
      },
    }).then((result) => {
      if (result !== 0) {
        r.status = statusCodes.SUCCESS;
        r.data = {
          message: "데이터가 성공적으로 삭제되었습니다.",
        };
      } else {
        r.status = statusCodes.NOT_FOUND;
        r.data = {
          message: "해당 id를 가진 데이터를 찾을 수 없습니다.",
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