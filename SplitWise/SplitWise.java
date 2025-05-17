package SplitWise;

import java.util.*;

public class SplitWise {
    public static void main(String[] args) {
        SplitWiseService splitWiseService = SplitWiseService.getInstance();
        splitWiseService.setSettleExpenseStrategy(new DefaultSettleExpensesStrategy());

        User user1 = new User("Pranjal");
        User user2 = new User("Priyanshi");
        User user3 = new User("Messi");
        User user4 = new User("Lamine");

        splitWiseService.addUser(user1); splitWiseService.addUser(user2); 
        splitWiseService.addUser(user3); splitWiseService.addUser(user4);

        List<User> payeeList = new ArrayList<>();
        payeeList.add(user1); payeeList.add(user2); payeeList.add(user3); 
        Expense expense = new Expense(user1, payeeList, new Amount(100, Currency.RUPEE));
        splitWiseService.addExpense(expense);

        // splitWiseService.trackBalances();

        payeeList = new ArrayList<>();
        payeeList.add(user2); payeeList.add(user3); payeeList.add(user4); 
        expense = new Expense(user1, payeeList, new Amount(1, Currency.DOLLAR));
        splitWiseService.addExpense(expense);

        // splitWiseService.trackBalances();

        splitWiseService.settleExpenses();
    }
}


class SplitWiseService
{
    List<User> users;
    List<Expense> expenses;
    List<Transaction> transactions;
    ISettleExpensesStrategy setlleExpensesStrategy;
    static SplitWiseService serviceInstance;

    private SplitWiseService()
    {
        this.users = new ArrayList<>();
        this.expenses = new ArrayList<>();
        this.transactions = new ArrayList<>();
    }

    public static SplitWiseService getInstance()
    {
        if(SplitWiseService.serviceInstance==null) SplitWiseService.serviceInstance = new SplitWiseService();
        return SplitWiseService.serviceInstance;
    }

    public void addUser(User user)
    {
        this.users.add(user);
    }

    public void addExpense(Expense expense)
    {
        this.expenses.add(expense);
        computeExpense(expense);
    }

    private void computeExpense(Expense expense)
    {
        Amount expenseAmount = expense.amount;

        User payer = expense.paidBy;
        payer.balance -= expenseAmount.convertCurrency(Currency.RUPEE).amount; //decrease payer balance

        List<User> paidFor = expense.paidFor;
        Amount splitAmount = new Amount(expenseAmount.amount/paidFor.size(), expenseAmount.currency);
        for(User user:paidFor)
        {
            user.balance += splitAmount.convertCurrency(Currency.RUPEE).amount;
        }
    }

    public void settleExpenses()
    {
        List<Transaction> transactionsForSettlement = setlleExpensesStrategy.settleExpenses(users);
        for(Transaction transaction:transactionsForSettlement)
        {
            transaction.completeTransaction();
            transactions.add(transaction);
        }
    }

    public void setSettleExpenseStrategy(ISettleExpensesStrategy strategy)
    {
        this.setlleExpensesStrategy = strategy;
    }

    public void trackBalances()
    {
        for(User user:users) 
        {
            System.out.println("User " + user.name + " has balance " + user.balance);
        }
    }
}

interface ISettleExpensesStrategy
{
    public List<Transaction> settleExpenses(List<User> users);
}

class DefaultSettleExpensesStrategy implements ISettleExpensesStrategy
{
    public List<Transaction> settleExpenses(List<User> users)
    {
        List<Transaction> transactions = new ArrayList<>();
        Collections.sort(users, (a,b)->Double.compare(b.balance, a.balance));
        List<Double> balances = new ArrayList<>();
        for(User user:users) balances.add(user.balance);
        for(int i = 0;i<users.size()-1;i++)
        {
            Transaction transaction = new Transaction(users.get(i), users.get(i+1), balances.get(i));
            transactions.add(transaction);
            balances.set(i+1, balances.get(i) + balances.get(i+1));
        }
        return transactions;
    }
}

class User 
{
    String id;
    String name;
    double balance;

    public User(String name)
    {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.balance = 0;
    }
}

class Expense
{
    User paidBy;
    List<User> paidFor;
    Amount amount;

    public Expense(User paidBy, List<User> paidFor, Amount amount)
    {
        this.paidBy = paidBy;
        this.paidFor = paidFor;
        this.amount = amount;
    }

}

class Transaction
{
    User payer;
    User payee;
    double amount;
    TranscationStatus status;

    public Transaction(User payer, User payee, double amount)
    {
        this.payer = payer;
        this.payee = payee;
        this.amount = amount;
        this.status = TranscationStatus.PENDING;
    }

    public void completeTransaction()
    {
        System.out.println("User " + payer.name + " paid " + payee.name + " amount " + amount);
        payer.balance-=amount;
        payee.balance+=amount;
        this.status = TranscationStatus.COMPLETED;
    }
}

class Amount 
{
    double amount;
    Currency currency;

    public Amount(double amount, Currency currency)
    {
        this.amount = amount;
        this.currency = currency;
    }

    public Amount convertCurrency(Currency newCurrency)
    {
        double baseAmount = amount * this.currency.getConversion();
        double newAmount = baseAmount/newCurrency.getConversion();
        return new Amount(newAmount, currency);
    }

}


enum Currency 
{
    RUPEE(1),
    DOLLAR(85),
    POUND(100);

    int conversion;

    private Currency(int conversion)
    {
        this.conversion = conversion;
    }

    public int getConversion()
    {
        return this.conversion;
    }
}

enum TranscationStatus 
{
    PENDING,
    COMPLETED
}

