package com.caprock.controller;

import com.caprock.dto.CreateOrderRequest;
import com.caprock.dto.CreateOrderResponse;
import com.caprock.model.Payment;
import com.caprock.model.User;
import com.caprock.repository.PaymentRepository;
import com.caprock.repository.UserRepository;
import com.caprock.security.services.UserDetailsImpl;
import com.caprock.service.CreditService;
import com.caprock.service.RazorpayService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private RazorpayService razorpayService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private CreditService creditService;

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    //Create Order
    @PostMapping("/create-order")
    public ResponseEntity<CreateOrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
            ){

        CreateOrderResponse response = razorpayService.createOrder(
                request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    //Verify Payment
    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetailsImpl userDetails){

        String razorpayOrderId = body.get("razorpay_order_id");
        String razorpayPaymentId = body.get("razorpay_payment_id");
        String razorpaySignature = body.get("razorpay_signature");
        String plan = body.get("plan");

        if(razorpayPaymentId == null || razorpayOrderId == null || razorpaySignature == null || plan == null){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Missing required payment fields"
            );
        }

        //Load user and upgrade plan + credits
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not fund"
                ));

        //Verify signature
        if(!isValidSignature(razorpayOrderId, razorpayPaymentId, razorpaySignature)){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid payment signature"
            );
        }

        //Upgrade plan
        user.setPlan(plan);
        creditService.resetCredits(user);
        userRepository.save(user);

        //save payment record
        Map<String, Integer> planPrices = Map.of("starter", 9900, "pro", 29900);
        Payment payment = Payment.builder()
                .user(user)
                .amount(planPrices.getOrDefault(plan, 0))
                .plan(plan)
                .razorpayPaymentId(razorpayPaymentId)
                .razorpayOrderId(razorpayOrderId)
                .status("success")
                .build();

        paymentRepository.save(payment);

        return ResponseEntity.ok(Map.of(
                "message", "Payment verified successfully",
                "plan", user.getPlan(),
                "credits", user.getCredits()
        ));
    }

    //Webhook
    //Public endpoint - Razorpay calls this for monthly renewals
    @PostMapping("/webhook")
    public ResponseEntity<?> webhook(
            @RequestBody String payload,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature
    ){

        //Placeholder - wired later
        return ResponseEntity.ok(Map.of("status", "received"));
    }

    //HMAC Signature Verification
    private boolean isValidSignature(String orderId, String paymentId, String signature){
        try{
            String data = orderId + "|" + paymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for(byte b : hash){
                String hex = Integer.toHexString(0xff & b);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString().equals(signature);
        }catch (Exception e){
            return false;
        }
    }
}
