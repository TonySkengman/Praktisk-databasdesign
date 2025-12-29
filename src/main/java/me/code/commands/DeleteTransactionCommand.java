package me.code.commands;

import me.code.models.Transaction;
import me.code.services.ITransactionService;
import me.code.utility.CommandHelper;

import java.util.UUID;

public class DeleteTransactionCommand extends Command {

    public DeleteTransactionCommand(ITransactionService transactionService) {
        super("delete-transaction", "Remove a transaction", transactionService);
    }

    @Override
    public void execute() {
        UUID transactionId = CommandHelper.queryTransactionId();
        if (transactionId == null) {
            return;
        }

        Transaction transaction;
        try {
            transaction = transactionService.deleteTransactionById(transactionId);
        } catch (Exception exception) {
            System.out.println("An error occurred, message: " + exception.getMessage());
            return;
        }

        if (transaction == null) {
            System.out.println("No such transaction was found.");
        } else {
            System.out.println("Deleted transaction with amount of: " + transaction.getFormattedAmount() + "kr.");
        }
    }
}
