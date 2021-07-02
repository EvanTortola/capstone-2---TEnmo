package com.techelevator.tenmo.controller;


import com.techelevator.tenmo.Exceptions.AccountNotFoundException;
import com.techelevator.tenmo.dao.AccountDao;
import com.techelevator.tenmo.dao.JdbcAccountDao;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.User;
import org.springframework.dao.DataAccessException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@RestController
@PreAuthorize("isAuthenticated()")
public class AccountController {

    private AccountDao accountDao;
    private UserDao userDao;

    public AccountController(AccountDao accountDao, UserDao userDao){
        this.accountDao = accountDao;
        this.userDao = userDao;
    }

    //First we'll need to get the balance. GET balance from account DAO method getBalance.
    //using a path variable int id

    @RequestMapping(path = "/balance", method = RequestMethod.GET)
    public BigDecimal getBalance(Principal principal) throws AccountNotFoundException {
        Long user_id = userDao.findIdByUsername(principal.getName());
        Long account_id = accountDao.findUserById(user_id).getAccount_id();
        return accountDao.getBalance(account_id);
    }

    //Then we have to list the users. GET list of users from userDAO.

    @RequestMapping(path = "/users", method = RequestMethod.GET)
    public List<User> listUsers(){
        return userDao.findAll();
    }


}
