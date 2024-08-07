const express = require('express');
const router = express.Router();
const multer = require('multer');
const { runRsync } = require("../../../middlewares/rsync");

const upload = multer({
  storage: multer.diskStorage({
      destination: function (req, file, cb) {
          cb(null, "../file");
      },
      filename: function (req, file, cb) {
          cb(null, Buffer.from(file.originalname, 'ascii').toString('utf8' ));
      },
  }),
});

router.get('/', (req, res) => {
  res.send('<html><body><h1>Welcome to the file upload page</h1></body></html>');
});

router.post('/', upload.single('file'), (req, res) => {
  console.log(req.body.file);
  console.log(req.file);
  // runRsync;
});

module.exports = router;

