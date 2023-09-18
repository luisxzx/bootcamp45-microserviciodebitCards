package com.example.demo.mapper;


import com.example.demo.document.DebitCardDocument;
import com.example.demo.model.DebitCard;

import java.time.ZoneOffset;

public class DebitCardMapper {

    public static DebitCard toModel(DebitCardDocument document) {
        if (document == null) {
            return null;
        }

        DebitCard debitCard = new DebitCard();
        debitCard.setId(document.getId());
        debitCard.setClientId(document.getClientId());
        debitCard.setIssueDate(document.getIssueDate().atOffset(ZoneOffset.UTC));
        debitCard.setMainAccountId(document.getMainAccountId());
        debitCard.setSecondaryAccountIds(document.getSecondaryAccountIds());

        return debitCard;
    }

    public static DebitCardDocument toDocument(DebitCard debitCard) {
        if (debitCard == null) {
            return null;
        }

        DebitCardDocument document = new DebitCardDocument();
        document.setId(debitCard.getId());
        document.setClientId(debitCard.getClientId());
        document.setIssueDate(debitCard.getIssueDate().toLocalDateTime());
        document.setMainAccountId(debitCard.getMainAccountId());
        document.setSecondaryAccountIds(debitCard.getSecondaryAccountIds());

        return document;
    }
}