CREATE TABLE CUSTOMER (
    cus_id NUMBER(38,0)  CONSTRAINT cus_ID_NN NOT NULL,
    cus_name VARCHAR(50)  CONSTRAINT cus_NAME_NN NOT NULL
);

CREATE INDEX CUS_CUS_ID_I ON CUSTOMER(CUS_ID) ;
ALTER TABLE CUSTOMER ADD CONSTRAINT CUS_ID_PK PRiMARY KEY (CUS_ID);
ALTER TABLE CUSTOMER ADD CONSTRAINT CUS_NAME_UK UNIQUE (CUS_NAME);

CREATE SEQUENCE CUS_SEQ START WITH 1000 INCREMENT by 10;