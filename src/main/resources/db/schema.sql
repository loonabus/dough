DROP TABLE IF EXISTS TEST_USER_INFO;

CREATE TABLE TEST_USER_INFO (
	TEST_USER_ID SERIAL PRIMARY KEY,
	TEST_USER_NAME VARCHAR(100) NOT NULL UNIQUE,
	CREATE_DT TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
