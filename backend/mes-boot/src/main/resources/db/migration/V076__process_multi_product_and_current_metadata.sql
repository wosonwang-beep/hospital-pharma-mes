-- Current production-process metadata and many-product applicability.
-- Existing single product links are copied; historical process/snapshot rows remain unchanged.
ALTER TABLE proc_package
  ADD COLUMN process_version VARCHAR(32) NULL,
  ADD COLUMN effective_approved_date DATE NULL,
  ADD UNIQUE KEY uk_proc_package_org_identity (org_id,id);

ALTER TABLE md_product
  ADD UNIQUE KEY uk_md_product_org_identity (org_id,id);

CREATE TABLE proc_package_product (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  org_id BIGINT NOT NULL,
  created_by BIGINT NOT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_by BIGINT NOT NULL,
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  version_no BIGINT NOT NULL DEFAULT 0,
  package_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  UNIQUE KEY uk_proc_package_product (org_id,package_id,product_id),
  KEY idx_proc_package_product_product (org_id,product_id,package_id),
  CONSTRAINT fk_proc_package_product_package
    FOREIGN KEY (org_id,package_id) REFERENCES proc_package(org_id,id)
    ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT fk_proc_package_product_product
    FOREIGN KEY (org_id,product_id) REFERENCES md_product(org_id,id)
    ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO proc_package_product(org_id,created_by,updated_by,package_id,product_id)
SELECT org_id,created_by,updated_by,id,product_id
FROM proc_package
WHERE product_id IS NOT NULL;
