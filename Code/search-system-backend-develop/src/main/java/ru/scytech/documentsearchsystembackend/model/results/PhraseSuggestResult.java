package ru.scytech.documentsearchsystembackend.model.results;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PhraseSuggestResult {
    private String text;
    private float score;
}
