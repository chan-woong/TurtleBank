const express = require("express");
const router = express.Router();
const Model = require("../../../models/index");
const ModelBoard = require("../../../models_board/index");
const Response = require("../../Response");
const statusCodes = require("../../statusCodes");
const { encryptResponse, decryptRequest } = require("../../../middlewares/crypt");

router.post("/", decryptRequest, (req, res) => {
  var r = new Response();
  let searchtext = req.body.searchtext;
  
  ModelBoard.qnas.findAll({
      attributes: ["id", "userId", "title", "updatedAt"],
      where: {
          [ModelBoard.Sequelize.Op.or]: [
              {
                  title: {
                      [ModelBoard.Sequelize.Op.like]: `%${searchtext}%`
                  }
              },
              {
                  content: {
                      [ModelBoard.Sequelize.Op.like]: `%${searchtext}%`
                  }
              },
              {
                  userId: {
                      [ModelBoard.Sequelize.Op.like]: `%${searchtext}%`
                  }
              }
          ]
      }
  })
  .then((data) => {
      r.status = statusCodes.SUCCESS;
      r.data = data;
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