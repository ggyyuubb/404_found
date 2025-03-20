CREATE TABLE Comments (
    comment_id NUMBER PRIMARY KEY,
    post_id NUMBER NOT NULL,
    user_id NUMBER NOT NULL,
    content CLOB NOT NULL,
    created_at TIMESTAMP DEFAULT SYSTIMESTAMP,
    CONSTRAINT fk_comment_post FOREIGN KEY (post_id) REFERENCES Community(post_id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

-- 자동 증가 SEQUENCE & TRIGGER
CREATE SEQUENCE comment_seq START WITH 1 INCREMENT BY 1;
CREATE OR REPLACE TRIGGER comment_trigger
BEFORE INSERT ON Comments
FOR EACH ROW
BEGIN
    SELECT comment_seq.NEXTVAL INTO :NEW.comment_id FROM DUAL;
END;
/
