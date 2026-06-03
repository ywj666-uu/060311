-- Demo venue: 5 rows x 10 columns with windows on both sides
INSERT INTO venue (name, total_rows, total_cols, has_window_left, has_window_right) VALUES ('多功能会议室A', 5, 10, true, true);

-- Generate seats for venue 1 (5x10 = 50 seats)
-- Row 1 (front)
INSERT INTO seat (venue_id, row_num, col_num, area_tag, is_available) VALUES (1, 1, 1, 'WINDOW_LEFT', true);
INSERT INTO seat (venue_id, row_num, col_num, area_tag, is_available) VALUES (1, 1, 2, 'FRONT', true);
INSERT INTO seat (venue_id, row_num, col_num, area_tag, is_available) VALUES (1, 1, 3, 'FRONT', true);
INSERT INTO seat (venue_id, row_num, col_num, area_tag, is_available) VALUES (1, 1, 4, 'FRONT', true);
INSERT INTO seat (venue_id, row_num, col_num, area_tag, is_available) VALUES (1, 1, 5, 'FRONT', true);
INSERT INTO seat (venue_id, row_num, col_num, area_tag, is_available) VALUES (1, 1, 6, 'FRONT', true);
INSERT INTO seat (venue_id, row_num, col_num, area_tag, is_available) VALUES (1, 1, 7, 'FRONT', true);
INSERT INTO seat (venue_id, row_num, col_num, area_tag, is_available) VALUES (1, 1, 8, 'FRONT', true);
INSERT INTO seat (venue_id, row_num, col_num, area_tag, is_available) VALUES (1, 1, 9, 'FRONT', true);
INSERT INTO seat (venue_id, row_num, col_num, area_tag, is_available) VALUES (1, 1, 10, 'WINDOW_RIGHT', true);

-- Row 2 (front)
INSERT INTO seat (venue_id, row_num, col_num, area_tag, is_available) VALUES (1, 2, 1, 'WINDOW_LEFT', true);
INSERT INTO seat (venue_id, row_num, col_num, area_tag, is_available) VALUES (1, 2, 2, 'FRONT', true);
INSERT INTO seat (venue_id, row_num, col_num, area_tag, is_available) VALUES (1, 2, 3, 'FRONT', true);
INSERT INTO seat (venue_id, row_num, col_num, area_tag, is_available) VALUES (1, 2, 4, 'FRONT', true);
INSERT INTO seat (venue_id, row_num, col_num, area_tag, is_available) VALUES (1, 2, 5, 'FRONT', true);
INSERT INTO seat (venue_id, row_num, col_num, area_tag, is_available) VALUES (1, 2, 6, 'FRONT', true);
INSERT INTO seat (venue_id, row_num, col_num, area_tag, is_available) VALUES (1, 2, 7, 'FRONT', true);
INSERT INTO seat (venue_id, row_num, col_num, area_tag, is_available) VALUES (1, 2, 8, 'FRONT', true);
INSERT INTO seat (venue_id, row_num, col_num, area_tag, is_available) VALUES (1, 2, 9, 'FRONT', true);
INSERT INTO seat (venue_id, row_num, col_num, area_tag, is_available) VALUES (1, 2, 10, 'WINDOW_RIGHT', true);

-- Row 3 (middle)
INSERT INTO seat (venue_id, row_num, col_num, area_tag, is_available) VALUES (1, 3, 1, 'WINDOW_LEFT', true);
INSERT INTO seat (venue_id, row_num, col_num, area_tag, is_available) VALUES (1, 3, 2, 'MIDDLE', true);
INSERT INTO seat (venue_id, row_num, col_num, area_tag, is_available) VALUES (1, 3, 3, 'MIDDLE', true);
INSERT INTO seat (venue_id, row_num, col_num, area_tag, is_available) VALUES (1, 3, 4, 'MIDDLE', true);
INSERT INTO seat (venue_id, row_num, col_num, area_tag, is_available) VALUES (1, 3, 5, 'MIDDLE', true);
INSERT INTO seat (venue_id, row_num, col_num, area_tag, is_available) VALUES (1, 3, 6, 'MIDDLE', true);
INSERT INTO seat (venue_id, row_num, col_num, area_tag, is_available) VALUES (1, 3, 7, 'MIDDLE', true);
INSERT INTO seat (venue_id, row_num, col_num, area_tag, is_available) VALUES (1, 3, 8, 'MIDDLE', true);
INSERT INTO seat (venue_id, row_num, col_num, area_tag, is_available) VALUES (1, 3, 9, 'MIDDLE', true);
INSERT INTO seat (venue_id, row_num, col_num, area_tag, is_available) VALUES (1, 3, 10, 'WINDOW_RIGHT', true);

