package com.caprock.service;

import com.caprock.model.User;

public interface CreditService {

    void checkCredits(User user);

    void deductCredit(User user);

    void resetCredits(User user);
}
