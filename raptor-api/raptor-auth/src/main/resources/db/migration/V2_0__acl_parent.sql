
-- clean up parent indexing as we use object identity now (no more direct inheritance based on type)

ALTER TABLE devices DROP FOREIGN KEY fk_devices_parent_id_devices_id;
ALTER TABLE devices DROP parent_id;

