CREATE TABLE C1 
(
    cid int not null primary key generated by default as identity,
    name varchar(32) not null
);

 CREATE TABLE C2 
 (
    cid int not null primary key generated always as identity,
    name varchar(32) not null
 );
 

 CREATE TABLE c3
 (
    name varchar(32) not null, 
    address varchar(64) not null,
    cid int not null primary key
);


CREATE TABLE c4
(
    cid int not null, 
    oid int not null,
    items int not null,
    primary key (cid, oid)
);    

CREATE TABLE c5
(
	cid int not null,
	oid int not null
);

CREATE TABLE c6 (
	cid int not null primary key,
	name varchar (32),
	address varchar (32)
);

CREATE TABLE long_str (
    id INT NOT NULL PRIMARY KEY,
    v VARCHAR(512)
);

CREATE TABLE types (
    id int primary key,
    a_int int,
    a_uint int unsigned,
    a_float float,
    a_ufloat float unsigned,
    a_double double,
    a_udouble double unsigned,
    a_decimal decimal(5, 2),
    a_varchar varchar(512),
    a_date date,
    a_time time,
    a_datetime datetime,
    a_boolean boolean
);
