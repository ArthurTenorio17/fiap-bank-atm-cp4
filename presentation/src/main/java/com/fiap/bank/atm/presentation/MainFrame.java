package br.com.fiap.bank.view;

import br.com.fiap.bank.dto.AccountInfoDTO;
import br.com.fiap.bank.enums.ATMState;
import br.com.fiap.bank.exception.ATMException;
import br.com.fiap.bank.service.AtmService;

import javax.swing.*;
import java.awt.event.ActionListener;

public class MainFrame extends javax.swing.JFrame {

    private final AtmService atmService;
    private ATMState currentState;
    private StringBuilder inputBuffer;
    private String targetAccountNumber;
    private String errorMessage;

    public MainFrame(AtmService atmService) {
        this.atmService = atmService;
        this.inputBuffer = new StringBuilder();
        this.currentState = ATMState.INSERT_CARD;

        initComponents();
        initCustomListeners();
        updateUIState();
    }

    private void initCustomListeners() {
        ActionListener numListener = e -> {
            JButton btn = (JButton) e.getSource();
            appendInput(btn.getText());
        };

        btn0.addActionListener(numListener);
        btn1.addActionListener(numListener);
        btn2.addActionListener(numListener);
        btn3.addActionListener(numListener);
        btn4.addActionListener(numListener);
        btn5.addActionListener(numListener);
        btn6.addActionListener(numListener);
        btn7.addActionListener(numListener);
        btn8.addActionListener(numListener);
        btn9.addActionListener(numListener);

        btnC.addActionListener(e -> handleClearOrCancel());
        btnBlank.addActionListener(e -> handleConfirmButton());

        btnLeft1.addActionListener(e -> handleLeftOption(1));
        btnLeft2.addActionListener(e -> handleLeftOption(2));
        btnLeft3.addActionListener(e -> handleLeftOption(3));

        btnRight1.addActionListener(e -> handleRightOption(1));
        btnRight2.addActionListener(e -> handleRightOption(2));
        btnRight3.addActionListener(e -> handleRightOption(3));
    }

    private void appendInput(String digit) {
        if (currentState == ATMState.INSERT_CARD ||
            currentState == ATMState.ENTER_PIN ||
            currentState == ATMState.WITHDRAW_CUSTOM ||
            currentState == ATMState.DEPOSIT_INPUT ||
            currentState == ATMState.TRANSFER_ACCOUNT ||
            currentState == ATMState.TRANSFER_VALUE) {

            if (inputBuffer.length() < 12) {
                inputBuffer.append(digit);
                updateUIState();
            }
        }
    }

    private void handleClearOrCancel() {
        if (inputBuffer.length() > 0) {
            inputBuffer.deleteCharAt(inputBuffer.length() - 1);
            updateUIState();
        } else {
            if (currentState != ATMState.INSERT_CARD) {
                atmService.logout();
                changeState(ATMState.INSERT_CARD);
            }
        }
    }

    private void handleConfirmButton() {
        try {
            switch (currentState) {
                case INSERT_CARD:
                    if (inputBuffer.length() > 0) {
                        String accNum = inputBuffer.toString();
                        inputBuffer.setLength(0);
                        atmService.selectAccount(accNum);
                        changeState(ATMState.ENTER_PIN);
                    }
                    break;

                case ENTER_PIN:
                    if (inputBuffer.length() > 0) {
                        String pin = inputBuffer.toString();
                        inputBuffer.setLength(0);
                        atmService.authenticate(pin);
                        changeState(ATMState.MAIN_MENU);
                    }
                    break;

                case WITHDRAW_CUSTOM:
                    if (inputBuffer.length() > 0) {
                        double amount = Double.parseDouble(inputBuffer.toString());
                        inputBuffer.setLength(0);
                        atmService.withdraw(amount);
                        changeState(ATMState.ANIMATION_CASH);
                    }
                    break;

                case DEPOSIT_INPUT:
                    if (inputBuffer.length() > 0) {
                        double amount = Double.parseDouble(inputBuffer.toString());
                        inputBuffer.setLength(0);
                        atmService.deposit(amount);
                        changeState(ATMState.ANIMATION_DEPOSIT);
                    }
                    break;

                case TRANSFER_ACCOUNT:
                    if (inputBuffer.length() > 0) {
                        targetAccountNumber = inputBuffer.toString();
                        inputBuffer.setLength(0);
                        changeState(ATMState.TRANSFER_VALUE);
                    }
                    break;

                case TRANSFER_VALUE:
                    if (inputBuffer.length() > 0 && targetAccountNumber != null) {
                        double amount = Double.parseDouble(inputBuffer.toString());
                        inputBuffer.setLength(0);
                        atmService.transfer(targetAccountNumber, amount);
                        changeState(ATMState.SUCCESS);
                    }
                    break;

                default:
                    break;
            }
        } catch (ATMException ex) {
            errorMessage = ex.getMessage();
            changeState(ATMState.ERROR);
        }
    }

