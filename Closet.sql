CREATE TABLE Weather (
    weather_id NUMBER PRIMARY KEY,
    temperature NUMBER NOT NULL,
    humidity NUMBER NOT NULL,
    wind_speed NUMBER NOT NULL,
    condition VARCHAR2(100) NOT NULL,
    recorded_at TIMESTAMP DEFAULT SYSTIMESTAMP
);

-- 자동 증가 SEQUENCE & TRIGGER
CREATE SEQUENCE weather_seq START WITH 1 INCREMENT BY 1;
CREATE OR REPLACE TRIGGER weather_trigger
BEFORE INSERT ON Weather
FOR EACH ROW
BEGIN
    SELECT weather_seq.NEXTVAL INTO :NEW.weather_id FROM DUAL;
END;
/
