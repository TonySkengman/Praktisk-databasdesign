package me.code.commands;

import me.code.models.Transaction;
import me.code.services.AuthService;
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
            transaction = transactionService.getTransactionById(transactionId);

            if (transaction == null) {
                System.out.println("No such transaction was found.");
                return;
            }

            if (!transaction.getUserId().equals(AuthService.getUserId())) {
                System.out.println("You can only delete your own transactions.");
                return;
            }

            transactionService.deleteTransactionById(transactionId);
            System.out.println("Deleted transaction with amount of: " + transaction.getFormattedAmount() + "kr.");

        } catch (Exception exception) {
            System.out.println("An error occurred, message: " + exception.getMessage());
        }
    }
}