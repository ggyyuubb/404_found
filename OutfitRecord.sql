CREATE TABLE OutfitRecord (
    record_id NUMBER PRIMARY KEY,
    user_id NUMBER NOT NULL,
    closet_id NUMBER NOT NULL,
    weather_id NUMBER NOT NULL,
    outfit_date TIMESTAMP DEFAULT SYSTIMESTAMP,
    CONSTRAINT fk_outfit_user FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_outfit_closet FOREIGN KEY (closet_id) REFERENCES Closet(closet_id) ON DELETE CASCADE,
    CONSTRAINT fk_outfit_weather FOREIGN KEY (weather_id) REFERENCES Weather(weather_id) ON DELETE CASCADE
);

-- 자동 증가 SEQUENCE & TRIGGER
CREATE SEQUENCE outfit_seq START WITH 1 INCREMENT BY 1;
CREATE OR REPLACE TRIGGER outfit_trigger
BEFORE INSERT ON OutfitRecord
FOR EACH ROW
BEGIN
    SELECT outfit_seq.NEXTVAL INTO :NEW.record_id FROM DUAL;
END;
/
