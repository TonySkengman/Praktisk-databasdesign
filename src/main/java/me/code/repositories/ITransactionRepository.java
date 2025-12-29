package me.code.repositories;

import me.code.models.Transaction;

import java.util.List;
import java.util.UUID;

public interface ITransactionRepository {

    Transaction findById(UUID transactionId) throws Exception;

    List<Transaction> findAll() throws Exception;

    void save(Transaction transaction) throws Exception;

    void delete(UUID transactionId) throws Exception;
}
