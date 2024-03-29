package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.Exceptions.AccountNotFoundException;
import com.techelevator.tenmo.model.Transfer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcTransferDao implements TransferDao{

    private final JdbcTemplate jdbcTemplate;
    private final AccountDao accountDao;

    public JdbcTransferDao(DataSource dataSource, AccountDao accountDao) {
        this.jdbcTemplate = new JdbcTemplate((dataSource));
        this.accountDao = accountDao;
    }


    @Override
    public List<Transfer> listAllTransfersByUserId(Long userId) {
        List<Transfer> list = new ArrayList<>();

        String sql = "SELECT transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount " +
                "FROM transfers " +
                "JOIN accounts ON account_from = account_id OR account_to = account_id " +
                "WHERE user_id = ?;";

        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId);

        while (results.next()) {
            list.add(mapRowToTransfer(results));
        }

        return list;
    }

    @Override
    public List<Transfer> listPendingTransfersByUserId(Long userId) {
        List<Transfer> list = new ArrayList<>();

        String sql = "SELECT transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount " +
                "FROM transfers " +
                "JOIN accounts ON account_from = account_id OR account_to = account_id " +
                "WHERE user_id = ? AND transfer_status_id = 1;"; // 1 representing pending

        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId);

        while (results.next()) {
            list.add(mapRowToTransfer(results));
        }

        return list;
    }

    @Override
    public Transfer getTransferDetailsByTransferId(Long transferId, String username) {

        Transfer transfer = null;

        String sql = "SELECT DISTINCT t.transfer_id, transfer_status_id, transfer_type_id, account_from, account_to, amount " +
                "FROM transfers t " +
                "JOIN accounts a ON account_from = a.account_id OR account_to = a.account_id " +
                "JOIN users u ON u.user_id = a.user_id " +
                "WHERE transfer_id = ? AND u.username = ?;";

        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, transferId, username);

        if (results.next()) {
            transfer = mapRowToTransfer(results);
        }

        return transfer;
    }

    @Override
    public boolean createTransfer(Transfer transfer, int type) throws AccountNotFoundException {

        if (type == 2) { // sending transfer
            if (accountDao.sendFunds(transfer.getAmount(), transfer.getAccount_from(), transfer.getAccount_to())) {

                String sql = "INSERT INTO transfers(transfer_type_id, transfer_status_id, account_from, account_to, amount) " +
                        "VALUES (?, ?, ?, ?, ?);";

                jdbcTemplate.update(sql, 2, 2, transfer.getAccount_from(), transfer.getAccount_to(), transfer.getAmount());
                return true;
            } else {
                return false;

            }
        } else if (type == 1) { //receiving transfer
            String sql = "INSERT INTO transfers(transfer_type_id, transfer_status_id, account_from, account_to, amount) " +
                    "VALUES (?, ?, ?, ?, ?);";

            jdbcTemplate.update(sql, 1, 1, transfer.getAccount_from(), transfer.getAccount_to(), transfer.getAmount());
            return true;
            // create a pending request transfer in transfers table, no funds are moved yet

        } else return false;




    }

    @Override
    public boolean acceptTransfer(Transfer transfer) throws AccountNotFoundException {
        // attempt to update funds in account balances

        if (accountDao.sendFunds(transfer.getAmount(), transfer.getAccount_from(), transfer.getAccount_to())) {

            String sql = "UPDATE transfers " +
                    "SET transfer_status_id = 2 " +
                    "WHERE transfer_id = ?;";

            jdbcTemplate.update(sql, transfer.getTransfer_id());
            return true;

        } else return false;

        // if that is successful, update the transfer data and return true
    }

    @Override
    public boolean rejectTransfer(Transfer transfer) {
        String sql = "UPDATE transfers " +
                "SET transfer_status_id = 3 " +
                "WHERE transfer_id = ?;";

        jdbcTemplate.update(sql, transfer.getTransfer_id());
        return true;
    }

    private Transfer mapRowToTransfer(SqlRowSet rowSet) {
        Transfer transfer = new Transfer();

        transfer.setTransfer_id(rowSet.getLong("transfer_id"));
        transfer.setTransfer_status_id(rowSet.getLong("transfer_status_id"));
        transfer.setTransfer_type_id(rowSet.getLong("transfer_type_id"));
        transfer.setAccount_from(rowSet.getLong("account_from"));
        transfer.setAccount_to(rowSet.getLong("account_to"));
        transfer.setAmount(rowSet.getBigDecimal("amount"));

        return transfer;
    }
}
