import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class AccountServiceTransferFundsTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    private Account fromAccount;
    private Account toAccount;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        fromAccount = new Account("12345", 100.0);
        toAccount = new Account("67890", 50.0);
    }

    @Test
    public void testTransferFundsSuccess() {
        when(accountRepository.findById(any())).thenReturn(fromAccount, toAccount);

        boolean result = accountService.transferFunds("12345", "67890", 20.0);

        assertTrue(result);
        assertEquals(80.0, fromAccount.getBalance());
        assertEquals(70.0, toAccount.getBalance());
    }

    @Test
    public void testTransferFundsInsufficientBalance() {
        when(accountRepository.findById(any())).thenReturn(fromAccount, toAccount);

        boolean result = accountService.transferFunds("12345", "67890", 150.0);

        assertFalse(result);
        assertEquals(100.0, fromAccount.getBalance());
        assertEquals(50.0, toAccount.getBalance());
    }

    @Test
    public void testTransferFundsNullFromAccount() {
        when(accountRepository.findById(any())).thenReturn(null, toAccount);

        boolean result = accountService.transferFunds("12345", "67890", 20.0);

        assertFalse(result);
        assertEquals(50.0, toAccount.getBalance());
    }

    @Test
    public void testTransferFundsNullToAccount() {
        when(accountRepository.findById(any())).thenReturn(fromAccount, null);

        boolean result = accountService.transferFunds("12345", "67890", 20.0);

        assertFalse(result);
        assertEquals(100.0, fromAccount.getBalance());
    }
}