package ru.scytech.documentsearchsystembackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaggedSubstring {
    int start;
    int end;
    String substring;
}
