SET NAMES utf8mb4;

ALTER TABLE qb_question
    ADD COLUMN bank_review_status TINYINT NOT NULL DEFAULT 0 COMMENT '0=private,1=pending,2=approved,3=rejected' AFTER status,
    ADD COLUMN bank_reviewer_id BIGINT UNSIGNED NULL AFTER bank_review_status,
    ADD COLUMN bank_reviewed_at DATETIME(3) NULL AFTER bank_reviewer_id,
    ADD COLUMN bank_review_comment VARCHAR(255) NULL AFTER bank_reviewed_at,
    ADD KEY idx_qb_question_bank_review (bank_review_status, status),
    ADD CONSTRAINT fk_question_bank_reviewer FOREIGN KEY (bank_reviewer_id) REFERENCES sys_user(id);

UPDATE qb_question q
JOIN sys_user_role ur ON ur.user_id = q.created_by
JOIN sys_role r ON r.id = ur.role_id AND r.role_code = 'ADMIN'
SET q.bank_review_status = 2,
    q.bank_reviewer_id = q.created_by,
    q.bank_reviewed_at = COALESCE(q.updated_at, q.created_at),
    q.bank_review_comment = NULL
WHERE q.is_deleted = 0
  AND q.bank_review_status = 0;

ALTER TABLE qb_paper
    DROP COLUMN status,
    MODIFY COLUMN paper_type TINYINT NOT NULL DEFAULT 1 COMMENT '1=assignment,2=paper';
