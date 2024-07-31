const express = require("express");
const router = express.Router();
const ModelBoard = require("../../../models_board/index");
const Response = require("../../Response");
const statusCodes = require("../../statusCodes");
const { encryptResponse, decryptRequest } = require("../../../middlewares/crypt");

router.post("/", decryptRequest, (req, res) => {
  var r = new Response();

  let userId = req.body.userId;
  if(userId == "admin") {
    userId = "관리자";
  }
  let title = req.body.title;
  let content = req.body.content;
  let filepath = req.body.filepath;
  let createdAt = req.body.createdAt;
  let updatedAt = req.body.updatedAt;

  ModelBoard.qnas.create({
    userId: userId,
    title: title,
    content: content,
    filepath: filepath,
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
  let{userId,title,content,seoultime} = req.body;
  ModelBoard.qnas.create({
    userId: userId,
    title: title,
    content: content,
    createdAt: seoultime,
    updatedAt: seoultime,
  }).then(() =>{
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

module.exports = router;