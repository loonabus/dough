DROP TABLE IF EXISTS "USER";

CREATE TABLE "USER" (
	USER_ID INT AUTO_INCREMENT PRIMARY KEY,
	USER_NAME VARCHAR(100) NOT NULL UNIQUE,
	CREATE_DT TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
