CREATE TABLE testtable (id int GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1) NOT NULL, input varchar(254) NOT NULL);
insert into testtable (input) values ('Insert 1');
insert into testtable (input) values ('Insert 2');
insert into testtable (input) values ('Insert 3');
ALTER TABLE testtable ADD COLUMN newinput VARCHAR(255);
