package io.lighting.config.example.embedded.mybatis;

import jakarta.validation.constraints.NotBlank;

public class CustomerNoteRequest {

    @NotBlank
    private String customer;

    @NotBlank
    private String note;

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
