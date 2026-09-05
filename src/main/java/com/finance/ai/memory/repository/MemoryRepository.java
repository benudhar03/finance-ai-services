package com.finance.ai.memory.repository;

import com.finance.ai.memory.model.MemoryRecord;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemoryRepository {

    void save(MemoryRecord record);
    Optional<MemoryRecord> findById(String id);
}
