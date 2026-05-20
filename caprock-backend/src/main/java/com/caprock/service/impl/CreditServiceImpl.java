package com.caprock.service.impl;

import com.caprock.model.User;
import com.caprock.repository.UserRepository;
import com.caprock.service.CreditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;

@Service
public class CreditServiceImpl implements CreditService {

    @Autowired
    private UserRepository userRepository;

    //Check credits
    @Override
    public void checkCredits(User user){
        if(user.getCredits() <= 0){
            throw new ResponseStatusException(
                    HttpStatus.PAYMENT_REQUIRED,
                    "You have no credits left. Please upgrade your plan."
            );
        }
    }

    //Deduct credits
    @Override
    public void deductCredit(User user){
        user.setCredits(user.getCredits() - 1);
        userRepository.save(user);
    }

    //Reset credits
    @Override
    public void resetCredits(User user){
        int resetAmount = switch (user.getPlan()){
            case "starter" -> 50;
            case "pro" -> 200;
            default -> 0;
        };

        if(resetAmount > 0){
            user.setCredits(resetAmount);
            user.setCreditsResetAt(OffsetDateTime.now());
            userRepository.save(user);
        }
    }
}
