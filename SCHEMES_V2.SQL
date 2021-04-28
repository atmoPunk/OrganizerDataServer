CREATE TABLE "timetable" (
                             "id"	INTEGER,
                             "weekday"	INTEGER NOT NULL,
                             "link"	TEXT,
                             "name"	TEXT NOT NULL,
                             "start_time"	INTEGER NOT NULL,
                             "end_time"	INTEGER NOT NULL,
                             "subtype"	TEXT NOT NULL,
                             "matlogic"	INTEGER CHECK("matlogic" > 1 AND "matlogic" <= 2), -- 1 - Жаворонков, 2 - Халанский
                             "algos"	INTEGER CHECK("algos" > 0 AND "algos" <= 2), -- 1 - Мишунин, 2 - Лапенок
                             "formlang"	INTEGER CHECK("formlang" > 0 AND "formlang" <= 2), -- 1 - Вербицкая, 2 - Халанский
                             "spec"	INTEGER CHECK("spec" > 0 AND "spec" <= 3), -- 1 - плюсы, 2 - матстат, 3 - языки
                             "is_temp"	INTEGER NOT NULL, -- 0 - not temp, 1 - temp
                             PRIMARY KEY("id" AUTOINCREMENT)
);

CREATE TABLE "homework" (
                            "id"	INTEGER,
                            "date"	INTEGER NOT NULL,
                            "subject"	TEXT NOT NULL,
                            "link"	TEXT,
                            "data"	TEXT,
                            "matlogic"	INTEGER CHECK("matlogic" > 1 AND "matlogic" <= 2), -- 1 - Жаворонков, 2 - Халанский
                            "algos"	INTEGER CHECK("algos" > 0 AND "algos" <= 2), -- 1 - Мишунин, 2 - Лапенок
                            "formlang"	INTEGER CHECK("formlang" > 0 AND "formlang" <= 2), -- 1 - Вербицкая, 2 - Халанский
                            "spec"	INTEGER CHECK("spec" > 0 AND "spec" <= 3), -- 1 - плюсы, 2 - матстат, 3 - языки
                            PRIMARY KEY("id" AUTOINCREMENT)
);

CREATE TABLE "users" (
                         "user"	INTEGER NOT NULL UNIQUE,
                         "matlogic"	INTEGER CHECK("matlogic" > 1 AND "matlogic" <= 2), -- 1 - Жаворонков, 2 - Халанский
                         "algos"	INTEGER CHECK("algos" > 0 AND "algos" <= 2), -- 1 - Мишунин, 2 - Лапенок
                         "formlang"	INTEGER CHECK("formlang" > 0 AND "formlang" <= 2), -- 1 - Вербицкая, 2 - Халанский
                         "spec"	INTEGER CHECK("spec" > 0 AND "spec" <= 3), -- 1 - плюсы, 2 - матстат, 3 - языки
                         PRIMARY KEY("user")
);

CREATE TABLE "changes" (
                           "id"	INTEGER,
                           "date"	INTEGER NOT NULL,
                           "message"	TEXT,
                           PRIMARY KEY("id" AUTOINCREMENT)
);

CREATE TABLE "change_to_tables" (
    "id" INTEGER,
    "change_id" INTEGER,
    "timetable_id" INTEGER,
    PRIMARY KEY("id" AUTOINCREMENT),
    FOREIGN KEY("change_id") REFERENCES "changes"("id"),
    FOREIGN KEY("timetable_id") REFERENCES "timetable"("id")
);

CREATE TABLE "teacher_emails" (
    "id" INTEGER,
    "email" TEXT,
    "perfreport" TEXT,
    "subject" TEXT NOT NULL,
    "matlogic"	INTEGER CHECK("matlogic" > 1 AND "matlogic" <= 2), -- 1 - Жаворонков, 2 - Халанский
    "algos"	INTEGER CHECK("algos" > 0 AND "algos" <= 2), -- 1 - Мишунин, 2 - Лапенок
    "formlang"	INTEGER CHECK("formlang" > 0 AND "formlang" <= 2), -- 1 - Вербицкая, 2 - Халанский
    "spec"	INTEGER CHECK("spec" > 0 AND "spec" <= 3), -- 1 - плюсы, 2 - матстат, 3 - языки
    PRIMARY KEY("id" AUTOINCREMENT)
);

