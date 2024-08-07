const axios = require("axios");

authresult = function (req, callback) {
    let cookie = "";
    try {
        cookie = req.cookies.Token
    } catch (e) {
        return res.send("<script>alert('로그인을 해주세요'); location.href = \"/user/login\";</script>");
    }
    axios({
        method: "get",
        url: api_url + "/api/Auth/check",
        headers: {"authorization": cookie},
    }).then((data) => {
        if (data.data.status.message == 'Success') {
            var result = true;
        } else {
            var result = false;
        }
        callback(result);
    });
}

admauthresult = function (req, callback) {
    let cookie = "";
    try {
        cookie = req.cookies.Token
    } catch (e) {
        return res.send("<script>alert('로그인을 해주세요'); location.href = \"/user/login\";</script>");
    }
    axios({
        method: "get",
        url: api_url + "/api/Auth/admcheck",
        headers: {"authorization": cookie},

    }).then((data) => {
        if (data.data.status.message == 'Success') {
            var result = true;
        } else {
            var result = false;
        }
        callback(result);
    });
}

module.exports = {
    authresult,
    admauthresult
}

