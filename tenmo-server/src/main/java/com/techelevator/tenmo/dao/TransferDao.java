package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.Exceptions.AccountNotFoundException;
import com.techelevator.tenmo.model.Transfer;

import java.math.BigDecimal;
import java.util.List;

public interface TransferDao {

    List<Transfer> listAllTransfersByUserId(Long userId);

    List<Transfer> listPendingTransfersByUserId(Long userId);

    Transfer getTransferDetailsByTransferId(Long transferId, String username);

    boolean createTransfer(Transfer transfer, int type) throws AccountNotFoundException;

    boolean acceptTransfer(Transfer transfer) throws AccountNotFoundException;

    boolean rejectTransfer(Transfer transfer);

}
