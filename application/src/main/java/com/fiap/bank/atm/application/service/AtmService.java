package com.fiap.bank.atm.application.service;

import com.fiap.bank.atm.domain.exception.InvalidPinException;
import com.fiap.bank.atm.domain.model.Account;
import com.fiap.bank.atm.domain.model.Money;
import com.fiap.bank.atm.domain.repository.AccountRepository;

import java.util.List;
import java.util.stream.Collectors;

public class AtmService {
    private final AccountRepository accountRepository;
    private Account currentAccount;

    public AtmService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public AccountInfoDTO authenticate(String accountNumber, String pin) {
        Account account = accountRepository.findByAccountNumber(accountNumber);

        if (account == null) {
            throw new InvalidPinException("Conta não encontrada.");
        }

        try {
            account.authenticate(pin);
            currentAccount = account;
            return toAccountInfoDTO(currentAccount);
        } catch (RuntimeException e) {
            accountRepository.save(account); // Salva para persistir tentativas com erro ou estado bloqueado
            throw e;
        }
    }

    public void withdraw(double amount) {
        ensureAuthenticated();
        currentAccount.withdraw(Money.of(amount));
        accountRepository.save(currentAccount);
    }

    public void deposit(double amount) {
        ensureAuthenticated();
        currentAccount.deposit(Money.of(amount));
        accountRepository.save(currentAccount);
    }

    public void transfer(String targetAccountNumber, double amount) {
        ensureAuthenticated();

        Account targetAccount = accountRepository.findByAccountNumber(targetAccountNumber);
        if (targetAccount == null) {
            throw new IllegalArgumentException("Conta de destino não encontrada.");
        }
        currentAccount.transfer(targetAccount, Money.of(amount));

        accountRepository.save(currentAccount);
        accountRepository.save(targetAccount);
    }

    public AccountInfoDTO getAccountInfo() {
        ensureAuthenticated();
        return toAccountInfoDTO(currentAccount);
    }

    public List<TransactionDTO> getStatement() {
        ensureAuthenticated();
        return currentAccount.getTransactions().stream()
                .map(t -> new TransactionDTO(
                        t.getId() != null ? t.getId().toString() : null,
                        t.getType() != null ? t.getType().name() : null,
                        t.getAmount() != null ? t.getAmount().getAmount() : null,
                        t.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    public void logout() {
        currentAccount = null;
    }

    public AccountInfoDTO getCurrentAccount() {
        if (currentAccount == null) {
            return null;
        }
        return toAccountInfoDTO(currentAccount);
    }

    public boolean isAuthenticated() {
        return currentAccount != null;
    }

    private void ensureAuthenticated() {
        if (!isAuthenticated()) {
            throw new IllegalStateException("Nenhum usuário está autenticado no momento.");
        }
    }

    private AccountInfoDTO toAccountInfoDTO(Account account) {
        return new AccountInfoDTO(
                account.getId() != null ? account.getId().toString() : null,
                account.getAgency(),
                account.getNumber(),
                account.getBalance() != null ? account.getBalance().getAmount() : null,
                account.getStatus() != null ? account.getStatus().name() : null
        );
    }
}