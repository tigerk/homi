-- 修复 Office 文档 MIME Type 过长导致 file_type 写入失败的问题。
-- 例如 .docx 的 MIME Type:
-- application/vnd.openxmlformats-officedocument.wordprocessingml.document

ALTER TABLE `file_meta`
  MODIFY COLUMN `file_type` varchar(255) DEFAULT NULL COMMENT '文件 MIME Type';

ALTER TABLE `file_attach`
  MODIFY COLUMN `file_type` varchar(255) DEFAULT NULL COMMENT '文件 MIME Type';