    private void handleLeftOption(int option) {
        switch (currentState) {
            case MAIN_MENU:
                if (option == 1) changeState(ATMState.WITHDRAW_SELECT);
                else if (option == 2) changeState(ATMState.DEPOSIT_INPUT);
                else if (option == 3) changeState(ATMState.TRANSFER_ACCOUNT);
                break;

            case WITHDRAW_SELECT:
                if (option == 1) processWithdraw(20.0);
                else if (option == 2) processWithdraw(50.0);
                else if (option == 3) processWithdraw(100.0);
                break;

            default:
                break;
        }
    }

    private void handleRightOption(int option) {
        switch (currentState) {
            case MAIN_MENU:
                if (option == 1) changeState(ATMState.SHOW_BALANCE);
                else if (option == 2) changeState(ATMState.SHOW_STATEMENT);
                else if (option == 3) {
                    atmService.logout();
                    changeState(ATMState.INSERT_CARD);
                }
                break;

            case WITHDRAW_SELECT:
                if (option == 1) processWithdraw(200.0);
                else if (option == 2) processWithdraw(500.0);
                else if (option == 3) changeState(ATMState.WITHDRAW_CUSTOM);
                break;

            case SHOW_BALANCE:
            case SHOW_STATEMENT:
                if (option == 3) changeState(ATMState.MAIN_MENU);
                break;

            default:
                break;
        }
    }

    private void processWithdraw(double amount) {
        try {
            atmService.withdraw(amount);
            changeState(ATMState.ANIMATION_CASH);
        } catch (ATMException ex) {
            errorMessage = ex.getMessage();
            changeState(ATMState.ERROR);
        }
    }

    private void changeState(ATMState newState) {
        this.currentState = newState;
        this.inputBuffer.setLength(0);
        updateUIState();
    }

