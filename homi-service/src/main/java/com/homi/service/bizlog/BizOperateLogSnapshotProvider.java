package com.homi.service.bizlog;

public interface BizOperateLogSnapshotProvider {
    Object getBeforeSnapshot(Object[] args);

    Object getAfterSnapshot(Object[] args, Object result);
}
