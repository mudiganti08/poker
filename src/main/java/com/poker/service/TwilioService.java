package com.poker.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;

@Service
public class TwilioService {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.from}")
    private String fromPhone;

    private boolean initialized = false;

    private void init() {
        if (!initialized) {
            Twilio.init(accountSid, authToken);
            initialized = true;
        }
    }

    public void sendSms(String toPhone, String message) {
        init();
        Message.creator(
                new com.twilio.type.PhoneNumber(toPhone),
                new com.twilio.type.PhoneNumber(fromPhone),
                message
        ).create();
    }
}


