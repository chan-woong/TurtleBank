const express = require("express");
const router = express.Router();
const path = require("path");


const healthRouter = require("../lib/api/Health");
const transactionsRouter = require("../lib/api/Transactions");
const balanceRouter = require("../lib/api/Balance");
const beneficiaryRouter = require("../lib/api/Beneficiary");
const userRouter = require("../lib/api/User");
const checkRouter = require("../lib/api/Auth");
const noticeRouter = require("../lib/api/notice");
const qnaRouter = require("../lib/api/qna");
const accountRouter = require("../lib/api/Account");
const mydataRouter = require("../lib/api/Mydata");
const loanRouter = require("../lib/api/Loan");

router.use("/balance", balanceRouter);
router.use("/transactions", transactionsRouter);
router.use("/health", healthRouter);
router.use("/beneficiary", beneficiaryRouter);
router.use("/user", userRouter);
router.use("/auth", checkRouter);
router.use("/notice", noticeRouter);
router.use("/qna", qnaRouter);
router.use("/account", accountRouter);
router.use("/mydata", mydataRouter);
router.use("/loan", loanRouter);

router.get("/download/:filename", (req, res) => {
    const filename = req.params.filename;
    const filePath = path.join(__dirname, "../file", filename);
  
    res.setHeader('Content-Disposition', `attachment; filename=${filename}`);
    res.sendFile(filePath);
});


module.exports = router;