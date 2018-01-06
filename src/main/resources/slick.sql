create table department(
 id bigserial primary key,
  no int,
  name varchar(255)
);

create table users(
  id bigserial primary key,
  deptid bigint,
  name varchar(255),
  gender int,
  birthday bigint,
  phone varchar(255),
  wx varchar(255),
  createtime bigint
);
