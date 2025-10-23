-- Seed distributions with customer mappings
-- Note: Distribution IDs should match what the mock AWS clients return
INSERT INTO distribution (distribution_id, customer_id, bucket_id, status, version) VALUES
('dist-1', 'customer#1', 'bucket#1', 'ACTIVE', 0),
('dist-2', 'customer#2', 'bucket#2', 'ACTIVE', 0),
('dist-3', 'customer#1', 'bucket#3', 'ACTIVE', 0);
