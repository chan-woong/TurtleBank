module.exports = function(sequelize, DataTypes) {
	var loan = sequelize.define("loan", {
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
        loan_amount: {
            type: DataTypes.BIGINT,
            allowNull: false,
            validate:{
                min: 0
            }
        },
        loan_time: {
            type: DataTypes.DATE,
            allowNull: false
        }
	},
    {
        tableName: "loan", 
		timestamps: false
	});

    loan.associate = function(models) {
        loan.belongsTo(models.users, {
            foreignKey: 'username',
            targetKey: 'username'
        });
    };

	return loan;
};
