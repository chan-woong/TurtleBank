var express = require('express');

var router = express.Router();
const mainRouter = require("../routes/Main")
const transactionsRouter = require("../routes/Transactions")
const balanceRouter = require("../routes/Balance");
const userRouter = require("../routes/User");
const bankJob = require("../routes/Banking")
const notice = require("../routes/notice");
const qna = require("../routes/qna");

router.use("/balance", balanceRouter);
router.use("/", mainRouter);
router.use("/bank", bankJob)
router.use("/transactions", transactionsRouter)
router.use("/user", userRouter);
router.use("/notice", notice);
router.use("/qna", qna);

module.exports = router;
