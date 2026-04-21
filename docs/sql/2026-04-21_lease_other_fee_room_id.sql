ALTER TABLE lease_other_fee
  ADD COLUMN room_id bigint NULL COMMENT '房间ID' AFTER lease_id;

CREATE INDEX idx_lease_other_fee_room_id ON lease_other_fee (room_id);
