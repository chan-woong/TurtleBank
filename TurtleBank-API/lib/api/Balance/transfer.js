var express = require('express');
var router = express.Router();
var Model = require('../../../models/index');
const Sequelize = require("sequelize");
var Response = require('../../Response');
var statusCodes = require('../../statusCodes');
var { validateUserToken } = require("../../../middlewares/validateToken");
var { encryptResponse, decryptRequest } = require("../../../middlewares/crypt");

const fee = 1000;
const admin_account = 999999;

/**
 * Balance transfer route
 * @path                 - /api/balance/transfer
 * @middleware
 * @param to_account     - Amount to be transferred to this account
 * @param amount         - Amount to be transferred
 * @return               - Status
 */
router.post('/', [validateUserToken, decryptRequest], async (req, res) => {
    var r = new Response();
    const { from_account, to_account, amount, sendtime, accountPW } = req.body;
    const username = req.username;

    if (amount <= 0) {
        r.status = statusCodes.BAD_INPUT;
        r.data = { "message": "입력 값을 0원 이상 입력해주세요." };
        return res.json(encryptResponse(r));
    }

    try {
        const fromAccountData = await Model.account.findOne({ where: { account_number: from_account }, attributes: ["username"] });
        if (!fromAccountData || username !== fromAccountData.username) {
            r.status = statusCodes.BAD_INPUT;
            r.data = { "message": "출금계좌가 고객님의 계좌가 아닙니다." };
            return res.json(encryptResponse(r));
        }

        const userData = await Model.users.findOne({ where: { username }, attributes: ["membership", "accountPW"] });
        if (accountPW !== userData.accountPW) {
            r.status = statusCodes.BAD_INPUT;
            r.data = { "message": "계좌 비밀번호가 다릅니다." };
            return res.json(encryptResponse(r));
        }

        let maxAmount;
        switch (userData.membership) {
            case "SILVER":
                maxAmount = 1000000;
                break;
            case "GOLD":
                maxAmount = 10000000;
                break;
            case "PLATINUM":
                maxAmount = 100000000;
                break;
            default:
                maxAmount = Infinity;
        }

        if (amount > maxAmount) {
            r.status = statusCodes.BAD_INPUT;
            r.data = { "message": "송금 한도 초과입니다." };
            return res.json(encryptResponse(r));
        }

        const toAccountData = await Model.account.findOne({ where: { account_number: to_account } });
        if (!toAccountData) {
            r.status = statusCodes.BAD_INPUT;
            r.data = { "message": "입금계좌가 존재하지 않습니다." };
            return res.json(encryptResponse(r));
        }

        const fromAccountData2 = await Model.account.findOne({ where: { account_number: from_account }, attributes: ["balance", "bank_code"] });
        if (fromAccountData2.balance < amount + fee) {
            r.status = statusCodes.BAD_INPUT;
            r.data = { "message": "계좌의 잔액이 송금액보다 적습니다." };
            return res.json(encryptResponse(r));
        }

        await Model.transactions.create({
            from_bankcode: fromAccountData2.bank_code,
            from_account,
            to_bankcode: toAccountData.bank_code,
            to_account,
            amount,
            fee,
            sendtime
        });

        await Model.account.update({ balance: Sequelize.literal(`balance - ${amount + fee}`) }, { where: { account_number: from_account } });
        await Model.account.update({ balance: Sequelize.literal(`balance + ${amount}`) }, { where: { account_number: to_account } });
        await Model.account.update({ balance: Sequelize.literal(`balance + ${fee}`) }, { where: { account_number: admin_account } });

        r.status = statusCodes.SUCCESS;
        r.data = { "message": "송금에 성공했습니다." };
        return res.json(encryptResponse(r));

    } catch (err) {
        r.status = statusCodes.SERVER_ERROR;
        r.data = { "message": "송금에 실패했습니다." };
        return res.json(encryptResponse(r));
    }
});

router.post('/check_account', [validateUserToken, decryptRequest], async (req, res) => {
    var r = new Response();
    const { to_account } = req.body;

    try {
        const accountData = await Model.account.findOne({ where: { account_number: to_account }, attributes: ["username"] });
        if (accountData) {
            r.status = statusCodes.SUCCESS;
            r.data = { "username": accountData.username };
        } else {
            r.status = statusCodes.BAD_INPUT;
            r.data = { "message": "해당 계좌가 존재하지 않습니다." };
        }
        return res.json(encryptResponse(r));

    } catch (err) {
        r.status = statusCodes.SERVER_ERROR;
        r.data = { "message": "알수 없는 문제가 발생했습니다." };
        return res.json(encryptResponse(r));
    }
});

module.exports = router;
