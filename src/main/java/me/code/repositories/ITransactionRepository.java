package me.code.repositories;

import me.code.models.Transaction;
import me.code.models.UserTransactionStats;

import java.util.List;
import java.util.UUID;

public interface ITransactionRepository {

    Transaction findById(UUID transactionId) throws Exception;

    List<Transaction> findAll() throws Exception;

    List<Transaction> findByUserId(UUID userId) throws Exception;

    void save(Transaction transaction) throws Exception;

    void delete(UUID transactionId) throws Exception;

    UserTransactionStats getUserTransactionStats(UUID userId) throws Exception;

    List<String> getTransactionsWithUsername(UUID userId) throws Exception;

    double getBalanceForUser(UUID userId) throws Exception;

    String getUsernameById(UUID userId) throws Exception;
}