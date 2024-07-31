drop database if exists dvba;
create database if not exists dvba;
use dvba;
create table users
(
    id             integer auto_increment,
    username       varchar(100)  UNIQUE             NOT NULL,
    password       varchar(1024)                    NOT NULL,
    accountPW      varchar(1024)                    NOT NULL,
    phone          varchar(20)   UNIQUE             NOT NULL,
    account_number integer       UNIQUE,
    balance        BIGINT unsigned default 1000000   NOT NULL,
    is_admin       boolean         default false,
    email          varchar(255)                     NOT NULL,
    membership     varchar(255)                     NOT NULL,
    is_loan        boolean     default false,
    is_mydata      boolean     default false,
    PRIMARY KEY (id, username)
) engine = innodb;

create table loan
(   
    id            integer PRIMARY KEY auto_increment,
    username       varchar(100) UNIQUE          NOT NULL,
    foreign key(username) references users(username),
    loan_amount      BIGINT                    NOT NULL,
    loan_time        DATETIME                    NOT NULL
) engine = innodb;

create table transactions
(
    id              integer PRIMARY KEY auto_increment,
    from_account    int(11)     NOT NULL,
    from_bankcode   int       NOT NULL,
    to_account      VARCHAR(50) NOT NULL,
    to_bankcode     int        NOT NULL,
    amount          int         NOT NULL,
    sendtime        DATETIME
) engine = innodb;

create table beneficiaries
(
    id                         integer PRIMARY KEY auto_increment,
    account_number             int(11)               NOT NULL,
    beneficiary_account_number int(11)               NOT NULL,
    approved                   boolean default false NOT NULL
) engine = innodb;

create table smsauths
(
    id                         integer PRIMARY KEY auto_increment,
    username                   varchar(100) UNIQUE   NOT NULL,
    authnum                    integer
) engine = innodb;

create table account
(
    account_number             integer  PRIMARY KEY,
    bank_code                  integer  NOT NULL,
    username                   varchar(100) NOT NULL,
    balance                    BIGINT  NOT NULL
) engine = innodb;

INSERT INTO `users`
values (default, "admin", "edfb2fa1462f68627350058583fed9f5fcb4da78769f2cc42230f78cad50f81b", "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92", "01099999999" , 999999, default, true,
        'admin@admin', "ADMIN", default, default);

INSERT INTO `account`
values (999999, 555, "admin", 100000000);


drop database if exists board;
create database if not exists board;
use board;

create table notices
(
    id        int not null auto_increment,
    userId    varchar(30),
    title     VARCHAR(255),
    content   VARCHAR(1023),
    filepath  VARCHAR(255),
    createdAt DATETIME,
    updatedAt DATETIME,
    PRIMARY KEY (id)
) engine = innodb;

create table qnas
(
    id        int not null auto_increment,
    userId    varchar(30),
    title     VARCHAR(255),
    content   VARCHAR(1023),
    createdAt DATETIME,
    updatedAt DATETIME,
    comment   VARCHAR(1000),
    PRIMARY KEY (id)
) engine = innodb;

drop user if exists api@'%';
drop user if exists web@'%';

create user api@'%' identified by 'shieldapi';
create user web@'%' identified by 'shieldbank';

grant all privileges on dvba.* to api@'%';
grant all privileges on board.* to web@'%';

flush privileges;
