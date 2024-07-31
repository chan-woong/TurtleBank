var express = require('express');
var router = express.Router();

const axios = require("axios");
var {decryptRequest, decryptEnc, encryptResponse} = require("../../middlewares/crypt");

router.get("/", (req, res) => {
    const filename =  req.query.url;
    const baseData = `{"filename" : "${filename}"}`;
    
    console.error("aslkdaskldjhkasjdh")
    axios({
        method: 'post',
        url: api_url + "/api/notice/download",
        responseType: 'stream',
        data: encryptResponse(baseData)
    }).then((data) => {
        if(data !== null){
            res.setHeader('Content-Type', data.headers['content-type']);
            res.setHeader('Content-Disposition', data.headers['content-disposition']==undefined ? 'attachment' :  data.headers['content-disposition']);
            data.data.pipe(res)
        }
        else{
            res.end();
        }
    }).catch((err) => {
        res.send(`<script>alert('파일이 존제하지 않습니다.');window.history.back();</script>`);
        
    })

})

module.exports = router;
