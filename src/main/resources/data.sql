INSERT INTO `validation_setting` (`id`) VALUES (1);
INSERT INTO `module_election` (`id`, `validation_setting_id`) VALUES (1, 1);
INSERT INTO `classes` (`name`) VALUES ("IT19b_WIN");
INSERT INTO `students` (`email`, `class_name`, `isip`, `istz`, `name`, `pa_dispensation`, `wpm_dispensation`, `election_id`) VALUES ('dev@zhaw.ch', 'IT19b_WIN', '0', '0', 'Max Mustermann', '0', '0', 1);
UPDATE `module_election` SET `student_id` = "dev@zhaw.ch" WHERE `id` = 1;