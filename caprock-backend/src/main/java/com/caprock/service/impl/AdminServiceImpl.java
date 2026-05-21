package com.caprock.service.impl;

import com.caprock.dto.AdminUserResponse;
import com.caprock.model.Payment;
import com.caprock.model.User;
import com.caprock.repository.PaymentRepository;
import com.caprock.repository.UserRepository;
import com.caprock.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentRepository paymentRepository;


    //Stats
    @Override
    public Map<String, Object> getStats(){

        long totalUsers = userRepository.count();

        //Total revenue - sum all successful payment amounts
        List<Payment> allPayments = paymentRepository.findAll();
        int totalRevenuePaise = allPayments.stream()
                .mapToInt(Payment::getAmount)
                .sum();
        //Convert paise to rupees
        double totalRevenueRupees = totalRevenuePaise / 100.00;

        //Count users by plan
        List<User> allUsers = userRepository.findAll();
        long freeUsers = allUsers.stream()
                .filter(u -> "free".equals(u.getPlan())).count();
        long starterUsers = allUsers.stream()
                .filter(u -> "starter".equals(u.getPlan())).count();
        long proUsers = allUsers.stream()
                .filter(u -> "pro".equals(u.getPlan())).count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("totalPayments", allPayments.size());
        stats.put("totalPaymentRupees", totalRevenueRupees);
        stats.put("freeUsers", freeUsers);
        stats.put("starterUsers", starterUsers);
        stats.put("proUsers", proUsers);

        return stats;
    }

    //Get all users
    @Override
    public List<AdminUserResponse> getAllUsers(){
        return userRepository.findAll()
                .stream()
                .map(AdminUserResponse::new)
                .toList();
    }

    //Update user
    @Override
    public AdminUserResponse updateUser(Long userId, String plan, Integer credits){

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"
                ));

        //Only update fields that were actually sent
        if(plan != null && !plan.isBlank()){
            user.setPlan(plan);
        }

        if(credits != null){
            user.setCredits(credits);
        }

        userRepository.save(user);

        return new AdminUserResponse(user);
    }
}
