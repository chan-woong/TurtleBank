'use strict';

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up (queryInterface, Sequelize) {
    await queryInterface.createTable('account', {
      account_number: {
        type: Sequelize.INTEGER,
        primaryKey: true,
      },
      bank_code: {
        type: Sequelize.INTEGER,
        allowNull: false
      },
      username: {
        type: Sequelize.STRING,
        allowNull: false
      },
      balance: {
        type: Sequelize.INTEGER,
        allowNull: false,
        defaultValue: 10000
      }
    });
  },

  async down (queryInterface, Sequelize) {
    await queryInterface.dropTable('account');
  }
};

//migrate 하는 법 : npx sequelize-cli db:migrate --name 20240219050557-add_account_table (sequelize-cli다운로드 필요)