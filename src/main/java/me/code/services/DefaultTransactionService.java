package me.code.services;

import me.code.models.Transaction;
import me.code.models.TransactionType;
import me.code.models.UserTransactionStats;
import me.code.repositories.ITransactionRepository;

import java.util.*;
import java.util.stream.Stream;

public class DefaultTransactionService implements ITransactionService {

    private final ITransactionRepository transactionRepository;

    public DefaultTransactionService(ITransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public void createTransaction(Transaction transaction) throws Exception {
        transactionRepository.save(transaction);
    }

    @Override
    public Transaction deleteTransactionById(UUID id) throws Exception {
        Transaction transaction = transactionRepository.findById(id);
        transactionRepository.delete(id);
        return transaction;
    }

    @Override
    public Transaction getTransactionById(UUID id) throws Exception {
        return transactionRepository.findById(id);
    }

    @Override
    public Stream<Transaction> getTransactions() throws Exception {
        UUID userId = AuthService.getUserId();
        if (userId == null) {
            return Stream.empty();
        }
        return transactionRepository.findByUserId(userId).stream();
    }

    @Override
    public double calculateBalance() throws Exception {
        UUID userId = AuthService.getUserId();
        if (userId == null) {
            return 0.0;
        }
        return transactionRepository.getBalanceForUser(userId);
    }

    @Override
    public String getCurrentUsername() throws Exception {
        UUID userId = AuthService.getUserId();
        if (userId == null) {
            return "Not logged in";
        }
        return transactionRepository.getUsernameById(userId);
    }

    @Override
    public UserTransactionStats getUserTransactionStats(UUID userId) throws Exception {
        return transactionRepository.getUserTransactionStats(userId);
    }
}