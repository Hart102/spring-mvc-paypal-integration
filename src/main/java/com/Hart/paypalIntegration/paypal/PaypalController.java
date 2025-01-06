package com.Hart.paypalIntegration.paypal;

import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;


// MVC Controller
@Controller
public class PaypalController {

    private static final Logger log = LoggerFactory.getLogger(PaypalController.class);

    private final PaypalService paypalService;
    public PaypalController(PaypalService paypalService) {
        this.paypalService = paypalService;
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @PostMapping("/payment/create")
    public RedirectView createPayment (
            @RequestParam String method,
            @RequestParam String amount,
            @RequestParam String currency,
            @RequestParam String description

    ) {
        try {
            String cancelUrl = "http://localhost:8080/payment/cancel";
            String successUrl = "http://localhost:8080/payment/success";

            /*
            * This creates the payment and returns the details used in
            * creating the payment and some other details.
            * */
            Payment payment = paypalService.createPayment(
                    Double.valueOf(amount),
                    currency,
                    method,
                    "sale",
                    description,
                    cancelUrl,
                    successUrl
            );

            System.out.println("Created payment: " + payment);

            // Get approval link once payment is created
            for(Links links: payment.getLinks()) {
                if (links.getRel().equals("approval_url")) {
                    System.out.println("Links: " + links);
                    return new RedirectView(links.getHref());
                }
            }
        } catch (PayPalRESTException e) {
            log.error("Error occured: ", e);
        }
        // This happens when there is Network failure
        return new RedirectView("/payment/error");
    }

    /*
    * This activates when the payment is created, and the
    *  user clicks on Continue to Review Order
    * */
    @GetMapping("/payment/success")
    public String paymentSuccess(@RequestParam String paymentId, @RequestParam String  PayerID) {

        try {
            /*
            * This returns the complete details about the payment
            * */
            Payment payment = paypalService.executePayment(paymentId, PayerID);
            System.out.println("Payment Success: " + payment);
            if (payment.getState().equals("approved")) {
                return "paymentSuccess";
            }
        } catch (PayPalRESTException e) {
            log.error("Error occurred: ", e);
        }
        return "paymentSuccess";
    }

    /*
    * This activates when payment is created and the user clicks on
    * Cancel and return to Test Store
    * */
    @GetMapping("/payment/cancel")
    public String paymentCancel() {
        return "paymentCancel";
    }

    // Payment cancel view
    @GetMapping("/payment/error")
    public String paymentError() {
        return "paymentError";
    }

}
