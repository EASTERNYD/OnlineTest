-- 智能在线考试系统 建表脚本（幂等，可重复执行）

CREATE TABLE IF NOT EXISTS sys_user (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    nickname    VARCHAR(50),
    role        TINYINT DEFAULT 1 COMMENT '0-管理员，1-学生',
    status      TINYINT DEFAULT 1 COMMENT '0-禁用，1-启用',
    create_time DATETIME
);

CREATE TABLE IF NOT EXISTS banner (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    title       VARCHAR(100),
    image_url   VARCHAR(255),
    link_url    VARCHAR(255),
    sort_order  INT DEFAULT 0,
    status      TINYINT DEFAULT 1 COMMENT '0-禁用，1-启用',
    create_time DATETIME
);

CREATE TABLE IF NOT EXISTS notice (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    title       VARCHAR(100),
    content     TEXT,
    type        TINYINT DEFAULT 1 COMMENT '1-通知，2-警告，3-活动',
    status      TINYINT DEFAULT 1 COMMENT '0-草稿，1-发布',
    create_time DATETIME
);

CREATE TABLE IF NOT EXISTS category (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(50),
    parent_id   BIGINT DEFAULT 0,
    sort_order  INT DEFAULT 0,
    create_time DATETIME
);

CREATE TABLE IF NOT EXISTS question (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_id BIGINT,
    stem        TEXT,
    options     TEXT COMMENT '选项 JSON 数组',
    answer      VARCHAR(10) COMMENT '正确答案，单选 A，多选 ABD',
    analysis    TEXT,
    difficulty  TINYINT DEFAULT 1 COMMENT '1-简单，2-中等，3-困难',
    view_count  INT DEFAULT 0,
    type        TINYINT DEFAULT 0 COMMENT '0-单选，1-多选',
    create_time DATETIME
);

CREATE TABLE IF NOT EXISTS exam (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    name         VARCHAR(100),
    total_score  INT,
    duration     INT COMMENT '考试时长(分钟)',
    question_ids TEXT COMMENT '题目ID列表 JSON 数组',
    status       TINYINT DEFAULT 0 COMMENT '0-草稿，1-已发布，2-已结束',
    create_time  DATETIME
);

CREATE TABLE IF NOT EXISTS exam_record (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id       BIGINT,
    exam_id       BIGINT,
    answers       TEXT COMMENT '考生答案 JSON 对象',
    score         INT DEFAULT 0,
    correct_count INT DEFAULT 0,
    wrong_count   INT DEFAULT 0,
    status        TINYINT DEFAULT 0 COMMENT '0-进行中，1-已交卷',
    start_time    DATETIME,
    submit_time   DATETIME,
    create_time   DATETIME
);
