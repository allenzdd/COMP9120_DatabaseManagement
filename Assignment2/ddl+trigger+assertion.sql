CREATE TABLE Country(
code CHAR(2) PRIMARY KEY,
name VARCHAR(50) UNIQUE NOT NULL);

create table Place(
name varchar(50) CHECK(REGEXP_LIKE(name,'^((a:)|(s:))','i')) primary key,
longitude float not null check (longitude between -180 and 180),
latitude float not null check (latitude between -90 and 90),
address varchar(100));


CREATE TABLE Accommodation(
name VARCHAR(50) CHECK(REGEXP_LIKE(name,'^(A:)','i')) PRIMARY KEY,
FOREIGN KEY (name) REFERENCES Place(name) ON DELETE CASCADE
);

create table Venue(
name varchar(50) CHECK(REGEXP_LIKE(name,'^(S:)','i')) references place(name) ON DELETE CASCADE, 
primary key (name)
);

CREATE TABLE Member(
member_id CHAR(10) PRIMARY KEY CHECK(REGEXP_LIKE(member_id,'^[1-4][[:digit:]]{9}|^[5-7][[:digit:]]{9}|^[8-9][[:digit:]]{9}','i')) NOT NULL,
title CHAR(2) NOT NULL CHECK (title in ('Mr','Ms')),
family_name VARCHAR(30),
given_name VARCHAR(40),
"from" CHAR(2) NOT NULL,
lives_in VARCHAR(50),
FOREIGN KEY ("from") REFERENCES Country(code) ON DELETE CASCADE,
FOREIGN KEY (lives_in) REFERENCES Accommodation(name) ON DELETE CASCADE,
CONSTRAINT check_name CHECK (family_name IS NOT NULL OR given_name IS NOT NULL));

create table Sport(
name varchar(20) PRIMARY key);

create table Vehicle(
code char(8) primary key check (regexp_like(code,'[[:alnum:]]{8}','i')),
capacity smallint not null check (capacity >0));



CREATE TABLE Staff(
member_id CHAR(10) PRIMARY KEY CHECK (REGEXP_LIKE (member_id, '^[8-9][[:digit:]]{9}','i')),
FOREIGN KEY(member_id) REFERENCES MEMBER(member_id) ON DELETE CASCADE
);

CREATE TABLE athlete(
member_id CHAR(10) PRIMARY KEY CHECK(REGEXP_LIKE (member_id, '^[1-4][[:digit:]]{9}','i')),
FOREIGN KEY(member_id) REFERENCES MEMBER(member_id) ON DELETE CASCADE
);

CREATE TABLE Official(
member_id CHAR(10) PRIMARY KEY CHECK(REGEXP_LIKE (member_id,'^[5-7][[:digit:]]{9}','i')),
FOREIGN KEY(member_id) REFERENCES MEMBER(member_id) ON DELETE CASCADE
);



CREATE TABLE Event(
name VARCHAR(50) PRIMARY KEY,
result_type VARCHAR(60) NOT NULL,
start_time TIMESTAMP NOT NULL,
start_date DATE NOT NULL,
sport_venue VARCHAR(80) NOT NULL,
sport_name VARCHAR(20) NOT NULL,
FOREIGN KEY(sport_name) REFERENCES SPORT(name)
);



CREATE TABLE runs(
event_name VARCHAR(50),
official_id CHAR(10) CHECK(REGEXP_LIKE (official_id,'^[5-7][[:digit:]]{9}','i')),
role VARCHAR(20) NOT NULL,
PRIMARY KEY(event_name,official_id),
FOREIGN KEY(event_name) REFERENCES EVENT(name),
FOREIGN KEY(official_id) REFERENCES OFFICIAL(member_id));


CREATE TABLE participates(
event_name VARCHAR(50),
athlete_id CHAR(10) REFERENCES ATHLETE(member_id),
result VARCHAR(20) NOT NULL,
medal VARCHAR(8) CHECK (medal IN ('GOLD','SILVER','BRONZE') OR medal is NULL));