    private void updateUIState() {
        lblLeftOpt1.setText(" ");
        lblLeftOpt2.setText(" ");
        lblLeftOpt3.setText(" ");
        lblRightOpt1.setText(" ");
        lblRightOpt2.setText(" ");
        lblRightOpt3.setText(" ");
        lblScreenMessage.setText(" ");

        switch (currentState) {
            case INSERT_CARD:
                lblScreenHeader.setText("--- ATM FIAP BANK ---");
                lblScreenStatus.setText("INSIRA SEU CARTÃO OU CONTA");
                lblScreenInput.setText(inputBuffer.length() > 0 ? inputBuffer.toString() : "_");
                lblScreenMessage.setText("Digite o número da conta e clique em 'Confirmar'.");
                btnBlank.setText("Confirmar");
                break;

            case ENTER_PIN:
                lblScreenHeader.setText("--- ATM FIAP BANK ---");
                lblScreenStatus.setText("INSIRA A SENHA DE 4 DÍGITOS");

                StringBuilder stars = new StringBuilder();
                for (int i = 0; i < inputBuffer.length(); i++) {
                    stars.append("*");
                }
                lblScreenInput.setText(stars.length() > 0 ? stars.toString() : "[SENHA]");
                lblScreenMessage.setText("Acesso de Segurança. Pressione 'Confirmar' ao finalizar.");
                btnBlank.setText("Confirmar");
                break;

            case MAIN_MENU:
                AccountInfoDTO currentAcc = atmService.getCurrentAccount();
                lblScreenHeader.setText("--- MENU PRINCIPAL ---");
                lblScreenStatus.setText("CONTA ATIVA: " + (currentAcc != null ? currentAcc.accountNumber() : ""));
                lblScreenInput.setText("SELECIONE A OPERAÇÃO");

                lblLeftOpt1.setText("> SACAR");
                lblLeftOpt2.setText("> DEPOSITAR");
                lblLeftOpt3.setText("> TRANSFERIR");

                lblRightOpt1.setText("SALDO <");
                lblRightOpt2.setText("EXTRATO <");
                lblRightOpt3.setText("SAIR <");
                btnBlank.setText("");
                break;

            case WITHDRAW_SELECT:
                lblScreenHeader.setText("--- REALIZAR SAQUE ---");
                lblScreenStatus.setText("ESCOLHA O VALOR DO SAQUE");
                lblScreenInput.setText("");

                lblLeftOpt1.setText("> R$ 20,00");
                lblLeftOpt2.setText("> R$ 50,00");
                lblLeftOpt3.setText("> R$ 100,00");

                lblRightOpt1.setText("R$ 200,00 <");
                lblRightOpt2.setText("R$ 500,00 <");
                lblRightOpt3.setText("OUTRO VALOR <");
                btnBlank.setText("");
                lblScreenMessage.setText("Pressione 'C' no teclado para voltar ao Menu.");
                break;

            case WITHDRAW_CUSTOM:
                lblScreenHeader.setText("--- VALOR PERSONALIZADO ---");
                lblScreenStatus.setText("DIGITE O VALOR PARA SAQUE");
                lblScreenInput.setText(inputBuffer.length() > 0 ? "R$ " + inputBuffer.toString() + ",00" : "R$ 0,00");
                lblScreenMessage.setText("Pressione 'Confirmar' para realizar o saque.");
                btnBlank.setText("Confirmar");
                break;

            case DEPOSIT_INPUT:
                lblScreenHeader.setText("--- REALIZAR DEPÓSITO ---");
                lblScreenStatus.setText("INSIRA O VALOR DE DEPÓSITO");
                lblScreenInput.setText(inputBuffer.length() > 0 ? "R$ " + inputBuffer.toString() + ",00" : "R$ 0,00");
                lblScreenMessage.setText("Digite e clique em 'Confirmar' para validar.");
                btnBlank.setText("Confirmar");
                break;

            case TRANSFER_ACCOUNT:
                lblScreenHeader.setText("--- TRANSFERÊNCIA BANCÁRIA ---");
                lblScreenStatus.setText("DIGITE A CONTA DE DESTINO");
                lblScreenInput.setText(inputBuffer.length() > 0 ? inputBuffer.toString() + "_" : "[CONTA DESTINO]_");
                lblScreenMessage.setText("Digite o número e clique 'Confirmar'.");
                btnBlank.setText("Confirmar");
                break;

            case TRANSFER_VALUE:
                lblScreenHeader.setText("--- VALOR DA TRANSFERÊNCIA ---");
                lblScreenStatus.setText("DESTINO: CONTA " + targetAccountNumber);
                lblScreenInput.setText(inputBuffer.length() > 0 ? "R$ " + inputBuffer.toString() + ",00" : "R$ 0,00");
                lblScreenMessage.setText("Pressione 'Confirmar' para efetuar.");
                btnBlank.setText("Confirmar");
                break;

            case SHOW_BALANCE:
                AccountInfoDTO balanceAcc = atmService.getCurrentAccount();
                lblScreenHeader.setText("--- CONSULTA DE SALDO ---");
                lblScreenStatus.setText("SALDO DISPONÍVEL");
                lblScreenInput.setText(balanceAcc != null ? String.format("R$ %.2f", balanceAcc.balance()) : "R$ 0,00");
                lblScreenMessage.setText("Limite Diário Restante: " +
                        (balanceAcc != null
                                ? String.format("R$ %.2f", balanceAcc.remainingDailyLimit())
                                : "R$ 0,00"));
                lblRightOpt3.setText("VOLTAR <");
                btnBlank.setText("");
                break;

            case SHOW_STATEMENT:
                lblScreenHeader.setText("--- EXTRATO IMPRESSO ---");
                lblScreenStatus.setText("EXTRATO GERADO");
                lblScreenInput.setText("VERIFIQUE A LATERAL");
                lblScreenMessage.setText("O extrato físico foi impresso na impressora lateral.");
                lblRightOpt3.setText("VOLTAR <");
                btnBlank.setText("");
                break;

            case ANIMATION_CASH:
                lblScreenHeader.setText("--- AGUARDE ---");
                lblScreenStatus.setText("CONSTANDO CÉDULAS...");
                lblScreenInput.setText("$$$$$$$$$$$$$");
                lblScreenMessage.setText("Retire as cédulas no dispensador abaixo.");
                btnBlank.setText("");
                break;

            case ANIMATION_PRINT:
                lblScreenHeader.setText("--- AGUARDE ---");
                lblScreenStatus.setText("IMPRIMINDO COMPROVANTE...");
                lblScreenInput.setText("■■■■■■■■■■■■■");
                lblScreenMessage.setText("Retire o papel térmico impresso ao lado.");
                btnBlank.setText("");
                break;

            case ANIMATION_DEPOSIT:
                lblScreenHeader.setText("--- AGUARDE ---");
                lblScreenStatus.setText("PROCESSANDO DEPÓSITO...");
                lblScreenInput.setText("•••••••••••••");
                lblScreenMessage.setText("Processando envelopes e autenticação...");
                btnBlank.setText("");
                break;

            case SUCCESS:
                lblScreenHeader.setText("--- OPERAÇÃO CONCLUÍDA ---");
                lblScreenStatus.setText("TRANSAÇÃO COM SUCESSO!");
                lblScreenInput.setText("OBRIGADO");
                lblScreenMessage.setText("Retornando em instantes...");
                btnBlank.setText("");
                break;

            case ERROR:
                lblScreenHeader.setText("--- ATENÇÃO ---");
                lblScreenStatus.setText("FALHA NA OPERAÇÃO");
                lblScreenInput.setText("ERRO");
                lblScreenMessage.setText(errorMessage != null ? errorMessage : "TENTE NOVAMENTE.");
                btnBlank.setText("");
                break;
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanelMain = new javax.swing.JPanel();
        jPanelHeader = new javax.swing.JPanel();
        lblHeaderTitle = new javax.swing.JLabel();
        jPanelCenterConsole = new javax.swing.JPanel();
        jPanelLeftButtons = new javax.swing.JPanel();
        btnLeft1 = new javax.swing.JButton();
        btnLeft2 = new javax.swing.JButton();
        btnLeft3 = new javax.swing.JButton();
        jPanelRightButtons = new javax.swing.JPanel();
        btnRight1 = new javax.swing.JButton();
        btnRight2 = new javax.swing.JButton();
        btnRight3 = new javax.swing.JButton();
        jPanelScreen = new javax.swing.JPanel();
        jPanelScreenHeader = new javax.swing.JPanel();
        lblScreenHeader = new javax.swing.JLabel();
        jPanelScreenCenter = new javax.swing.JPanel();
        lblScreenStatus = new javax.swing.JLabel();
        lblScreenInput = new javax.swing.JLabel();
        lblScreenMessage = new javax.swing.JLabel();
        jPanelScreenLeftLabels = new javax.swing.JPanel();
        lblLeftOpt1 = new javax.swing.JLabel();
        lblLeftOpt2 = new javax.swing.JLabel();
        lblLeftOpt3 = new javax.swing.JLabel();
        jPanelScreenRightLabels = new javax.swing.JPanel();
        lblRightOpt1 = new javax.swing.JLabel();
        lblRightOpt2 = new javax.swing.JLabel();
        lblRightOpt3 = new javax.swing.JLabel();
        jPanelBottomConsole = new javax.swing.JPanel();
        jPanelKeypad = new javax.swing.JPanel();
        btn1 = new javax.swing.JButton();
        btn2 = new javax.swing.JButton();
        btn3 = new javax.swing.JButton();
        btn4 = new javax.swing.JButton();
        btn5 = new javax.swing.JButton();
        btn6 = new javax.swing.JButton();
        btn7 = new javax.swing.JButton();
        btn8 = new javax.swing.JButton();
        btn9 = new javax.swing.JButton();
        btnBlank = new javax.swing.JButton();
        btn0 = new javax.swing.JButton();
        btnC = new javax.swing.JButton();
        jPanelPeripherals = new javax.swing.JPanel();
        cardSlotContainer = new javax.swing.JPanel();
        lblCardIndicatorLed = new javax.swing.JLabel();
        receiptPrinterContainer = new javax.swing.JPanel();
        lblPrinterStatus = new javax.swing.JLabel();
        cashDispenserContainer = new javax.swing.JPanel();
        lblCashDispenserStatus = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("FIAP Bank - Caixa Eletrônico (ATM)");
        setResizable(false);

        jPanelMain.setBackground(new java.awt.Color(19, 30, 43));
        jPanelMain.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 51, 51), 8));
        jPanelMain.setLayout(new java.awt.BorderLayout());

        jPanelHeader.setBackground(new java.awt.Color(13, 20, 31));
        jPanelHeader.setPreferredSize(new java.awt.Dimension(800, 80));
        jPanelHeader.setLayout(new java.awt.GridBagLayout());

        lblHeaderTitle.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        lblHeaderTitle.setForeground(new java.awt.Color(241, 248, 252));
        lblHeaderTitle.setText("FIAP BANK");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = -1;
        gridBagConstraints.gridy = -1;
        jPanelHeader.add(lblHeaderTitle, gridBagConstraints);

        jPanelMain.add(jPanelHeader, java.awt.BorderLayout.NORTH);

        jPanelCenterConsole.setBackground(new java.awt.Color(19, 30, 43));
        jPanelCenterConsole.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 40, 20, 40));
        jPanelCenterConsole.setLayout(new java.awt.BorderLayout());

        jPanelLeftButtons.setBackground(new java.awt.Color(19, 30, 43));
        jPanelLeftButtons.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 0, 20, 15));
        jPanelLeftButtons.setPreferredSize(new java.awt.Dimension(100, 300));
        jPanelLeftButtons.setLayout(new java.awt.GridLayout(3, 1, 0, 35));

        btnLeft1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnLeft1.setText("[ ]");
        jPanelLeftButtons.add(btnLeft1);

        btnLeft2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnLeft2.setText("[ ]");
        jPanelLeftButtons.add(btnLeft2);

        btnLeft3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnLeft3.setText("[ ]");
        jPanelLeftButtons.add(btnLeft3);

        jPanelCenterConsole.add(jPanelLeftButtons, java.awt.BorderLayout.WEST);

        jPanelRightButtons.setBackground(new java.awt.Color(19, 30, 43));
        jPanelRightButtons.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 15, 0, 0));
        jPanelRightButtons.setPreferredSize(new java.awt.Dimension(100, 300));
        jPanelRightButtons.setLayout(new java.awt.GridLayout(3, 1, 0, 35));

        btnRight1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnRight1.setText("[ ]");
        jPanelRightButtons.add(btnRight1);

        btnRight2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnRight2.setText("[ ]");
        jPanelRightButtons.add(btnRight2);

        btnRight3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnRight3.setText("[ ]");
        jPanelRightButtons.add(btnRight3);

        jPanelCenterConsole.add(jPanelRightButtons, java.awt.BorderLayout.EAST);

        jPanelScreen.setBackground(new java.awt.Color(11, 18, 28));
        jPanelScreen.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(43, 56, 77), 4, true));
        jPanelScreen.setLayout(new java.awt.BorderLayout());

        jPanelScreenHeader.setBackground(new java.awt.Color(11, 18, 28));
        jPanelScreenHeader.setPreferredSize(new java.awt.Dimension(500, 45));

        lblScreenHeader.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        lblScreenHeader.setForeground(new java.awt.Color(254, 240, 138));
        lblScreenHeader.setText("--- ATM FIAP BANK ---");
        jPanelScreenHeader.add(lblScreenHeader);

        jPanelScreen.add(jPanelScreenHeader, java.awt.BorderLayout.NORTH);

        jPanelScreenCenter.setBackground(new java.awt.Color(11, 18, 28));
        jPanelScreenCenter.setLayout(new java.awt.GridLayout(3, 1));

        lblScreenStatus.setFont(new java.awt.Font("Monospaced", 1, 14)); // NOI18N
        lblScreenStatus.setForeground(new java.awt.Color(241, 245, 249));
        lblScreenStatus.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblScreenStatus.setText("INSIRA SEU CARTÃO OU CONTA");
        jPanelScreenCenter.add(lblScreenStatus);

        lblScreenInput.setFont(new java.awt.Font("Monospaced", 1, 24)); // NOI18N
        lblScreenInput.setForeground(new java.awt.Color(56, 189, 248));
        lblScreenInput.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblScreenInput.setText("_");
        jPanelScreenCenter.add(lblScreenInput);

        lblScreenMessage.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        lblScreenMessage.setForeground(new java.awt.Color(234, 113, 113));
        lblScreenMessage.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblScreenMessage.setText(" ");
        jPanelScreenCenter.add(lblScreenMessage);

        jPanelScreen.add(jPanelScreenCenter, java.awt.BorderLayout.CENTER);

        jPanelScreenLeftLabels.setBackground(new java.awt.Color(11, 18, 28));
        jPanelScreenLeftLabels.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 10, 20, 0));
        jPanelScreenLeftLabels.setPreferredSize(new java.awt.Dimension(130, 200));
        jPanelScreenLeftLabels.setLayout(new java.awt.GridLayout(3, 1, 0, 35));

        lblLeftOpt1.setFont(new java.awt.Font("Monospaced", 1, 14)); // NOI18N
        lblLeftOpt1.setForeground(new java.awt.Color(56, 189, 248));
        lblLeftOpt1.setText(" ");
        jPanelScreenLeftLabels.add(lblLeftOpt1);

        lblLeftOpt2.setFont(new java.awt.Font("Monospaced", 1, 14)); // NOI18N
        lblLeftOpt2.setForeground(new java.awt.Color(56, 189, 248));
        lblLeftOpt2.setText(" ");
        jPanelScreenLeftLabels.add(lblLeftOpt2);

        lblLeftOpt3.setFont(new java.awt.Font("Monospaced", 1, 14)); // NOI18N
        lblLeftOpt3.setForeground(new java.awt.Color(56, 189, 248));
        lblLeftOpt3.setText(" ");
        jPanelScreenLeftLabels.add(lblLeftOpt3);

        jPanelScreen.add(jPanelScreenLeftLabels, java.awt.BorderLayout.WEST);

        jPanelScreenRightLabels.setBackground(new java.awt.Color(11, 18, 28));
        jPanelScreenRightLabels.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 0, 20, 10));
        jPanelScreenRightLabels.setPreferredSize(new java.awt.Dimension(130, 200));
        jPanelScreenRightLabels.setLayout(new java.awt.GridLayout(3, 1, 0, 35));

        lblRightOpt1.setFont(new java.awt.Font("Monospaced", 1, 14)); // NOI18N
        lblRightOpt1.setForeground(new java.awt.Color(56, 189, 248));
        lblRightOpt1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblRightOpt1.setText(" ");
        jPanelScreenRightLabels.add(lblRightOpt1);

        lblRightOpt2.setFont(new java.awt.Font("Monospaced", 1, 14)); // NOI18N
        lblRightOpt2.setForeground(new java.awt.Color(56, 189, 248));
        lblRightOpt2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblRightOpt2.setText(" ");
        jPanelScreenRightLabels.add(lblRightOpt2);

        lblRightOpt3.setFont(new java.awt.Font("Monospaced", 1, 14)); // NOI18N
        lblRightOpt3.setForeground(new java.awt.Color(56, 189, 248));
        lblRightOpt3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblRightOpt3.setText(" ");
        jPanelScreenRightLabels.add(lblRightOpt3);

        jPanelScreen.add(jPanelScreenRightLabels, java.awt.BorderLayout.EAST);

        jPanelCenterConsole.add(jPanelScreen, java.awt.BorderLayout.CENTER);

        jPanelMain.add(jPanelCenterConsole, java.awt.BorderLayout.CENTER);

        jPanelBottomConsole.setBackground(new java.awt.Color(13, 20, 31));
        jPanelBottomConsole.setBorder(javax.swing.BorderFactory.createTitledBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(85, 85, 85), 2), "CONSOLE DO OPERADOR",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new java.awt.Font("Segoe UI", 1, 12), new java.awt.Color(170, 170, 170))); // NOI18N
        jPanelBottomConsole.setPreferredSize(new java.awt.Dimension(800, 360));
        jPanelBottomConsole.setLayout(new java.awt.GridLayout(1, 2, 30, 0));

        jPanelKeypad.setBackground(new java.awt.Color(13, 20, 31));
        jPanelKeypad.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 30, 15, 15));
        jPanelKeypad.setLayout(new java.awt.GridLayout(4, 3, 10, 10));

        btn1.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        btn1.setText("1");
        jPanelKeypad.add(btn1);

        btn2.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        btn2.setText("2");
        jPanelKeypad.add(btn2);

        btn3.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        btn3.setText("3");
        jPanelKeypad.add(btn3);

        btn4.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        btn4.setText("4");
        jPanelKeypad.add(btn4);

        btn5.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        btn5.setText("5");
        jPanelKeypad.add(btn5);

        btn6.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        btn6.setText("6");
        jPanelKeypad.add(btn6);

        btn7.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        btn7.setText("7");
        jPanelKeypad.add(btn7);

        btn8.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        btn8.setText("8");
        jPanelKeypad.add(btn8);

        btn9.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        btn9.setText("9");
        jPanelKeypad.add(btn9);

        btnBlank.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnBlank.setText("Cartão");
        jPanelKeypad.add(btnBlank);

        btn0.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        btn0.setText("0");
        jPanelKeypad.add(btn0);

        btnC.setBackground(new java.awt.Color(189, 58, 58));
        btnC.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        btnC.setForeground(new java.awt.Color(255, 255, 255));
        btnC.setText("C");
        jPanelKeypad.add(btnC);

        jPanelBottomConsole.add(jPanelKeypad);

        jPanelPeripherals.setBackground(new java.awt.Color(13, 20, 31));
        jPanelPeripherals.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 15, 30, 30));
        jPanelPeripherals.setLayout(new java.awt.GridLayout(3, 1, 0, 15));

        cardSlotContainer.setBackground(new java.awt.Color(18, 27, 38));
        cardSlotContainer.setBorder(javax.swing.BorderFactory.createTitledBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(38, 52, 71), 2), "ENTRADA DE CARTÃO",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new java.awt.Font("Segoe UI", 1, 10), new java.awt.Color(153, 153, 153))); // NOI18N
        cardSlotContainer.setLayout(new java.awt.BorderLayout());

        lblCardIndicatorLed.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblCardIndicatorLed.setForeground(new java.awt.Color(80, 200, 80));
        lblCardIndicatorLed.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblCardIndicatorLed.setText("● AGUARDANDO CARTÃO");
        cardSlotContainer.add(lblCardIndicatorLed, java.awt.BorderLayout.CENTER);

        jPanelPeripherals.add(cardSlotContainer);

        receiptPrinterContainer.setBackground(new java.awt.Color(18, 27, 38));
        receiptPrinterContainer.setBorder(javax.swing.BorderFactory.createTitledBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(38, 52, 71), 2), "IMPRESSORA DE EXTRATO",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new java.awt.Font("Segoe UI", 1, 10), new java.awt.Color(153, 153, 153))); // NOI18N
        receiptPrinterContainer.setLayout(new java.awt.BorderLayout());

        lblPrinterStatus.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblPrinterStatus.setForeground(new java.awt.Color(204, 204, 204));
        lblPrinterStatus.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblPrinterStatus.setText("PRONTA");
        receiptPrinterContainer.add(lblPrinterStatus, java.awt.BorderLayout.CENTER);

        jPanelPeripherals.add(receiptPrinterContainer);

        cashDispenserContainer.setBackground(new java.awt.Color(18, 27, 38));
        cashDispenserContainer.setBorder(javax.swing.BorderFactory.createTitledBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(38, 52, 71), 2), "DISPENSADOR DE CÉDULAS",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new java.awt.Font("Segoe UI", 1, 10), new java.awt.Color(153, 153, 153))); // NOI18N
        cashDispenserContainer.setLayout(new java.awt.BorderLayout());

        lblCashDispenserStatus.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblCashDispenserStatus.setForeground(new java.awt.Color(153, 153, 153));
        lblCashDispenserStatus.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblCashDispenserStatus.setText("FECHADO");
        cashDispenserContainer.add(lblCashDispenserStatus, java.awt.BorderLayout.CENTER);

        jPanelPeripherals.add(cashDispenserContainer);

        jPanelBottomConsole.add(jPanelPeripherals);

        jPanelMain.add(jPanelBottomConsole, java.awt.BorderLayout.SOUTH);

        getContentPane().add(jPanelMain, java.awt.BorderLayout.CENTER);

        setSize(new java.awt.Dimension(816, 839));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn0;
    private javax.swing.JButton btn1;
    private javax.swing.JButton btn2;
    private javax.swing.JButton btn3;
    private javax.swing.JButton btn4;
    private javax.swing.JButton btn5;
    private javax.swing.JButton btn6;
    private javax.swing.JButton btn7;
    private javax.swing.JButton btn8;
    private javax.swing.JButton btn9;
    private javax.swing.JButton btnBlank;
    private javax.swing.JButton btnC;
    private javax.swing.JButton btnLeft1;
    private javax.swing.JButton btnLeft2;
    private javax.swing.JButton btnLeft3;
    private javax.swing.JButton btnRight1;
    private javax.swing.JButton btnRight2;
    private javax.swing.JButton btnRight3;
    private javax.swing.JPanel cardSlotContainer;
    private javax.swing.JPanel cashDispenserContainer;
    private javax.swing.JPanel jPanelBottomConsole;
    private javax.swing.JPanel jPanelCenterConsole;
    private javax.swing.JPanel jPanelHeader;
    private javax.swing.JPanel jPanelKeypad;
    private javax.swing.JPanel jPanelLeftButtons;
    private javax.swing.JPanel jPanelMain;
    private javax.swing.JPanel jPanelPeripherals;
    private javax.swing.JPanel jPanelRightButtons;
    private javax.swing.JPanel jPanelScreen;
    private javax.swing.JPanel jPanelScreenCenter;
    private javax.swing.JPanel jPanelScreenHeader;
    private javax.swing.JPanel jPanelScreenLeftLabels;
    private javax.swing.JPanel jPanelScreenRightLabels;
    private javax.swing.JLabel lblCardIndicatorLed;
    private javax.swing.JLabel lblCashDispenserStatus;
    private javax.swing.JLabel lblHeaderTitle;
    private javax.swing.JLabel lblLeftOpt1;
    private javax.swing.JLabel lblLeftOpt2;
    private javax.swing.JLabel lblLeftOpt3;
    private javax.swing.JLabel lblPrinterStatus;
    private javax.swing.JLabel lblRightOpt1;
    private javax.swing.JLabel lblRightOpt2;
    private javax.swing.JLabel lblRightOpt3;
    private javax.swing.JLabel lblScreenHeader;
    private javax.swing.JLabel lblScreenInput;
    private javax.swing.JLabel lblScreenMessage;
    private javax.swing.JLabel lblScreenStatus;
    private javax.swing.JPanel receiptPrinterContainer;
    // End of variables declaration//GEN-END:variables
}