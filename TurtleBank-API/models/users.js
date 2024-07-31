module.exports = function(sequelize, DataTypes) {
	var Users = sequelize.define("users", {
    id: {
      type: DataTypes.INTEGER,
      primaryKey: true,
      autoIncrement: true,
    },
    username: {
      type: DataTypes.STRING,
      allowNull: false,
      unique: true
    },
    password: {
      type: DataTypes.STRING,
      allowNull: false
    },
    phone: {
      type: DataTypes.STRING,
      allowNull: false,
      unique: true
    },
    account_number: {
      type: DataTypes.INTEGER,
      allowNull: false,
      unique: true
    },
    accountPW: {
      type: DataTypes.INTEGER,
      allowNull: false,
      unique: false
    },
    balance: {
      type: DataTypes.INTEGER,
      allowNull: false,
      defaultValue: 1000000
    },
    is_admin: {
      type: DataTypes.BOOLEAN,
      allowNull: false,
      defaultValue: false
    },
    is_loan: {
      type: DataTypes.BOOLEAN,
      allowNull: false,
      defaultValue: false
    },
    is_mydata: {
      type: DataTypes.BOOLEAN,
      allowNull: false,
      defaultValue: false
    },
    email: {
      type: DataTypes.STRING,
      allowNull: false,
      unique: true
    },
    membership: {
      type: DataTypes.STRING,
      allowNull: false
    }
  }, {
    timestamps: false
  });
	return Users;
};
