Drop DATABASE IF EXISTS cab302;
CREATE DATABASE IF NOT EXISTS cab302;

USE cab302;
-- use the sql query "describe table;" at a mariadb console to get column data types and other info

CREATE TABLE IF NOT EXISTS user (
    id INT UNSIGNED AUTO_INCREMENT,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    salt VARBINARY(32) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY (username)
);

CREATE TABLE If NOT EXISTS permission(
    id INT UNSIGNED AUTO_INCREMENT,
    user_id INT UNSIGNED NOT NULL,
    create_billboard TINYINT(1) NOT NULL,
    edit_billboard TINYINT(1) NOT NULL,
    schedule_billboard TINYINT(1) NOT NULL,
    edit_user TINYINT(1) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_permission_user
        FOREIGN KEY (user_id) REFERENCES user (id)
);

CREATE TABLE IF NOT EXISTS billboard(
    id INT UNSIGNED AUTO_INCREMENT NOT NULL ,
    user_id INT UNSIGNED NOT NULL,
    schedule_id INT UNSIGNED DEFAULT NULL,
    billboard_name VARCHAR(255) UNIQUE,
    xml_data TEXT NOT NULL,
    status BOOLEAN NOT NULL DEFAULT false,
    PRIMARY KEY (id),
    CONSTRAINT fk_billboard_user
        FOREIGN KEY (user_id) REFERENCES user (id)
    /*
    CONSTRAINT fk_schedule_id
        FOREIGN KEY (schedule_id) REFERENCES schedule (id)
    */
);

--complete as per requirements
CREATE TABLE IF NOT EXISTS schedule(
    id INT UNSIGNED AUTO_INCREMENT NOT NULL ,
    user_id INT UNSIGNED NOT NULL,
    billboard_id INT UNSIGNED NOT NULL,
    start_time timestamp default current_timestamp,
    duration INT default 60, -- Seconds that the billboard will be displayed
    PRIMARY KEY (id),
    CONSTRAINT fk_schedule_user
        FOREIGN KEY (user_id) REFERENCES user (id),
    CONSTRAINT fk_schedule_billboard
        FOREIGN KEY (billboard_id) REFERENCES billboard (id)
);

INSERT INTO user VALUES (1, 'ADMIN', 'pass', '' );
INSERT INTO permission VALUES (1, 1, true, true, true, true);

--INSERT INTO billboard VALUES (1, 1, 'BillboardName', '1', 'xml data', false );