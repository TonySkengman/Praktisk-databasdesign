package me.code.services;

import me.code.models.Transaction;
import me.code.models.TransactionType;
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
    public Transaction updateTypeById(UUID transactionId, TransactionType type) throws Exception {
        Transaction transaction = getTransactionById(transactionId);
        if (transaction == null) {
            return null;
        } else {
            transaction.setTransactionType(type);      // Uppdatera status
            transactionRepository.save(transaction);   // Spara tillbaka
            return transaction;
        }
    }

    @Override
    public Transaction getTransactionById(UUID id) throws Exception {
        return transactionRepository.findById(id);
    }

    @Override
    public Stream<Transaction> getTransactions() throws Exception {
        return transactionRepository.findAll().stream();
    }

    @Override
    public double calculateBalance() throws Exception {
        return transactionRepository.findAll().stream()
                .mapToDouble(Transaction::getAmount)
                .sum();
    }
}