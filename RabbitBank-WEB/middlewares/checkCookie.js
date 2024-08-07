const { decryptEnc } = require("./crypt");
const checkCookie = function (req, res, next) {
  try {
    req.cookies.Token = decryptEnc(req.cookies.Token);
    next();
  } catch (e) {
    return res.send(
      "<script>alert('로그인을 해주세요'); location.href = \"/user/login\";</script>"
    );
  }
};

module.exports = checkCookie;
