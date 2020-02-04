import java.util.Scanner;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.InputMismatchException;

/**
 * Client class
 *
 * @author Kerly Titus
 */

public class Client implements Runnable {

  private static int maxNbTransactions;         /* Maximum number of transactions */
  private static Transaction[] transactions;   /* Transactions to be processed */
  private static Network network;               /* Client object to handle network operations */
  private String clientOperation;               /* sending or receiving */

  /**
   * Constructor method of Client class
   *
   * @param
   * @return
   */
  public Client(String operation) {
    if (operation.equals("sending")) {
      // Initialize Client Sending
      System.out.println("\nInitializing client sending application ...");
      clientOperation = operation;
      // Get transactions
      System.out.println("\nInitializing the transactions ... ");
      maxNbTransactions = 100;
      transactions = new Transaction[maxNbTransactions];
      readTransactions();
      // Connect to Network
      System.out.println("\nConnecting client to network ...");
      network = new Network("client");
      String cip = network.getClientIP();
      if (!(network.connect(cip))) {
        System.out.println("\nTerminating client application, network unavailable");
        System.exit(0);
      }
    } else if (operation.equals("receiving")) {
      System.out.println("\nInitializing client receiving application ...");
      clientOperation = operation;
    }
  }

  /**
   * Accessor method of Client class
   *
   * @param
   * @return numberOfTransactions
   */
  public int getNumberOfTransactions() {
    return transactions.length;
  }

  /**
   * Accessor method of Client class
   *
   * @param
   * @return clientOperation
   */
  public String getClientOperation() {
    return clientOperation;
  }

  /**
   * Mutator method of Client class
   *
   * @param operation
   * @return
   */
  public void setClientOperation(String operation) {
    clientOperation = operation;
  }

  /**
   * Reading of the transactions from an input file
   *
   * @param
   * @return
   */
  public void readTransactions() {
    Scanner inputStream = null;     /* Transactions input file stream */
    int i = 0;                      /* Index of transactions array */

    try {
      inputStream = new Scanner(new FileInputStream("src/Data/transaction.txt"));
    } catch (FileNotFoundException e) {
      System.out.println("File transaction.txt was not found");
      System.out.println("or could not be opened.");
      System.exit(0);
    }
    while (inputStream.hasNextLine()) {
      try {
        transactions[i] = new Transaction();
        transactions[i].setAccountNumber(inputStream.next());            /* Read account number */
        transactions[i].setOperationType(inputStream.next());            /* Read transaction type */
        transactions[i].setTransactionAmount(inputStream.nextDouble());  /* Read transaction amount */
        transactions[i].setTransactionStatus("pending");                 /* Set current transaction status */
        i++;
      } catch (InputMismatchException e) {
        System.out.println("Line " + i + "file transactions.txt invalid input");
        System.exit(0);
      }
    }

    // System.out.println("\nDEBUG : Client.readTransactions() - " + getNumberOfTransactions() + " transactions processed");

    inputStream.close();

  }

  /**
   * Sending the transactions to the server
   *
   * @param
   * @return
   */
  public void sendTransactions() {
    int i = 0;     /* index of transaction array */

    while (i < getNumberOfTransactions()) {
      // while( network.getInBufferStatus().equals("full") );     /* Alternatively, busy-wait until the network input buffer is available */

      transactions[i].setTransactionStatus("sent");   /* Set current transaction status */

      // System.out.println("\nDEBUG : Client.sendTransactions() - sending transaction on account " + transaction[i].getAccountNumber());

      network.send(transactions[i]);    /* Transmit current transaction */
      i++;
    }

  }

  /**
   * Receiving the completed transactions from the server
   *
   * @param transact
   * @return
   */
  public void receiveTransactions(Transaction transact) {
    int i = 0;     /* Index of transaction array */

    while (i < getNumberOfTransactions()) {
      // while( network.getOutBufferStatus().equals("empty"));  	/* Alternatively, busy-wait until the network output buffer is available */

      network.receive(transact);                                /* Receive updated transaction from the network buffer */

      // System.out.println("\nDEBUG : Client.receiveTransactions() - receiving updated transaction on account " + transact.getAccountNumber());

      System.out.println(transact);                                /* Display updated transaction */
      i++;
    }
  }

  /**
   * Create a String representation based on the Client Object
   *
   * @param
   * @return String representation
   */
  public String toString() {
    return ("client IP " + network.getClientIP() + " Connection status" + network.getClientConnectionStatus() + "Number of transactions " + getNumberOfTransactions());
  }

  // Helper Method For Run Method
  private void sending() {
    for (int i = 0; i < getNumberOfTransactions(); i++) {
      while (network.getInBufferStatus().equals("full")) {
        Thread.yield();
      }
      if (transactions[i] != null) {
        network.send(transactions[i]);
      }
    }
    network.disconnect(network.getClientIP());
  }

  // Helper Method For Run Method
  private void receiving() {
    for (int i = 0; i < getNumberOfTransactions() - 1; i++) {
      while (network.getOutBufferStatus().equals("empty")) {
        Thread.yield();
      }
      if (transactions[i] != null) {
        network.receive(transactions[i]);
         System.out.println(transactions[i].toString());
      }
    }
    network.disconnect(network.getClientIP());
  }

  /**
   * Code for the run method
   *
   * @param
   * @return
   */
  public void run() {
    long time = System.currentTimeMillis();

    if (getClientOperation().equals("sending")) {
      sending();
    } else if (getClientOperation().equals("receiving")) {
      receiving();
    } else {
      System.out.println("ERROR: Wrong client operation type!");
      System.exit(1);
    }

    System.out.println("\nTerminating " + clientOperation + " client thread - " + " Running time " + (System.currentTimeMillis() - time) + " ms");
  }
}
