var express = require('express');
var router = express.Router();

router.get("/",(req, res)=>{
    //화이트 리스트 적용 해야함
    const url = req.query.url
    return res.send("<script>document.location=\""+url+"\"</script>");
})

module.exports = router;