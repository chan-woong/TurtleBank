var express = require("express");
var router = express.Router();
var Model = require("../../../models/index");
var Response = require("../../Response");
var statusCodes = require("../../statusCodes");
var { validateUserToken } = require("../../../middlewares/validateToken");
const { Op } = require("sequelize");
var { encryptResponse,decryptRequest } = require("../../../middlewares/crypt");
/**
 * Transactions viewing route
 * This endpoint allows to view all transactions of authorized user
 * @path                             - /api/transactions/view
 * @middleware
 * @return                           - Status
 */
router.post("/", validateUserToken, (req, res) => {
    var r = new Response();
    let { account_number } = req;

    Model.transactions.findAll({          // select from_account, to_account, amount, sendtime from transactions where from_account = account_number and to_account = account_number;
            where: {
                [Op.or]: [
                    { from_account: account_number },
                    { to_account: account_number },
                ],
            },
            attributes: ["from_account", "to_account", "amount", "sendtime"],
        }).then((transactions) => {
            r.status = statusCodes.SUCCESS;
            r.data = transactions;
            return res.json(encryptResponse(r));          // 데이터가 있으면 SUCCESS return
        }).catch((err) => {
            r.status = statusCodes.SERVER_ERROR;
            r.data = {
                message: err.toString(),
            };
            return res.json(encryptResponse(r));
        });
});

router.post("/search", [validateUserToken,decryptRequest], async (req, res) => {
    var r = new Response();
    let username = req.username;
    const startDate = req.body.tripstart;
    const endDate = req.body.tripend + " 23:59:59";
    try{
    const results = await Model.sequelize.query(     //username 받아서 해당 username으로 account 테이블에서 List 뽑아서, 해당 transactions에서 뽑아오는걸로.
         `
        SELECT DISTINCT t.* FROM transactions t JOIN account a ON t.from_account = a.account_number OR t.to_account = a.account_number WHERE a.username = '${username}' AND sendtime >= '${startDate}' AND sendtime <= '${endDate}';
         `

    );
    const [returndata] = results;
    r.status = statusCodes.SUCCESS;
    r.data = { result: returndata };
}
    catch(error){
    r.status = statusCodes.ERROR;
    r.message = error.message;
    }
    return res.json(encryptResponse(r));
});

module.exports = router;
