-- Update existing sections from CLOSE to OPEN to allow bidding
UPDATE section SET status = 'OPEN' WHERE status = 'CLOSE';
