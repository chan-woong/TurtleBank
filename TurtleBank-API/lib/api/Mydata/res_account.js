var express = require('express');
var router = express.Router();
var Model = require('../../../models/index');
var Response = require('../../Response');

router.post('/', function(req, res) {
    var r = new Response();
    var req_phone = req.body.phone;

    Model.users.findOne({          // select username from users where phone = req_phone;
        where: {
            phone: req_phone
        },
        attribute: ["username"]
    }).then((data) => {
        Model.account.findAll({          // select account_number, balance, bank_code from account where username = data.username;
            where: {
                username : data.username
            },
            attributes: ["account_number", "balance", "bank_code"]
        }).then((data2) => {
            if(data2.length > 0) {          // account 테이블에 해당 user에 대한 정보가 있는 경우
                
                r.data2 = data2;
                return res.json((r));          
            } else {          // account 테이블에 해당 user에 대한 정보가 없는 경우
                
                r.data2 = {
                    "message": "계좌가 존재하지 않습니다."
                }
                return res.json((r));
            }
        }).catch((err2) => {
            
            r.data2 = {
                "message": err2.toString()
            };
            return res.json((r));
        });
    }).catch((err) => {
        r.data={
            "message":err.toString()
        };
        return res.json((r));
    });
});

module.exports = router;
