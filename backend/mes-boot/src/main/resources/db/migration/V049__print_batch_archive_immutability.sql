-- Append-only evidence protections supplement V047. Never modify the executed V047.
DELIMITER $$
CREATE TRIGGER bu_print_template_type BEFORE UPDATE ON mes_print_template_version FOR EACH ROW
BEGIN
 IF NOT(OLD.print_type <=> NEW.print_type) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Immutable print template type; append a new version'; END IF;
END$$
CREATE TRIGGER bu_mes_print_batch BEFORE UPDATE ON mes_print_batch FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Immutable print batch archive'$$
CREATE TRIGGER bd_mes_print_batch BEFORE DELETE ON mes_print_batch FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Print batch archive deletion prohibited'$$
DELIMITER ;
