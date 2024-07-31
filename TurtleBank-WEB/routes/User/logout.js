var express = require('express');
var router = express.Router();


router.get('/', function (req, res) {
    res.clearCookie("Token");         // 쿠키값에서 토큰 삭제
    res.send("<script>alert('로그아웃 되었습니다.');location.href='/';</script>");       
});


module.exports = router;