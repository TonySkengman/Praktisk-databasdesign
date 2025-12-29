package me.code.services;

import me.code.models.Transaction;
import me.code.models.TransactionType;

import java.util.UUID;
import java.util.stream.Stream;

public interface ITransactionService {

    void createTransaction(Transaction transaction) throws Exception;

    Transaction deleteTransactionById(UUID id) throws Exception;

    Transaction updateTypeById(UUID transactionID, TransactionType status) throws Exception;

    Transaction getTransactionById(UUID id) throws Exception;

    Stream<Transaction> getTransactions() throws Exception;

    double calculateBalance() throws Exception;

}
