CREATE TABLE sys_foundation_probe (
    id BIGINT NOT NULL AUTO_INCREMENT,
    probe_key VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_foundation_probe_key (probe_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
