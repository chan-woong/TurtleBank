const requestIp = require("request-ip")
const IpCheck = function (req, res, next) {
    try{
        const clientIp = requestIp.getClientIp(req).split(":")
        if(clientIp[clientIp.length-1] === "127.0.0.1"){
            next()
        }else{
            // return res.send(
            //     "<script>alert('관리자가 아닙니다.'); location.href = \"/bank/list\";</script>")
            next()
        }
    }catch (e) {
        return res.send(
            "<script>alert('관리자가 아닙니다.'); location.href = \"/bank/list\";</script>")
    }
};

module.exports = IpCheck;
