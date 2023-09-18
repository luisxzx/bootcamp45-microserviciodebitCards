package com.example.demo.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "debit_cards")
public class DebitCardDocument {

    @Id
    private String id;

    private String clientId;
    private BigDecimal mount;
    private LocalDateTime issueDate;
    private String mainAccountId;
    private List<String> secondaryAccountIds;
}