package ru.scytech.documentsearchsystembackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Highlight {
    private String highlightString;
    private List<TaggedSubstring> taggedSubstrings;
}
