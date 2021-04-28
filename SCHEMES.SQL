CREATE TABLE week
(
    dayofweek INTEGER NOT NULL UNIQUE,
    timetable TEXT NOT NULL, -- Actually json of [{"link": "...", "name": "Алгоритмы", "subtype": "Лекция", "start_time": 123, "end_time": 456}, ...]
    PRIMARY KEY(dayofweek)
);

CREATE TABLE changes
(
    id            INTEGER,
    date          INTEGER UNIQUE NOT NULL,
    message       TEXT,
    new_timetable TEXT, -- json same as timetable
    PRIMARY KEY (id AUTOINCREMENT)
);
