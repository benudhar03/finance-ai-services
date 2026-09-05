package com.finance.ai.memory.service;

import org.springframework.stereotype.Service;
import com.finance.ai.memory.model.MemoryRecord;
import com.finance.ai.memory.repository.MemoryRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemoryService {

    private final MemoryRepository repository;

    public void save(MemoryRecord record) { repository.save(record); }
    public Optional<MemoryRecord> find(String id) { return repository.findById(id); }
}
