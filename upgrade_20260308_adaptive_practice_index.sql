SET NAMES utf8mb4;

SET @db = DATABASE();

SET @idx_exists = (
  SELECT COUNT(1)
  FROM information_schema.statistics
  WHERE table_schema = @db
    AND table_name = 'qb_question_user_stat'
    AND index_name = 'idx_qbus_user_question'
);
SET @sql = IF(
  @idx_exists = 0,
  'ALTER TABLE qb_question_user_stat ADD INDEX idx_qbus_user_question (user_id, question_id)',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists = (
  SELECT COUNT(1)
  FROM information_schema.statistics
  WHERE table_schema = @db
    AND table_name = 'qb_wrong_question'
    AND index_name = 'idx_qbw_user_resolved_question'
);
SET @sql = IF(
  @idx_exists = 0,
  'ALTER TABLE qb_wrong_question ADD INDEX idx_qbw_user_resolved_question (user_id, is_resolved, question_id)',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists = (
  SELECT COUNT(1)
  FROM information_schema.statistics
  WHERE table_schema = @db
    AND table_name = 'qb_question_tag'
    AND index_name = 'idx_qbqt_question_tag'
);
SET @sql = IF(
  @idx_exists = 0,
  'ALTER TABLE qb_question_tag ADD INDEX idx_qbqt_question_tag (question_id, tag_id)',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
