var express = require('express');
var router = express.Router();
var Model = require('../../../models/index');
var Response = require('../../Response');
var statusCodes = require('../../statusCodes');
var { validateUserToken } = require("../../../middlewares/validateToken");
var { encryptResponse, decryptRequest } = require("../../../middlewares/crypt");

/**
 * User profile route
 * This endpoint allows the user to see profile
 * @path                             - /api/user/profile
 * @middleware
 * @return                           - Status: Balance, Account number, username, is_admin
 */
router.post('/', validateUserToken, (req, res) => {
    var r = new Response();
    Model.users.findOne({          // select balance, account_number, username, is_admin, membership, is_mydata, is_loan from users where account_number = req.account_number;
        where: {
            account_number: req.account_number
        },
        attributes: ["balance", "account_number", "username", "is_admin", "membership", "is_mydata", "is_loan"]
    }).then((user) => {
        Model.account.findAll({
            where: {
                username: req.username
            },
            attributes: [
                [Model.sequelize.fn('sum', Model.sequelize.col('balance')), 'total_balance'],
            ],
            raw: true
        }).then((total) => {
            total[0].total_balance = Number(total[0].total_balance);
            total[0].total_balance = total[0].total_balance.toLocaleString();
            user.dataValues = Object.assign({}, user.dataValues, total[0]);          //pending에 total_balance도 추가해주는 것.
            //console.log(user);
            if (user) {          // user가 존재하는 경우
                r.status = statusCodes.SUCCESS;
                r.data = user;
                return res.json(encryptResponse(r));          // SUCCESS를 return
            } else {          // user가 존재하는 경우
                r.status = statusCodes.NOT_AUTHORIZED;
                r.data = {
                    "message": "Not authorized"
                }
                return res.json(encryptResponse(r));          // Not Authorized를 return
            }

        })

    }).catch((err) => {
        r.status = statusCodes.SERVER_ERROR;
        r.data = {
            "message": err.toString()
        };
        res.json(encryptResponse(r));
    });
});

router.post('/S', validateUserToken,(req, res)=> {
    const ID=req.body.id;
    var r = new Response();
    Model.users.findOne({          // select balance, account_number, username, is_admin, membership, is_mydata, is_loan from users where account_number = req.account_number;
        where: {
            id: ID
        },
        attributes: ["id", "membership"]
    }).then(() => {
        Model.users.update({
            membership:"SILVER"
        },{where:{
                id:ID
            },
        }).then(()=>{
            r.status = statusCodes.SUCCESS;
            r.data={
                "message":"successfullly"
            }
            return res.send()
        });
    })
});

router.post('/G', validateUserToken,(req, res)=> {
    const ID=req.body.id;
    var r = new Response();
    Model.users.findOne({          // select balance, account_number, username, is_admin, membership, is_mydata, is_loan from users where account_number = req.account_number;
        where: {
            id: ID
        },
        attributes: ["id", "membership"]
    }).then(() => {
        Model.users.update({
            membership:"GOLD"
        },{where:{
                id:ID
            },
        }).then(()=>{
            r.status = statusCodes.SUCCESS;
            r.data={
                "message":"successfullly"
            }
            return res.send()
        });
        })
    });

router.post('/P', validateUserToken,(req, res)=> {
    const ID=req.body.id;
    var r = new Response();
    Model.users.findOne({          // select balance, account_number, username, is_admin, membership, is_mydata, is_loan from users where account_number = req.account_number;
        where: {
            id: ID
        },
        attributes: ["id", "membership"]
    }).then(() => {
        Model.users.update({
            membership:"PLATINUM"
        },{where:{
                id:ID
            },
        }).then(()=>{
            r.status = statusCodes.SUCCESS;
            r.data={
                "message":"successfullly"
            }
            return res.send()
        });
        })
    });

router.get('/main',decryptRequest ,(req, res)=> {
    var r = new Response();
    Model.notices.findAll({
        order: [['id', 'DESC']],
    }).then((data) => {
        r.status = statusCodes.SUCCESS;
        r.data= data;
        return res.json(encryptResponse(r));
    })
    .catch((err) => {
        r.status = statusCodes.SERVER_ERROR;
        r.data = {
            message: err.toString(),
        };
        return res.json(encryptResponse(r));
    });
});

module.exports = router;