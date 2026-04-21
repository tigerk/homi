ALTER TABLE lease_room
  ADD COLUMN rent_price decimal(12,2) NULL COMMENT '房间租金' AFTER room_id;
