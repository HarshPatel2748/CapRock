package com.caprock.service;

import com.caprock.dto.CreateOrderRequest;
import com.caprock.dto.CreateOrderResponse;

public interface RazorpayService {

    //Creates a Razorpay order and returns orderId + amount
    CreateOrderResponse createOrder(CreateOrderRequest request, String userEmail);

    //Verifies HMAC signature and upgrades user plan + credits
    void verifyPayment(String razorpayOrderId,
                       String razorpayPaymentId,
                       String razorpaySignature,
                       String userEmail);
}
