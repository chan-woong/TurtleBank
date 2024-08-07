var express = require('express');
var router = express.Router();

const axios = require("axios");
var {decryptRequest, decryptEnc, encryptResponse} = require("../../middlewares/crypt");
var {runRsync} = require("../../middlewares/rsync");

router.get("/", (req, res) => {
    const filename =  req.query.url;
    const baseData = `{"filename" : "${filename}"}`;
    axios({
        method: 'post',
        url: api_url + "/api/notice/download",
        responseType: 'stream',
        data: encryptResponse(baseData)
    }).then((data) => {
        console.log("data");
        res.setHeader('Content-Type', data.headers['content-type']);
        res.setHeader('Content-Disposition', data.headers['content-disposition']==undefined ? 'attachment' : data.headers['content-disposition']);
        data.data.pipe(res)
    })
})

module.exports = router;