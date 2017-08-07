CREATE SCHEMA transit;

use transit;

CREATE TABLE `transit`.`agency` (
  `id` VARCHAR(36) NOT NULL,
  `agency_name` VARCHAR(100) NULL,
  `agency_url` VARCHAR(255) NULL,
  `agency_timezone` VARCHAR(32) NULL,
  `agency_lang` VARCHAR(32) NULL,
  `agency_phone` VARCHAR(32) NULL,
  `agency_fare_url` VARCHAR(255) NULL,
  PRIMARY KEY (`id`));

CREATE TABLE `transit`.`calendar` (
  `id` VARCHAR(36) NOT NULL,
  `service_id` VARCHAR(32) NOT NULL,
  `monday` TINYINT(1) NOT NULL DEFAULT 0,
  `tuesday` TINYINT(1) NOT NULL DEFAULT 0,
  `wednesday` TINYINT(1) NOT NULL DEFAULT 0,
  `thursday` TINYINT(1) NOT NULL DEFAULT 0,
  `friday` TINYINT(1) NOT NULL DEFAULT 0,
  `saturday` TINYINT(1) NOT NULL DEFAULT 0,
  `sunday` TINYINT(1) NOT NULL DEFAULT 0,
  `start_date` DATE NOT NULL,
  `end_date` DATE NOT NULL,
  PRIMARY KEY (`id`));

