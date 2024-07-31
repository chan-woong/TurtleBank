const express = require("express");
const router = express.Router();
const ModelBoard = require("../../../models_board/index");
const Response = require("../../Response");
const statusCodes = require("../../statusCodes");
const { encryptResponse, decryptRequest } = require("../../../middlewares/crypt");
const multer = require("multer");

router.post("/", decryptRequest, (req, res) => {
  var r = new Response();

  let userId = "관리자";
  let title = req.body.title;
  let content = req.body.content;
  let filepath =  Buffer.from(req.body.filepath, 'ascii').toString('utf8' );
  let createdAt = req.body.createdAt;
  let updatedAt = req.body.updatedAt;

  ModelBoard.notices.create({
    userId: userId,
    title: title,
    content: content,
    filepath: Buffer.from(filepath, 'ascii').toString('utf8' ),
    createdAt: createdAt,
    updatedAt: updatedAt,
  })
  .then(() => {
    r.status = statusCodes.SUCCESS;
    r.data = {
      message: "데이터가 성공적으로 추가되었습니다.",
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

router.post("/write", decryptRequest, (req, res) => {
  var r = new Response();
  let {title,contents,userId, file_name,seoultime} = req.body;
  ModelBoard.notices.create({
      userId: userId,
      title: title,
      content: contents,
      filepath: Buffer.from(file_name, 'ascii').toString('utf8' ),
      createdAt: seoultime,
      updatedAt: seoultime,
  }).then((data) => {
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