const axios = require("axios");

authresult = function (req, callback) {          // authresult함수 정의
    let cookie = "";
    try {          // 쿠키 확인
        cookie = req.cookies.Token
    } catch (e) {          // 쿠키가 없으면 alert
        return res.send("<script>alert('로그인을 해주세요'); location.href = \"/user/login\";</script>");
    }

    axios({          // /api/Auth/check로 req
        method: "get",
        url: api_url + "/api/Auth/check",
        headers: {"authorization": cookie},

    }).then((data) => {
        if (data.data.status.message == 'Success') {          // message가 Success면,
            var result = true;
        } else {          // message가 Success가 아니면,
            var result = false;
        }
        callback(result);
    });
}

admauthresult = function (req, callback) {          // admauthresult함수 정의
    let cookie = "";
    try {          // 쿠키 확인
        cookie = req.cookies.Token
    } catch (e) {          // 쿠키가 없으면 alert
        return res.send("<script>alert('로그인을 해주세요'); location.href = \"/user/login\";</script>");
    }
    axios({          // /api/Authh/admcheck로 req
        method: "get",
        url: api_url + "/api/Auth/admcheck",
        headers: {"authorization": cookie},

    }).then((data) => {
        if (data.data.status.message == 'Success') {          // message가 Success면,
            var result = true;
        } else {          // message가 Success가 아니면,
            var result = false;
        }
        callback(result);
    });
}

module.exports = {
    authresult,
    admauthresult
}
