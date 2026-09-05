package com.finance.ai.memory.repository;

import org.springframework.stereotype.Repository;
import com.finance.ai.memory.model.MemoryRecord;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class MemoryRepositoryImpl implements MemoryRepository {
    private final ConcurrentMap<String, MemoryRecord> store = new ConcurrentHashMap<>();

    @Override
    public void save(MemoryRecord record) {
        if (record == null || record.getId() == null) return;
        store.put(record.getId(), record);
    }

    @Override
    public Optional<MemoryRecord> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }
}
