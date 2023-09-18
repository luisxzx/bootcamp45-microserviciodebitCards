package com.example.demo.repository;



import com.example.demo.document.DebitCardDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface DebitCardRepository extends ReactiveMongoRepository<DebitCardDocument, String> {

}