-- Row 4 (back)
INSERT INTO seat (venue_id, row_num, col_num, area_tag, is_available) VALUES (1, 4, 1, 'WINDOW_LEFT', true);
INSERT INTO seat (venue_id, row_num, col_num, area_tag, is_available) VALUES (1, 4, 2, 'BACK', true);
INSERT INTO seat (venue_id, row_num, col_num, area_tag, is_available) VALUES (1, 4, 3, 'BACK', true);
INSERT INTO seat (venue_id, row_num, col_num, area_tag, is_available) VALUES (1, 4, 4, 'BACK', true);
INSERT INTO seat (venue_id, row_num, col_num, area_tag, is_available) VALUES (1, 4, 5, 'BACK', true);
INSERT INTO seat (venue_id, row_num, col_num, area_tag, is_available) VALUES (1, 4, 6, 'BACK', true);
INSERT INTO seat (venue_id, row_num, col_num, area_tag, is_available) VALUES (1, 4, 7, 'BACK', true);
INSERT INTO seat (venue_id, row_num, col_num, area_tag, is_available) VALUES (1, 4, 8, 'BACK', true);
INSERT INTO seat (venue_id, row_num, col_num, area_tag, is_available) VALUES (1, 4, 9, 'BACK', true);
INSERT INTO seat (venue_id, row_num, col_num, area_tag, is_available) VALUES (1, 4, 10, 'WINDOW_RIGHT', true);

-- Row 5 (back)
INSERT INTO seat (venue_id, row_num, col_num, area_tag, is_available) VALUES (1, 5, 1, 'WINDOW_LEFT', true);
INSERT INTO seat (venue_id, row_num, col_num, area_tag, is_available) VALUES (1, 5, 2, 'BACK', true);
INSERT INTO seat (venue_id, row_num, col_num, area_tag, is_available) VALUES (1, 5, 3, 'BACK', true);
INSERT INTO seat (venue_id, row_num, col_num, area_tag, is_available) VALUES (1, 5, 4, 'BACK', true);
INSERT INTO seat (venue_id, row_num, col_num, area_tag, is_available) VALUES (1, 5, 5, 'BACK', true);
INSERT INTO seat (venue_id, row_num, col_num, area_tag, is_available) VALUES (1, 5, 6, 'BACK', true);
INSERT INTO seat (venue_id, row_num, col_num, area_tag, is_available) VALUES (1, 5, 7, 'BACK', true);
INSERT INTO seat (venue_id, row_num, col_num, area_tag, is_available) VALUES (1, 5, 8, 'BACK', true);
INSERT INTO seat (venue_id, row_num, col_num, area_tag, is_available) VALUES (1, 5, 9, 'BACK', true);
INSERT INTO seat (venue_id, row_num, col_num, area_tag, is_available) VALUES (1, 5, 10, 'WINDOW_RIGHT', true);

-- Demo activity
INSERT INTO activity (title, description, venue_id, start_time, status, created_at) VALUES ('2026年春季技术分享会', '本次活动邀请优秀学长分享实习经验', 1, '2026-06-15 14:00:00', 'OPEN', '2026-06-01 10:00:00');

-- Demo users
INSERT INTO app_user (student_id, name) VALUES ('2022001', '张三');
INSERT INTO app_user (student_id, name) VALUES ('2022002', '李四');
INSERT INTO app_user (student_id, name) VALUES ('2022003', '王五');
INSERT INTO app_user (student_id, name) VALUES ('2022004', '赵六');
INSERT INTO app_user (student_id, name) VALUES ('2022005', '钱七');
