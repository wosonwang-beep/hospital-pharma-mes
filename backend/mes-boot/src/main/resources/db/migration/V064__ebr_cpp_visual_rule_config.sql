-- CPP visual rule designer metadata. Existing eBR rule expression remains executable source.
ALTER TABLE ebr_rule_def
  ADD COLUMN cpp_config_json LONGTEXT NULL AFTER active_flag,
  ADD CONSTRAINT chk_ebr_rule_cpp_config_json
    CHECK (cpp_config_json IS NULL OR JSON_VALID(cpp_config_json));
