var express = require('express');
var router = express.Router();
const ModelBoard = require("../../../models_board/index");
var Response = require('../../Response');
var statusCodes = require('../../statusCodes');
var { encryptResponse, decryptRequest } = require("../../../middlewares/crypt");
const fs = require("fs");

router.post("/", decryptRequest, (req, res) => {
  var r = new Response();
  let filename = req.body.filename;
  let download_file_Path = file_path + filename;
  fs.stat(download_file_Path, (err, stats) =>{
    if (err != null) {
        r.status = 500;
        r.data = {
          data : "filename : " + filename,
          message: "해당하는 파일이 없습니다.",
        };
        return res.json(r);
    }
    res.download(download_file_Path, filename);
  });
});

module.exports = router;