create table journey(
code char(8) references vehicle(code) check (regexp_like(code,'[[:alnum:]]{8}','i')),
start_time timestamp (6),
start_date date,
nbooked smallint default 0 check(nbooked >=0) not null,
"to" varchar(50) references Place(name)not null, 
"from" varchar(50) references Place(name) not null,
primary key (code,start_time,start_date),
CONSTRAINT palcecheck check ("to"!="from"));

create table books(
member_id CHAR(10) CHECK(REGEXP_LIKE(member_id,'^[1-4][[:digit:]]{9}|^[5-7][[:digit:]]{9}|^[8-9][[:digit:]]{9}','i')),
start_date date,
start_time timestamp(6) ,
code char(8) check (regexp_like(code,'[[:alnum:]]{8}','i')),
"when" timestamp default current_timestamp,
"by" CHAR(10) CHECK (REGEXP_LIKE ("by", '^[8-9][[:digit:]]{9}','i')) not null,
primary key (member_id,start_time,start_date,code),
foreign key (member_id) references member(member_id),
foreign key (start_date,start_time,code) references JOURNEY(START_DATE,start_time,code),
foreign key ("by") references staff(member_id));


CREATE VIEW Athlete_Detail AS
SELECT DISTINCT M.member_id, M.title, M.family_name, M.given_name, 
                                            (SELECT COUNT(athlete_id) FROM participates WHERE medal = 'GOLD' AND member_id=M.member_id) AS GOLD,
                                            (SELECT COUNT(athlete_id) FROM participates WHERE medal = 'SILVER' AND member_id=M.member_id) AS SILVER,
                                            (SELECT COUNT(athlete_id) FROM participates WHERE medal = 'BRONZER' AND member_id=M.member_id) AS BRONZER
FROM MEMBER M JOIN PARTICIPATES P ON (M.member_id=P.athlete_id);


CREATE TRIGGER CHECK_BOOKED
AFTER INSERT ON BOOKS
FOR EACH ROW
DECLARE
C_nbooked INTEGER;
V_code CHAR(8);
J_code CHAR(10);
V_capacity SMALLINT;
BEGIN
 SELECT nbooked,code INTO C_nbooked,J_code FROM JOURNEY;
 SELECT capacity,code INTO V_capacity,V_code FROM VEHICLE;
    IF V_code = :NEW.code AND J_code = :NEW.code AND C_nbooked < V_capacity THEN
    C_nbooked := C_nbooked+1;
    UPDATE JOURNEY SET nbooked = C_nbooked;
    ELSIF C_nbooked = V_capacity THEN
    DBMS_OUTPUT.PUT_LINE('The Vehicle is full');
    ROLLBACK;
    END IF;
END CHECK_BOOKED;



CREATE TRIGGER MEMBER_AUTO_ADD
AFTER INSERT ON MEMBER
FOR EACH ROW
BEGIN
        IF REGEXP_LIKE (:NEW.member_id,'^[1-4][[:digit:]]{9}','i') THEN
        INSERT INTO ATHLETE VALUES(:NEW.member_id);
        ELSIF REGEXP_LIKE (:NEW.member_id,'^[5-7][[:digit:]]{9}','i') THEN
        INSERT INTO OFFICIAL VALUES (:NEW.member_id);
        ELSIF REGEXP_LIKE (:NEW.member_id,'^[8-9][[:digit:]]{9}','i') THEN
        INSERT INTO STAFF VALUES (:NEW.member_id);
        ELSE
        DBMS_OUTPUT.PUT_LINE('Your member_id is Wrong, Please Check it wheter have 10 numbers');
        END IF;
END MEMBER_AUTO_ADD;


--ASSERTION:
--create assertion book_check as check
--not exists(SELECT nbooked 
--from journey j join books b using (start_time,start_date,code)
--where nbooked!=(select count(*) 
--from books b2 
--where b2.code=journey.code));