CREATE TABLE `transit`.`calendar_dates` (
  `id` VARCHAR(36) NOT NULL,
  `service_id` varchar(32) NOT NULL,
  `date` date NOT NULL,
  `exception_type` int(2) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`));

CREATE TABLE `transit`.`fare_attributes` (
  `id` VARCHAR(36) NOT NULL,
  `fare_id` VARCHAR(32) NOT NULL,
  `price` FLOAT NULL,
  `currency_type` VARCHAR(32) NULL,
  `payment_method` INT(1) NULL,
  `transfers` INT(1) NULL,
  `transfer_duration` INT(11) NULL,
  PRIMARY KEY (`id`));

CREATE TABLE `transit`.`fare_rules` (
  `id` VARCHAR(36) NOT NULL,
  `fare_id` VARCHAR(32) NOT NULL,
  `route_id` VARCHAR(32) NULL,
  `origin_id` VARCHAR(32) NULL,
  `destination_id` VARCHAR(32) NULL,
  `contains_id` VARCHAR(32) NULL,
  PRIMARY KEY (`id`));

CREATE TABLE `transit`.`feed_info` (
  `id` VARCHAR(36) NOT NULL,
  `feed_publisher_name` VARCHAR(100) NULL,
  `feed_publisher_url` VARCHAR(100) NULL,
  `feed_lang` VARCHAR(32) NULL,
  `feed_start_date` DATE NULL,
  `feed_end_date` DATE NULL,
  `feed_version` VARCHAR(32) NULL,
  PRIMARY KEY (`id`));

CREATE TABLE `transit`.`routes` (
  `id` VARCHAR(36) NOT NULL,
  `agency_id` VARCHAR(32) NULL,
  `route_short_name` VARCHAR(100) NULL,
  `route_long_name` VARCHAR(100) NULL,
  `route_desc` VARCHAR(100) NULL,
  `route_type` VARCHAR(32) NULL,
  `route_url` VARCHAR(100) NULL,
  `route_color` VARCHAR(32) NULL,
  `route_text_color` VARCHAR(32) NULL,
  PRIMARY KEY (`id`));

CREATE TABLE `transit`.`shapes` (
  `id` VARCHAR(36) NOT NULL,
  `shape_id` VARCHAR(36) NOT NULL,
  `shape_pt_lat` DOUBLE NULL,
  `shape_pt_lon` DOUBLE NULL,
  `shape_pt_sequence` INT(11) NULL,
  `shape_dist_traveled` DOUBLE NULL,
  PRIMARY KEY (`id`));

CREATE TABLE `transit`.`stop_times` (
  `id` VARCHAR(36) NOT NULL,
  `trip_id` VARCHAR(32) NULL,
  `arrival_time` TIME(0) NULL,
  `departure_time` TIME(0) NULL,
  `stop_id` VARCHAR(32) NULL,
  `stop_sequence` INT(11) NULL,
  `stop_headsign` VARCHAR(100) NULL,
  `pickup_type` INT(1) NULL,
  `drop_off_type` INT(1) NULL,
  `shape_dist_traveled` DOUBLE NULL,
  PRIMARY KEY (`id`));

CREATE TABLE `transit`.`stops` (
  `id` VARCHAR(36) NOT NULL,
  `stop_code` VARCHAR(32) NULL,
  `stop_name` VARCHAR(100) NULL,
  `stop_desc` VARCHAR(100) NULL,
  `stop_lat` DOUBLE NULL,
  `stop_lon` DOUBLE NULL,
  `zone_id` VARCHAR(32) NULL,
  `stop_url` VARCHAR(100) NULL,
  `location_type` TINYINT(1) NULL,
  `parent_station` TINYINT(1) NULL,
  `stop_timezone` VARCHAR(32) NULL,
  `wheelchair_boarding` TINYINT(1) NULL,
  PRIMARY KEY (`id`));

CREATE TABLE `transit`.`trips` (
  `id` VARCHAR(36) NOT NULL,
  `route_id` VARCHAR(32) NULL,
  `service_id` VARCHAR(32) NULL,
  `trip_headsign` VARCHAR(100) NULL,
  `trip_short_name` VARCHAR(100) NULL,
  `direction_id` TINYINT(1) NULL,
  `block_id` VARCHAR(32) NULL,
  `shape_id` VARCHAR(32) NULL,
  `wheelchair_accessible` TINYINT(1) NULL,
  `bikes_allowed` TINYINT(1) NULL,
  PRIMARY KEY (`id`));

DELIMITER $$
CREATE TRIGGER calendar_id
BEFORE INSERT ON calendar
FOR EACH  ROW
BEGIN
    SET NEW.id = UUID();
END$$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER calendar_dates_id
BEFORE INSERT ON calendar_dates
FOR EACH  ROW
BEGIN
    SET NEW.id = UUID();
END$$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER fare_attributes_id
BEFORE INSERT ON fare_attributes
FOR EACH  ROW
BEGIN
    SET NEW.id = UUID();
END$$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER fare_rules_id
BEFORE INSERT ON fare_rules
FOR EACH  ROW
BEGIN
    SET NEW.id = UUID();
END$$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER feed_info_id
BEFORE INSERT ON feed_info
FOR EACH  ROW
BEGIN
    SET NEW.id = UUID();
END$$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER shapes_id
BEFORE INSERT ON shapes
FOR EACH  ROW
BEGIN
    SET NEW.id = UUID();
END$$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER stop_times_id
BEFORE INSERT ON stop_times
FOR EACH  ROW
BEGIN
    SET NEW.id = UUID();
END$$
DELIMITER ;

CREATE
     OR REPLACE
VIEW `route_to_stop` AS
    SELECT
        CONCAT(`tr`.`route_id`,
                `sp`.`id`,
                `tr`.`direction_id`) AS `id`,
        `tr`.`route_id` AS `route_id`,
        `sp`.`id` AS `stop_id`,
        `tr`.`direction_id` AS `direction_id`,
         MAX(st.stop_sequence) AS stop_sequence
    FROM
        ((`trips` `tr`
        JOIN `stop_times` `st` ON ((`tr`.`id` = `st`.`trip_id`)))
        JOIN `stops` `sp` ON ((`sp`.`id` = `st`.`stop_id`)))
        GROUP BY tr.route_id, sp.id, tr.direction_id;

CREATE OR REPLACE VIEW `stop_times_by_stop` AS
SELECT DISTINCT
    `st`.`id` AS `id`,
    `st`.`trip_id` AS `trip_id`,
    `st`.`arrival_time` AS `arrival_time`,
    `st`.`departure_time` AS `departure_time`,
    `st`.`stop_id` AS `stop_id`,
    `st`.`stop_sequence` AS `stop_sequence`,
    `st`.`stop_headsign` AS `stop_headsign`,
    `st`.`pickup_type` AS `pickup_type`,
    `st`.`drop_off_type` AS `drop_off_type`,
    `st`.`shape_dist_traveled` AS `shape_dist_traveled`,
    `c`.`start_date` AS `start_date`,
    `c`.`end_date` AS `end_date`,
    `c`.`monday` AS `monday`,
    `c`.`tuesday` AS `tuesday`,
    `c`.`wednesday` AS `wednesday`,
    `c`.`thursday` AS `thursday`,
    `c`.`friday` AS `friday`,
    `c`.`saturday` AS `saturday`,
    `c`.`sunday` AS `sunday`,
    `t`.`route_id` AS `route_id`
FROM
    ((`calendar` `c`
    JOIN `trips` `t` ON ((`t`.`service_id` = `c`.`service_id`)))
    JOIN `stop_times` `st` ON ((`st`.`trip_id` = `t`.`id`)))