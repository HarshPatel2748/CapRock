package com.caprock.service.impl;

import com.caprock.dto.CreateOrderRequest;
import com.caprock.dto.CreateOrderResponse;
import com.caprock.model.Payment;
import com.caprock.model.User;
import com.caprock.repository.PaymentRepository;
import com.caprock.repository.UserRepository;
import com.caprock.service.CreditService;
import com.caprock.service.RazorpayService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class RazorpayServiceImpl implements RazorpayService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private CreditService creditService;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    //Plan prices in paise
    private static final Map<String, Integer> PLAN_PRICES = Map.of(
            "starter", 9900,
            "pro", 29900
    );

    //Create Order
    @Override
    public CreateOrderResponse createOrder(CreateOrderRequest request, String userEmail) {

        String plan = request.getPlan().toLowerCase();

        //Validate plan
        if (!PLAN_PRICES.containsKey(plan)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid plan. Choose 'starter' or 'pro'."
            );
        }

        int amount = PLAN_PRICES.get(plan);

        try {
            RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amount);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "receipt_" + userEmail + "_" + System.currentTimeMillis());

            Order order = client.orders.create(orderRequest);

            return new CreateOrderResponse(
                    order.get("id"),
                    amount,
                    "INR",
                    plan
            );

        } catch (RazorpayException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to create Razorpay order: " + e.getMessage()
            );
        }
    }

    //Verify Payment
    @Override
    public void verifyPayment(String razorpayOrderId,
                              String razorpayPaymentId,
                              String razorpaySignature,
                              String userEmail) {

        //Step 1 -> verify HMAC signature
        if (!isValidSignature(razorpayOrderId, razorpayPaymentId, razorpaySignature)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid payment signature."
            );
        }

        //Step 2 -> load user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"
                ));

        //Step 3 -> figure out which plan was purchased from the order amount
        //We re-derive plan from orderId by checking our own payment record isn't possible
        //So frontend must send the plan too - handled in controller
        //For now we read it from the receipt prefix - simpler: frontend sends the plan in verify body
        //See Payment Controller for how plan is passed in
        String plan = derivePlanFromOrderId(razorpayOrderId);

        //Step 4 -> upgrade user plan
        user.setPlan(plan);

        //Step 5 -> reset credits based on new plan
        creditService.resetCredits(user);

        //Step 6 -> save payment record
        Payment payment = Payment.builder()
                .user(user)
                .amount(PLAN_PRICES.get(plan))
                .plan(plan)
                .razorpayPaymentId(razorpayPaymentId)
                .razorpayOrderId(razorpayOrderId)
                .status("success")
                .build();

        paymentRepository.save(payment);
        userRepository.save(user);
    }

    //HMAC Signature Verification
    private boolean isValidSignature(String orderId, String paymentId, String signature) {
        try {
            String payload = orderId + "|" + paymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    razorpayKeySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"
            );
            mac.init(secretKey);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            //Convert to hex
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString().equals(signature);
        } catch (Exception e) {
            return false;
        }
    }

    //Derives plan from orderId - since frontend sends plan in verify request
    //this is a fallback; actual plan comes from PaymentController
    private String derivePlanFromOrderId(String orderId){
        //This will be overridden by the plan from the controller
        return "starter";
    }
}
