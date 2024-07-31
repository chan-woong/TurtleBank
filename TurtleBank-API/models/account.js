module.exports = function(sequelize, DataTypes) {
	var Account = sequelize.define("account", {
		account_number: {
            type: DataTypes.INTEGER,
            primaryKey: true,
          },
          bank_code: {
            type: DataTypes.INTEGER,
            allowNull: false
          },
          username: {
            type: DataTypes.STRING,
            allowNull: false
          },
          balance: {
            type: DataTypes.INTEGER,
            allowNull: false,
            defaultValue: 1000000
          }
	}, {
        tableName: 'account',
		timestamps: false
	});
	return Account;
};

