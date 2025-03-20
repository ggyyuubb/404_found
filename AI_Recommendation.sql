CREATE TABLE AI_Recommendation (
    rec_id NUMBER PRIMARY KEY,
    user_id NUMBER NOT NULL,
    weather_id NUMBER NOT NULL,
    suggested_items CLOB NOT NULL,  -- JSON 데이터 저장
    created_at TIMESTAMP DEFAULT SYSTIMESTAMP,
    CONSTRAINT fk_ai_user FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_ai_weather FOREIGN KEY (weather_id) REFERENCES Weather(weather_id) ON DELETE CASCADE
);

-- 자동 증가 SEQUENCE & TRIGGER
CREATE SEQUENCE ai_rec_seq START WITH 1 INCREMENT BY 1;
CREATE OR REPLACE TRIGGER ai_rec_trigger
BEFORE INSERT ON AI_Recommendation
FOR EACH ROW
BEGIN
    SELECT ai_rec_seq.NEXTVAL INTO :NEW.rec_id FROM DUAL;
END;
/
