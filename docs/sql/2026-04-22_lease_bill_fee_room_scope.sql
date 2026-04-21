ALTER TABLE lease_bill_fee
  ADD COLUMN room_id bigint NULL COMMENT '房间ID' AFTER bill_id;

CREATE INDEX idx_lease_bill_fee_room_id ON lease_bill_fee (room_id);

ALTER TABLE lease_other_fee
  MODIFY COLUMN room_id bigint NOT NULL COMMENT '房间ID';
