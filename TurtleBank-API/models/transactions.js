module.exports = function(sequelize, DataTypes) {
    var Transactions = sequelize.define("transactions", {
         id: {
             type: DataTypes.INTEGER,
             primaryKey: true,
             autoIncrement: true,
         },
         from_bankcode: {
             type: DataTypes.INTEGER,
             allowNull: false
         },
         from_account: {
             type: DataTypes.INTEGER,
             allowNull: false
         },
         to_bankcode: {
             type: DataTypes.INTEGER,
             allowNull: false
         },
         to_account: {
             type: DataTypes.INTEGER,
             allowNull: false
         },
         amount: {
             type: DataTypes.INTEGER,
             allowNull: false
         },
         sendtime: {
             type: DataTypes.DATE,
             allowNull: false
         }
     }, {
         timestamps: false
     });
    return Transactions;
 };