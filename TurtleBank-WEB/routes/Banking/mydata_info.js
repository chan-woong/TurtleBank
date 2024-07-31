var express = require('express');
var router = express.Router();
const axios = require("axios");
const profile = require('../../middlewares/profile');
const { decryptRequest } = require("../../middlewares/crypt")
const checkCookie = require("../../middlewares/checkCookie");
const IpCheck = require('../../middlewares/IpCheck');

MYDATA_PNG = `<div style="text-align: center;">
<img src="../img/mydata.png" style="width:45%;">
</div> `

router.get('/', checkCookie, function (req, res, next) {
	    const cookie = req.cookies.Token;
	    profile(cookie).then(profileData => {
		            axios({
				                method: "post",
				                url: api_url + "/api/account/view",
				                headers: { "authorization": "1 " + cookie }
				            }).then((data) => {
						                let html_data=""
						                html_data +=   "<h2 align='center'>마이데이터란 ?</h2>\n" + MYDATA_PNG 
						                html_data += `
								                <div class="text-center">
										                    <h5>마이데이터는 고객의 데이터를 수집하고 관리하여 맞춤형 서비스를 제공하는 기능입니다. </h5>
												                    </div>

														                    <div class="text-center">
																                        <h5>마이데이터 기능을 사용하고 싶으시다면, 모바일 앱을 다운로드해주세요!</h5>
																			                </div>
																					            `;
						                return res.render("Banking/mydata_info", { html: html_data, pending: profileData, select: "mydata" });
						            }).catch(function (error) {

								                var html_data = [
										                { username: error, balance: error, account_number: error, bank_code: error }
								                ];
								                return res.render("Banking/mydata_info", { html_data: html_data, pending: profileData, select: "mydata" });
								            });
		        });
});

module.exports = router;

