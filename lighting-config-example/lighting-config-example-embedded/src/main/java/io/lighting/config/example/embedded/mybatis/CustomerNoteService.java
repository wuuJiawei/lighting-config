package io.lighting.config.example.embedded.mybatis;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CustomerNoteService extends ServiceImpl<CustomerNoteMapper, CustomerNote> {

    public CustomerNote create(CustomerNoteRequest request) {
        CustomerNote note = new CustomerNote();
        note.setCustomer(request.getCustomer());
        note.setNote(request.getNote());
        note.setCreatedAt(LocalDateTime.now());
        save(note);
        return note;
    }

    public Optional<CustomerNote> update(Long id, CustomerNoteRequest request) {
        CustomerNote existing = getById(id);
        if (existing == null) {
            return Optional.empty();
        }
        existing.setCustomer(request.getCustomer());
        existing.setNote(request.getNote());
        updateById(existing);
        return Optional.of(existing);
    }
}
