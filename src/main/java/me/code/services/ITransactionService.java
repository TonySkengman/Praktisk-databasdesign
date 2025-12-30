package me.code.services;

import me.code.models.Transaction;
import me.code.models.TransactionType;
import me.code.models.UserTransactionStats;

import java.util.UUID;
import java.util.stream.Stream;

public interface ITransactionService {

    void createTransaction(Transaction transaction) throws Exception;

    Transaction deleteTransactionById(UUID id) throws Exception;

    String getCurrentUsername() throws Exception;

    Transaction getTransactionById(UUID id) throws Exception;

    Stream<Transaction> getTransactions() throws Exception;

    double calculateBalance() throws Exception;

    UserTransactionStats getUserTransactionStats(UUID userId) throws Exception;
}