const express = require("express");
const router = express.Router();
const ModelBoard = require("../../../models_board/index");
var Response = require('../../Response');
var statusCodes = require('../../statusCodes');
var { encryptResponse, decryptRequest } = require("../../../middlewares/crypt");
const fs = require("fs");

router.post("/", decryptRequest, (req, res) => {
  var r = new Response();
  let filename = req.body.filename;

  ModelBoard.notices.findOne({
      attributes: ['filepath'], // filepath 컬럼만 선택
      where: {filepath: filename}
  }).then((data) => {
    if (data != null) {
      let download_file_Path = file_path + data.dataValues.filepath;
      fs.stat(download_file_Path, (err, stats) =>{
        if (err != null) {
            r.status = 500;
            r.data = {
              data : "filename : " + filename,
              message: "해당하는 파일이 없습니다.",
            };
            return res.json(encryptResponse(r));
        }
        res.download(download_file_Path, filename);
      });
    } else {
      r.status = statusCodes.NOT_FOUND;
      r.data = {
        message: "해당하는 데이터가 없습니다.",
      };
      return res.json(encryptResponse(r));
    }
    
  })
  .catch((err) => {
    r.status = statusCodes.SERVER_ERROR;
    r.data = {
      data : "err.toString() : " + filename,
      message: err.toString(),
    };
    return res.json(encryptResponse(r));
  });
});


router.get("/", (req, res) => {
  const filename = Buffer.from(req.query.filename, 'ascii').toString('utf8' );
  console.log(filename);
  const filePath = "../file/" + filename;
});

module.exports = router;