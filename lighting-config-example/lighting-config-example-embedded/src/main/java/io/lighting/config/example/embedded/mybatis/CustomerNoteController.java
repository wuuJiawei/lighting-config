package io.lighting.config.example.embedded.mybatis;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/examples/mybatis/notes")
public class CustomerNoteController {

    private final CustomerNoteService service;

    public CustomerNoteController(CustomerNoteService service) {
        this.service = service;
    }

    @GetMapping
    public List<CustomerNote> list() {
        return service.list();
    }

    @PostMapping
    public ResponseEntity<CustomerNote> create(@Valid @RequestBody CustomerNoteRequest request) {
        CustomerNote note = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(note);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerNote> update(@PathVariable Long id,
                                               @Valid @RequestBody CustomerNoteRequest request) {
        return service.update(id, request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!service.removeById(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
